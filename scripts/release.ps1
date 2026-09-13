[CmdletBinding()]
param(
    [Parameter(Mandatory = $true)]
    [ValidatePattern('^\d+\.\d+$')]
    [string]$VersionName,

    [Parameter(Mandatory = $true)]
    [ValidateRange(1, 2147483647)]
    [int]$VersionCode,

    [Parameter(Mandatory = $true, ParameterSetName = 'Verify')]
    [switch]$Verify,

    [Parameter(Mandatory = $true, ParameterSetName = 'Finalize')]
    [switch]$Finalize
)

$ErrorActionPreference = 'Stop'
$repoRoot = (Resolve-Path (Join-Path $PSScriptRoot '..')).Path
$buildGradle = Join-Path $repoRoot 'app\build.gradle'
$changelog = Join-Path $repoRoot 'CHANGELOG.md'
$keystorePropertiesFile = Join-Path $repoRoot 'keystore.properties'
$releaseDirectory = Join-Path $repoRoot 'app\release'
$gradlew = Join-Path $repoRoot 'gradlew.bat'
$tagName = "Release-$VersionName"
$releaseCommitMessage = "Release $VersionName"
$allowedReleaseFiles = @('app/build.gradle', 'CHANGELOG.md')

function Fail([string]$Message) {
    throw $Message
}

function Invoke-Git([string[]]$Arguments) {
    & git -C $repoRoot @Arguments
    if ($LASTEXITCODE -ne 0) {
        Fail "Git command failed: git $($Arguments -join ' ')"
    }
}

function Get-GitOutput([string[]]$Arguments) {
    $output = & git -C $repoRoot @Arguments
    if ($LASTEXITCODE -ne 0) {
        Fail "Git command failed: git $($Arguments -join ' ')"
    }
    return @($output)
}

function Invoke-Gradle([string[]]$Tasks) {
    Push-Location $repoRoot
    try {
        & $gradlew @Tasks
        if ($LASTEXITCODE -ne 0) {
            Fail "Gradle command failed: .\gradlew.bat $($Tasks -join ' ')"
        }
    }
    finally {
        Pop-Location
    }
}

function Get-SdkDirectory {
    $candidates = @()
    if ($env:ANDROID_SDK_ROOT) { $candidates += $env:ANDROID_SDK_ROOT }
    if ($env:ANDROID_HOME) { $candidates += $env:ANDROID_HOME }

    $localProperties = Join-Path $repoRoot 'local.properties'
    if (Test-Path -LiteralPath $localProperties) {
        $sdkLine = Get-Content -LiteralPath $localProperties | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
        if ($sdkLine) {
            $sdkValue = $sdkLine -replace '^sdk\.dir=', ''
            $sdkValue = $sdkValue -replace '\\:', ':'
            $sdkValue = $sdkValue -replace '\\\\', '\'
            $candidates += $sdkValue
        }
    }

    foreach ($candidate in $candidates) {
        if ($candidate -and (Test-Path -LiteralPath $candidate)) {
            return (Resolve-Path -LiteralPath $candidate).Path
        }
    }
    return $null
}

function Get-BuildTool([string]$ToolName) {
    $sdkDirectory = Get-SdkDirectory
    if (-not $sdkDirectory) { return $null }

    $buildToolsDirectory = Join-Path $sdkDirectory 'build-tools'
    if (-not (Test-Path -LiteralPath $buildToolsDirectory)) { return $null }

    return Get-ChildItem -LiteralPath $buildToolsDirectory -Recurse -File -Filter $ToolName |
        Sort-Object FullName -Descending |
        Select-Object -First 1
}

