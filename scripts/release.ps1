[CmdletBinding(DefaultParameterSetName = 'Verify')]
param(
    [Parameter(Mandatory = $true, ParameterSetName = 'Prepare')]
    [Parameter(Mandatory = $true, ParameterSetName = 'Verify')]
    [Parameter(Mandatory = $true, ParameterSetName = 'Finalize')]
    [ValidatePattern('^\d+\.\d+$')]
    [string]$VersionName,

    [Parameter(ParameterSetName = 'Prepare')]
    [Parameter(ParameterSetName = 'Verify')]
    [Parameter(ParameterSetName = 'Finalize')]
    [ValidateRange(1, 2147483647)]
    [int]$VersionCode,

    [Parameter(Mandatory = $true, ParameterSetName = 'Prepare')]
    [switch]$Prepare,

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

function Get-AppMetadata {
    $gradleText = Get-Content -LiteralPath $buildGradle -Raw
    $versionNameMatch = [regex]::Match($gradleText, 'versionName\s+["'']([^"'']+)["'']')
    $versionCodeMatch = [regex]::Match($gradleText, 'versionCode\s+(\d+)')
    if (-not $versionNameMatch.Success -or -not $versionCodeMatch.Success) {
        Fail 'Could not read versionName and versionCode from app/build.gradle.'
    }

    return [pscustomobject]@{
        VersionName = $versionNameMatch.Groups[1].Value
        VersionCode = [int]$versionCodeMatch.Groups[1].Value
    }
}

function Set-AppMetadata([string]$NewVersionName, [int]$NewVersionCode) {
    $gradleText = Get-Content -LiteralPath $buildGradle -Raw
    $versionNamePattern = '(?m)^([ \t]*versionName[ \t]+["''])([^"'']+)(["''][ \t]*)(\r?)$'
    $versionCodePattern = '(?m)^([ \t]*versionCode[ \t]+)\d+([ \t]*)(\r?)$'
    if ([regex]::Matches($gradleText, $versionNamePattern).Count -ne 1) {
        Fail 'Expected exactly one versionName line in app/build.gradle.'
    }
    if ([regex]::Matches($gradleText, $versionCodePattern).Count -ne 1) {
        Fail 'Expected exactly one versionCode line in app/build.gradle.'
    }

    $updatedText = [regex]::Replace(
            $gradleText,
            $versionNamePattern,
            '${1}' + $NewVersionName + '${3}${4}',
            1
    )
    $updatedText = [regex]::Replace(
            $updatedText,
            $versionCodePattern,
            '${1}' + $NewVersionCode + '${2}${3}',
            1
    )
    [System.IO.File]::WriteAllText(
            $buildGradle,
            $updatedText,
            [System.Text.UTF8Encoding]::new($false)
    )
}

function Add-ChangelogEntry([string]$NewVersionName) {
    $changelogText = Get-Content -LiteralPath $changelog -Raw
    $headingPattern = "(?m)^##\s+$([regex]::Escape($NewVersionName))(\s|$)"
    if ([regex]::IsMatch($changelogText, $headingPattern)) {
        Fail "CHANGELOG.md already contains an entry for version $NewVersionName."
    }

    $firstLineEnd = $changelogText.IndexOf("`n")
    if ($firstLineEnd -lt 0 -or $changelogText.Substring(0, $firstLineEnd).Trim("`r") -ne '# Changelog') {
        Fail 'CHANGELOG.md must begin with a # Changelog heading.'
    }

    $lineEnding = if ($changelogText.Contains("`r`n")) { "`r`n" } else { "`n" }
    $date = Get-Date -Format 'yyyy-MM-dd'
    $entry = @(
        "## $NewVersionName - $date",
        '',
        '### Added',
        '',
        '- TODO: describe additions.',
        '',
        '### Changed',
        '',
        '- TODO: describe changes.',
        '',
        '### Fixed',
        '',
        '- TODO: describe fixes.',
        ''
    ) -join $lineEnding
    $entry = $lineEnding + $entry + $lineEnding
    $insertAt = $firstLineEnd + 1
    $updatedText = $changelogText.Substring(0, $insertAt) + $entry + $changelogText.Substring($insertAt)
    [System.IO.File]::WriteAllText(
            $changelog,
            $updatedText,
            [System.Text.UTF8Encoding]::new($false)
    )
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

function Assert-ReleaseMetadata([int]$ExpectedVersionCode) {
    $metadata = Get-AppMetadata
    if ($metadata.VersionName -ne $VersionName) {
        Fail "app/build.gradle must set versionName to $VersionName."
    }
    if ($metadata.VersionCode -ne $ExpectedVersionCode) {
        Fail "app/build.gradle must set versionCode to $ExpectedVersionCode."
    }

    if (-not (Test-Path -LiteralPath $changelog)) {
        Fail 'CHANGELOG.md is missing.'
    }
    $headingPattern = "^##\s+$([regex]::Escape($VersionName))(\s|$)"
    if (-not (Select-String -LiteralPath $changelog -Pattern $headingPattern -Quiet)) {
        Fail "CHANGELOG.md must contain a heading for version $VersionName."
    }
    if (Select-String -LiteralPath $changelog -Pattern 'TODO: describe' -Quiet) {
        Fail 'Replace the generated TODO changelog entries before verification.'
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

function Assert-ReleaseArtifact([int]$ExpectedVersionCode) {
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
    $metadataPattern = "name='stoneframe\.chorelist'.*versionCode='$ExpectedVersionCode'.*versionName='$([regex]::Escape($VersionName))'"
    if ($packageLine -notmatch $metadataPattern) {
        Fail "APK metadata does not match stoneframe.chorelist/$VersionName ($ExpectedVersionCode): $packageLine"
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
Assert-NoExistingTag

$currentMetadata = Get-AppMetadata
if ($Prepare) {
    if ($currentMetadata.VersionName -eq $VersionName) {
        Fail "Version $VersionName is already configured in app/build.gradle."
    }

    $newVersionCode = if ($PSBoundParameters.ContainsKey('VersionCode')) {
        $VersionCode
    }
    else {
        $currentMetadata.VersionCode + 1
    }
    if ($newVersionCode -le $currentMetadata.VersionCode) {
        Fail "The new versionCode ($newVersionCode) must be greater than the current versionCode ($($currentMetadata.VersionCode))."
    }

    Set-AppMetadata -NewVersionName $VersionName -NewVersionCode $newVersionCode
    Add-ChangelogEntry -NewVersionName $VersionName
    Write-Output "Prepared version $VersionName with versionCode $newVersionCode."
    Write-Output 'Review CHANGELOG.md, then run -Verify.'
    exit 0
}

$expectedVersionCode = if ($PSBoundParameters.ContainsKey('VersionCode')) {
    if ($VersionCode -ne $currentMetadata.VersionCode) {
        Fail "VersionCode argument $VersionCode does not match app/build.gradle ($($currentMetadata.VersionCode))."
    }
    $VersionCode
}
else {
    $currentMetadata.VersionCode
}

Assert-ReleaseMetadata -ExpectedVersionCode $expectedVersionCode
Assert-SigningProperties
Invoke-Git @('diff', '--check', '--', 'app/build.gradle', 'CHANGELOG.md')
Invoke-Gradle @('test')
Invoke-Gradle @('assembleRelease')
Assert-ReleaseArtifact -ExpectedVersionCode $expectedVersionCode

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
