# Serena agent guide

## Working rules

- Follow **Inspect → Plan → Implement → Verify**, scaling each phase to the task's complexity and risk.
- Before proposing changes, inspect the relevant implementation, tests, configuration, documentation,
  and Git status. Prefer observed behavior and tests over implementation, configuration, documentation,
  comments, and assumptions, in that order. Point out conflicts with the requested assumptions.
- Before introducing new code, abstractions, helpers, utilities, patterns, or dependencies, search the
  existing codebase for equivalent or closely related functionality. Prefer reusing or extending
  established solutions when they are appropriate. Do not force reuse when the existing solution is
  unsuitable; the goal is to discover before inventing.
- When planning a change, identify relevant existing patterns and components that can be reused or
  extended.
- For non-trivial changes, state the current behavior, intended change, affected components, risks,
  and verification approach before editing.
- Write plans so that a lower-capability model can execute them without relying on unstated context:
  name the relevant files, concrete steps, assumptions, edge cases, and verification commands.
- Make the smallest sufficient conceptual change. Preserve behavior outside the request and unrelated
  user changes. Avoid unrelated refactoring, renaming, formatting, abstractions, or dependency changes.
- After implementing, actively look for incorrect assumptions, regressions, boundary cases, state and
  concurrency problems, and missing tests. Fix discovered problems and verify again.
- Review the final diff and explain verification limits. Do not claim completion solely from reading code.

## Repository map and conventions

- Before working in a subdirectory, search for more specific nested `AGENTS.md` files. Read all
  applicable instruction files from the repository root toward the target; the most specific file
  governs its subtree when instructions conflict.
- Keep production code in the single `app` module, under `app/src/main/java/stoneframe/serena/`.
  Feature packages contain domain managers, editors, and models; `gui/` contains Android screens,
  adapters, and notification handling; `storages/` contains persistence, with JSON adapters in
  `storages/json/` and migrations in `storages/versions/`; `timeservices/` supplies the clock seam.
- Keep XML layouts, styles, strings, and other resources under `app/src/main/res/`, and component and
  permission declarations in `app/src/main/AndroidManifest.xml`.
- Put JVM tests under `app/src/test/java/stoneframe/serena/`, mirroring production packages.
  Reuse the handwritten doubles and fixtures in `mocks/` where suitable. Use the existing JUnit 4 setup.
- Follow [docs/code-style.md](docs/code-style.md) for Java, tests, XML, Groovy, and Markdown.
  Apply its required rules to new and substantially changed code; treat explicitly labeled
  recommendations as guidance. Preserve surrounding legacy conventions outside the change.
- Consult [docs/architecture.md](docs/architecture.md) for component relationships and risk areas.
  Treat its findings and verification results as a dated analysis; check current code before relying
  on them. Do not interpret its improvement proposals as authorization to expand a task.
- Inspect `app/build.gradle`, root Gradle files, and `gradle/wrapper/` for current build configuration.
  Do not infer language settings from local IDE defaults or edit generated build outputs.

## Architecture, persistence, and state

- Extend the existing Java managers/editors and XML Views approach unless the task requires a broader
  architectural change. Trace the affected screen, manager/editor, persistence, and notification paths.
- Account for the shared mutable `Container` owned by `Serena`. Managers resolve feature containers
  through suppliers; selected entities and editors can retain stale references after `Serena.load()`.
  Do not assume query methods are side-effect free or returned model objects are isolated copies.
- Distinguish editor/model checkpoints from persistence: `Revertible.save()` records an in-memory
  checkpoint, while `Serena.save()` delegates to storage. Verify Save, Cancel, refresh, and alarm
  rescheduling behavior at the affected call sites; do not assume a manager mutation persists itself.
- Preserve stored-data compatibility. `SharedPreferencesStorage` serializes the whole aggregate as
  JSON, and Gson field names, including private fields, can form part of the schema. Do not rename
  persisted fields, change sentinel values, or restructure models as style cleanup.
