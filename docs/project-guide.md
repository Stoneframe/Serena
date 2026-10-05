# Serena project guide

Use the relevant section for repository navigation, file placement, or build setup.
Follow [AGENTS.md](../AGENTS.md) for workflow, verification policy, and core state invariants.

## Repository structure

- Keep production code in the single `app` module under `app/src/main/java/stoneframe/serena/`.
  Feature packages contain domain managers, editors, and models; `gui/` contains Android screens,
  adapters, and notification handling; `storages/` contains persistence, with JSON adapters in
  `storages/json/` and migrations in `storages/versions/`; `timeservices/` supplies the clock seam.
- Keep layouts, styles, strings, and other resources under `app/src/main/res/`, and components and
  permissions in `app/src/main/AndroidManifest.xml`.
- Put JVM tests under `app/src/test/java/stoneframe/serena/`, mirroring production packages.
  Use the existing JUnit 4 setup and reuse suitable handwritten doubles and fixtures in `mocks/`.
- Extend the existing Java managers/editors and XML Views approach unless the task requires broader
  architecture. Follow applicable rules in [code-style.md](code-style.md).
- Use the [architecture analysis](architecture.md) when component boundaries or state ownership are
  unclear. Verify its dated findings against current code; its proposals do not authorize broader work.

## Build and verification

- Run the checked-in wrapper from the repository root with a compatible JDK, Android SDK, and writable
  Gradle cache. Use `.\gradlew.bat` in PowerShell or `./gradlew` in POSIX shells.
- Inspect `app/build.gradle`, root Gradle files, and `gradle/wrapper/` when build behavior, compatibility,
  dependencies, or new language features matter. Do not infer language settings from IDE defaults
  or edit generated build outputs.
- Use [Running checks](testing.md#running-checks) for unit-test, debug APK, and lint commands, report
  locations, and interpretation of fresh, cached, or dry-run results. Inspect reports for the intended
  run and variant. Use `--offline` only when the wrapper distribution and dependencies are cached.
- Run `git diff --check` for tracked diff whitespace; inspect untracked files separately.
- Release builds follow the [serena-release skill](../.agents/skills/serena-release/SKILL.md).