function Assert-WorkingTree {
    $statusLines = @(Get-GitOutput @('status', '--porcelain'))
    foreach ($statusLine in $statusLines) {
        if ($statusLine.Length -lt 4) { continue }
        $path = $statusLine.Substring(3).Trim().Replace('\', '/')
        if ($path -notin $allowedReleaseFiles) {
            Fail "Working tree contains an unrelated change: $path"
        }
    }
}

function Assert-ReleaseMetadata {
    $gradleText = Get-Content -LiteralPath $buildGradle -Raw
    $versionNameMatch = [regex]::Match($gradleText, 'versionName\s+["'']([^"'']+)["'']')
    $versionCodeMatch = [regex]::Match($gradleText, 'versionCode\s+(\d+)')
    if (-not $versionNameMatch.Success -or $versionNameMatch.Groups[1].Value -ne $VersionName) {
        Fail "app/build.gradle must set versionName to $VersionName."
    }
    if (-not $versionCodeMatch.Success -or [int]$versionCodeMatch.Groups[1].Value -ne $VersionCode) {
        Fail "app/build.gradle must set versionCode to $VersionCode."
    }

    if (-not (Test-Path -LiteralPath $changelog)) {
        Fail 'CHANGELOG.md is missing.'
    }
    $headingPattern = "^##\s+$([regex]::Escape($VersionName))(\s|$)"
    if (-not (Select-String -LiteralPath $changelog -Pattern $headingPattern -Quiet)) {
        Fail "CHANGELOG.md must contain a heading for version $VersionName."
    }
}

function Assert-SigningProperties {
    if (-not (Test-Path -LiteralPath $keystorePropertiesFile)) {
        Fail 'keystore.properties is required for a signed release APK. Copy keystore.properties.example and fill it in locally.'
    }

    $properties = @{}
    foreach ($line in Get-Content -LiteralPath $keystorePropertiesFile) {
        if ($line -match '^\s*([^#=]+?)\s*=\s*(.*)\s*$') {
            $properties[$matches[1].Trim()] = $matches[2].Trim()
        }
    }
    foreach ($key in @('storeFile', 'storePassword', 'keyAlias', 'keyPassword')) {
        if (-not $properties.ContainsKey($key) -or [string]::IsNullOrWhiteSpace($properties[$key])) {
            Fail "keystore.properties is missing $key."
        }
    }

    $storeFile = $properties['storeFile']
    if (-not [System.IO.Path]::IsPathRooted($storeFile)) {
        $storeFile = Join-Path $repoRoot $storeFile
    }
    if (-not (Test-Path -LiteralPath $storeFile -PathType Leaf)) {
        Fail "The configured keystore does not exist: $storeFile"
    }
}

function Assert-NoExistingTag {
    $existingTag = @(Get-GitOutput @('tag', '--list', $tagName))
    if ($existingTag.Count -gt 0) {
        Fail "Tag $tagName already exists."
    }
}

function Assert-ReleaseArtifact {
    $apkDirectory = Join-Path $repoRoot 'app\build\outputs\apk\release'
    $apkFiles = @(Get-ChildItem -LiteralPath $apkDirectory -File -Filter '*.apk' -ErrorAction SilentlyContinue |
        Where-Object { $_.Name -notmatch '-unsigned\.apk$' })
    if ($apkFiles.Count -ne 1) {
        Fail "Expected exactly one signed release APK in $apkDirectory. Remove stale outputs and verify signing configuration."
    }
    $builtApk = $apkFiles[0]
    if ($builtApk.Name -notmatch [regex]::Escape("Serena-v$VersionName")) {
        Fail "Release APK has an unexpected name: $($builtApk.Name)"
    }

    New-Item -ItemType Directory -Path $releaseDirectory -Force | Out-Null
    $releaseApkPath = Join-Path $releaseDirectory $builtApk.Name
    Copy-Item -LiteralPath $builtApk.FullName -Destination $releaseApkPath -Force
    $apk = Get-Item -LiteralPath $releaseApkPath

    $apksigner = Get-BuildTool 'apksigner.bat'
    $aapt = Get-BuildTool 'aapt.exe'
    if (-not $apksigner -or -not $aapt) {
        Fail 'Android build-tools with apksigner.bat and aapt.exe are required to validate the release APK.'
    }

    & $apksigner.FullName verify --verbose $apk.FullName
    if ($LASTEXITCODE -ne 0) {
        Fail "APK signature verification failed: $($apk.FullName)"
    }

    $badging = & $aapt.FullName dump badging $apk.FullName
    if ($LASTEXITCODE -ne 0) {
        Fail "Could not read APK metadata: $($apk.FullName)"
    }
    $packageLine = $badging | Select-Object -First 1
    $metadataPattern = "name='stoneframe\.chorelist'.*versionCode='$VersionCode'.*versionName='$([regex]::Escape($VersionName))'"
    if ($packageLine -notmatch $metadataPattern) {
        Fail "APK metadata does not match stoneframe.chorelist/$VersionName ($VersionCode): $packageLine"
    }

    $hash = Get-FileHash -LiteralPath $apk.FullName -Algorithm SHA256
    Write-Output "Verified APK: $($apk.FullName)"
    Write-Output "SHA-256: $($hash.Hash)"
}

$branch = (Get-GitOutput @('branch', '--show-current') | Select-Object -First 1).Trim()
if ($branch -ne 'master') {
    Fail "Releases must be finalized from master; current branch is $branch."
}

Assert-WorkingTree
Assert-ReleaseMetadata
Assert-SigningProperties
Assert-NoExistingTag

Invoke-Git @('diff', '--check', '--', 'app/build.gradle', 'CHANGELOG.md')
Invoke-Gradle @('test')
Invoke-Gradle @('assembleRelease')
Assert-ReleaseArtifact

if ($Finalize) {
    Invoke-Git @('add', '--', 'app/build.gradle', 'CHANGELOG.md')
    $stagedChanges = @(Get-GitOutput @('diff', '--cached', '--name-only'))
    if ($stagedChanges.Count -eq 0) {
        Fail 'No release metadata or changelog changes are staged for finalization.'
    }
    Invoke-Git @('commit', '-m', $releaseCommitMessage)
    Invoke-Git @('tag', $tagName)
    Write-Output "Created commit '$releaseCommitMessage' and tag '$tagName'."
}
else {
    Write-Output "Release $VersionName verification completed. Run with -Finalize to commit and tag it."
}
