# Serena release process

This project uses a version bump in `app/build.gradle`, a tracked changelog entry,
and a lightweight `Release-<version>` Git tag. Release credentials and keystores
remain local and are never committed.

## One-time local signing setup

1. Create a local `keystore.properties` file with these keys:

   ```properties
   storeFile=path/to/serena-release.keystore
   storePassword=your-store-password
   keyAlias=your-key-alias
   keyPassword=your-key-password
   ```

2. Keep the keystore and `keystore.properties` outside version control.
3. Ensure the machine has a writable Gradle cache. If Gradle reports an inaccessible
   wrapper lock, set `GRADLE_USER_HOME` to a writable local cache directory before running the release script.

## Prepare a release

Commit the release tooling (`.gitignore`, `RELEASE.md`, and `scripts/release.ps1`)
before preparing the release. Leave `app/build.gradle`
and `CHANGELOG.md` as the release changes for finalization. The script intentionally
rejects unrelated working-tree changes.

1. Finish and review the changes intended for the release.
2. From `master`, prepare the next version. For example:

   ```powershell
   .\scripts\release.ps1 -Prepare -VersionName 1.8
   ```

   This increments `versionCode`, updates `versionName`, and adds a dated
   changelog skeleton. Review the generated changes and replace all TODO entries.
   Pass `-VersionCode <number>` only when an external build has a higher code
   than the repository.

3. Confirm the working tree contains no unrelated changes.
4. Verify the release:

   ```powershell
   .\scripts\release.ps1 -VersionName 1.8 -Verify
   ```

The verification mode runs unit tests, builds the signed release APK, copies it
to `app\release\`, checks its signature and package metadata, and reports its
SHA-256 checksum.

## Finalize and tag

After verification succeeds, run:

```powershell
.\scripts\release.ps1 -VersionName 1.8 -Finalize
git push origin master
git push origin Release-1.8
```

Finalization creates the release commit and lightweight `Release-1.8` tag. It does
not push commits or upload the APK.

## Manual handoff

Install `app\release\Serena-v<version>-release.apk` over the previous release
and perform the smoke checks for reminders, sleep sessions, Today, tasks,
Balancers, routines, notes, and data migrations. Hand off the verified APK
together with the checksum and changelog.