- When changing the schema, inspect `JsonConverter` and its ordered migration registration. Test
  representative older inputs through the affected migration chain, current-data round trips, and
  preservation of user data. An isolated upgrade-script test does not verify the complete chain.
- For import/replacement changes, test malformed and unsupported inputs and ensure failure preserves
  the last usable saved data. Never use real user data as a destructive test fixture.
- Use the existing `TimeService` seam for new time-dependent domain behavior and deterministic test
  dates. Check relevant date boundaries, recurrence, time-zone/DST conversion, and clock changes.
- For lifecycle or notification changes, consider backgrounding, recreation, process loss, cold receiver
  entry, permissions, and alarm recovery where relevant. `GlobalState` selections are process-local;
  they are not durable restoration state. Pair listener registration and callback/resource cleanup.
- Before moving model or storage operations off the main thread, define state ownership and write
  ordering; do not assume the shared graph is thread-safe.

## Build and verification commands

Run commands from the repository root using the checked-in wrapper. These examples use PowerShell;
use `./gradlew` in a POSIX shell. Provide a compatible JDK, Android SDK, and writable Gradle cache.

| Purpose | Command |
| --- | --- |
| Debug JVM unit tests | `.\gradlew.bat :app:testDebugUnitTest --console=plain` |
| One test class | `.\gradlew.bat :app:testDebugUnitTest --tests "stoneframe.serena.reminders.ReminderManagerTest" --console=plain` |
| Debug APK | `.\gradlew.bat :app:assembleDebug --console=plain` |
| Android lint | `.\gradlew.bat :app:lintDebug --console=plain` |
| Tracked diff whitespace | `git diff --check` |

- Choose checks appropriate to the change: relevant unit tests for domain logic, a debug build for
  code/resource integration, and lint for Android-facing changes. For documentation-only edits,
  verify accuracy, links, commands, and the diff; an application rebuild is usually unnecessary.
- Use `--offline` only when dependencies are cached. Report environment failures separately from
  code failures. Do not change dependencies or signing configuration merely to bypass a local failure.
- Inspect unit-test results under `app/build/test-results/testDebugUnitTest/` and lint reports under
  `app/build/reports/`. Distinguish cached/up-to-date results and dry runs from freshly executed checks.
- Do not treat JVM tests as proof of Android persistence, lifecycle, or alarm behavior: `MockStorage.save()`
  is a no-op. Perform relevant device/emulator checks, or explicitly report that they remain unverified.

## Releases and secrets

- Follow [RELEASE.md](RELEASE.md) and use [scripts/release.ps1](scripts/release.ps1) for release work.
  Consult the repository's [serena-release skill](.agents/skills/serena-release/SKILL.md) when preparing,
  verifying, finalizing, tagging, or handing off a release.
- Use `.\scripts\release.ps1 -Verify -VersionName <major.minor>` after replacing the placeholder with
  the intended release version. The script requires `master`, matching version/changelog metadata,
  no unrelated working-tree changes, an unused release tag, and local signing/build tools.
- Expect release verification to run `test` and `assembleRelease`, copy the signed APK to `app/release/`,
  verify its signature and metadata, and report a checksum. It is not the everyday debug-build command.
- Use `-Prepare` for release metadata and changelog preparation; replace generated changelog TODOs.
  Use `-Finalize` only within requested release finalization: it creates a commit and tag. It does not
  push or upload the APK. Do not bump versions, tag, or publish as part of unrelated work.
- Keep keystores, `keystore.properties`, credentials, and local SDK configuration out of version control.
  Do not print signing secrets in logs or responses. Preserve ignored local configuration.

## Before handing off

- Review every changed file and the final diff. Do not remove unrelated user edits; preserve overlapping
  work and report it when it affects the task. Include untracked files in review because ordinary
  `git diff` and `git diff --check` do not include them.
- Check relevant failure paths and boundaries, not just the expected case. Confirm fixes with tests
  that would catch the original problem where practical.
- Report what changed, what checks ran and their results, and what could not be verified. Separate
  observed evidence from inference, and call out remaining risks or blockers without claiming success.
