---
name: serena-release
description: Guide the Serena Android release workflow when preparing, verifying, finalizing, tagging, or handing off a release. Use the repository's release script; do not use this skill for ordinary Android builds or unrelated version changes.
---

# Serena Release

Use this skill for Serena release work. Read `RELEASE.md` and
`scripts/release.ps1` before acting; they are the repository's source of truth.
Use the PowerShell release script instead of reimplementing its checks.

## Prepare

1. Confirm the requested release version. If no version was supplied, inspect
   `app/build.gradle`, `CHANGELOG.md`, and Git tags, then ask for the intended
   version rather than guessing.
2. Confirm the current branch is `master` and that there are no unrelated
   working-tree changes. Never stash, discard, overwrite, or commit unrelated
   work to make the release proceed.
3. Run, from the repository root:

   ```powershell
   .\scripts\release.ps1 -Prepare -VersionName <version>
   ```

   The script increments `versionCode`, updates `versionName`, and inserts the
   dated changelog entry. Pass `-VersionCode <number>` only when a specific
   higher external build number is required.
4. Review the diff and replace every generated `TODO: describe` entry in
   `CHANGELOG.md`. Do not continue to verification while TODO entries remain.

## Verify

Run:

```powershell
.\scripts\release.ps1 -VersionName <version> -Verify
```

Verification runs the tests, builds the signed release APK, copies it to
`app/release/`, validates the APK signature and package metadata, and prints
the SHA-256 checksum. The release signing configuration is local and ignored:
use `keystore.properties` and its configured keystore, but never print, commit,
or request secret values in chat. If signing setup is missing or invalid, stop
and explain what must be configured locally.

After verification, report the APK path, checksum, and validation result. Ask
the user to perform or confirm the manual smoke tests listed in `RELEASE.md`.

## Finalize and publish

Only finalize after the user explicitly approves it. Run:

```powershell
.\scripts\release.ps1 -VersionName <version> -Finalize
```

Finalization reruns verification, commits only `app/build.gradle` and
`CHANGELOG.md` as `Release <version>`, and creates the lightweight tag
`Release-<version>`. It does not push or upload the APK. Never push implicitly;
only after a separate explicit request run:

```powershell
git push origin master
git push origin Release-<version>
```

Report the final commit, tag, APK path, checksum, and any manual handoff still
pending. Do not include keystore files, `keystore.properties`, or the ignored
`app/release/` APK in commits.

If the user only asks how the process works, explain these stages without
running commands. If any command fails, preserve the working tree and report
the concrete failure and the safest next action.
