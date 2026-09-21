# Testing Serena

Use this guide to choose checks, write tests, and interpret results. Follow [AGENTS.md](../AGENTS.md)
for agent workflow and [code-style.md](code-style.md) for formatting and test conventions.
Required practices below apply to the affected behavior; items labeled **Recommendation** are guidance.
The [architecture analysis](architecture.md) explains integration risks, but its results are historical,
not evidence that the current checkout has passed a check.

## Test locations and layers

Serena has one Gradle module, `:app`. Production Java lives in
`app/src/main/java/stoneframe/serena/`, resources in `app/src/main/res/`, and JVM tests in
`app/src/test/java/stoneframe/serena/`. Mirror the production package when adding tests.
Task behavior is also tested in `SerenaTest.java`; search assertions as well as feature directories.

There is currently no checked-in `app/src/androidTest/` suite. The instrumentation runner and test
dependencies in `app/build.gradle` do not establish device coverage or a working instrumentation suite.

| Change | Relevant checks | What they establish |
| --- | --- | --- |
| Domain rules and time calculations | Focused JVM tests, then the relevant broader suite | Behavior under controlled inputs |
| JSON schema and migrations | Converter and migration-chain JVM tests | Compatibility and preservation through serialization |
| Java, XML, resources, manifest | Debug build and Android lint | Compilation, packaging, and static Android diagnostics |
| Screens, lifecycle, alarms, permissions, real storage | Emulator/device scenarios plus applicable JVM tests | Behavior involving the Android runtime |
| Documentation | Paths, commands, links, and diff review | Accuracy of the changed guidance |

A successful build does not run the app. Lint does not exercise user flows, and JVM tests using
handwritten doubles do not establish lifecycle recovery or durable Android storage behavior.

## Running checks

Run the checked-in wrapper from the repository root. Use a compatible JDK, the Android SDK required
by `app/build.gradle`, and a writable Gradle cache. These examples use PowerShell; substitute
`./gradlew` for `.\gradlew.bat` in a POSIX shell.

| Purpose | Command |
| --- | --- |
| All unit-test variants | `.\gradlew.bat :app:test --console=plain` |
| All debug JVM tests | `.\gradlew.bat :app:testDebugUnitTest --console=plain` |
| One test class | `.\gradlew.bat :app:testDebugUnitTest --tests "stoneframe.serena.reminders.ReminderManagerTest" --console=plain` |
| Debug APK | `.\gradlew.bat :app:assembleDebug --console=plain` |
| Debug Android lint | `.\gradlew.bat :app:lintDebug --console=plain` |

**Recommendation:** start with the relevant test class while developing, then broaden checks according
to the affected behavior. For a fresh debug test execution, use:

```powershell
.\gradlew.bat :app:testDebugUnitTest --rerun --console=plain
```

Use `--offline` only when the wrapper distribution and dependencies are already cached. Distinguish
setup, dependency-resolution, and assertion failures. Investigate whether a setup failure comes from
local configuration, unavailable infrastructure, or a repository change before classifying it. Report
the failed stage and supporting evidence. Do not change dependencies or signing configuration just to
bypass a local failure.
An existing incomplete `keystore.properties` can fail Gradle configuration even for debug tasks;
inspect the reported missing property names without exposing credentials.

Interpret task output before reporting success:

- `--dry-run` resolves the task graph but skips execution; it validates command selection, not tests.
- `UP-TO-DATE` reuses existing outputs; `FROM-CACHE` restores cached outputs. Neither is a fresh run.
- Fresh execution requires the test task to run. Inspect its results, including failures and skips.
  A filtered run proves only the selected tests; use the unfiltered command for the complete suite.

Reports are under `app/build/`:

- Debug test XML: `test-results/testDebugUnitTest/`.
- Debug test HTML: `reports/tests/testDebugUnitTest/index.html`.
- Release-variant test reports use `testReleaseUnitTest` in place of `testDebugUnitTest`.
- Debug lint: `reports/lint-results-debug.html` and `reports/lint-results-debug.xml`.

Confirm reports belong to the intended run and variant. Read assertion messages and stack traces;
compare lint diagnostics with existing warnings rather than treating a successful task as zero warnings.

## Writing tests and choosing fixtures

Use the existing JUnit 4 dependency, `@Test`, and `@Before` where shared setup helps. Name classes
`*Test` and methods `method_scenario_expectedResult`; keep names consistent with assertions.
Separate Arrange, Act, and Assert with blank lines, adding labels when useful. Test observable
behavior, failure cases, and boundaries. For bug fixes, choose assertions that would catch the original
failure. Do not add commented-out tests or a mocking framework solely for consistency.

**Recommendation:** use static `org.junit.Assert` imports and fixed dates for new tests, following
the style guide. Create fresh mutable fixtures per test so test order cannot change results.

| Existing fixture | Use and limitation |
| --- | --- |
| `mocks/MockTimeService` | Supplies Joda `LocalDateTime` and `LocalDate`; advance or rewind it with `setNow(...)`. It does not change Android's clock. |
| `mocks/MockStorage` | Returns a shared in-memory container initialized for routines, chores, and balances. `save()` is a no-op; other feature containers are not initialized. |
| `mocks/TestContext` | Assembles Serena with the above storage, a controlled clock, and replaceable chore-selection/effort strategies. Use `setCurrentTime(...)`; do not assume every feature is ready. |
| `mocks/MockEffortTracker` | Returns a fixed effort allowance; spending and resetting do nothing. Use a real or recording implementation to verify accounting. |

`SerenaTest` also has its own private `MockStorage`; distinguish it from the shared fixture before
copying setup. Neither mock verifies JSON serialization or real persistence. For a small feature test,
construct its container and manager directly when that makes state clearer.
See [ReminderManagerTest.java](../app/src/test/java/stoneframe/serena/reminders/ReminderManagerTest.java)
for a fixed clock and direct manager setup.

## Time-dependent behavior

Use the existing `TimeService` seam for new domain behavior, or pass explicit dates to APIs that accept
them. Advance `MockTimeService` instead of sleeping. Check constructors and factories too: a manager
with an injected clock can still call code that reads the wall clock. Existing tests that call `now()`
are not a requirement to repeat that pattern.

**Recommended scenarios**, chosen for the affected rule:

- Test immediately before, exactly at, and immediately after a deadline or occurrence boundary.
  Include midnight, daily-counter resets, and week/fortnight transitions for recurrence changes.
- Test tasks with and without deadlines, urgency cutoffs, completion, repeated completion, and undo.
- Set a known reminder time, snooze using a fixed current time, and assert the resulting timestamp
  and pending/completed state. Test delivery separately on Android.
- For sleep, use explicit start/stop times; cover invalid intervals, midnight crossings, history
  pruning, and recalculation after session edits where relevant.
- Move the clock forward and backward for rules affected by clock changes. For zone/DST conversion,
  specify the zone and expected instant: a Joda `LocalDateTime` alone has no zone. Restore any global
  timezone setting changed by a test, and validate Android alarm effects on a device.

## Persistence and migrations

`SharedPreferencesStorage` writes the whole aggregate as JSON. Model field names, including private
fields, can affect the saved schema. `Revertible.save()` only creates an in-memory checkpoint;
`Serena.save()` delegates to storage. Test the actual save boundary as well as model mutation.

For schema or migration changes, verify data preservation through the affected chain:

The checked-in migration test is isolated rather than comprehensive. Treat the procedure below as
coverage to add or extend when a schema change affects the migration chain.

1. Construct a complete representative current container with meaningful values and the appropriate
   schema version. Configure `JsonConverter` with the same relevant strategy adapters as production.
2. Serialize with `toJson`, reload with `fromJson`, and assert important values, relationships, dates,
   and polymorphic types. Compare meaning rather than JSON key order; repeat a save/load round trip.
3. Supply synthetic or sanitized older JSON fixtures to `JsonConverter.fromJson` so ordered upgrades
   run before deserialization. Include relevant intermediate versions and historical shapes.
4. Assert both the intended transformation and preservation of unrelated user data. Test the individual
   script where useful, but do not treat that as proof of compatibility across the chain.

The existing
[UpgradeScriptVersion29Test.java](../app/src/test/java/stoneframe/serena/storages/versions/UpgradeScriptVersion29Test.java)
illustrates an isolated script test. It does not cover older inputs through the full converter.

For import/replacement changes, exercise malformed JSON, missing feature containers, unknown type
discriminators, unsupported shapes, and future schema versions. Verify rejection preserves the last
usable saved value and leaves the live model usable. The current converter does not comprehensively
validate inputs or reject future versions; these are cases to investigate and protect when changing
that path, not claims of existing safeguards. See the [architecture analysis](architecture.md) for
documented migration and import gaps, and recheck the current implementation.

Use disposable data. A recording storage double can verify save calls, and converter tests can verify
JSON contents; neither demonstrates persistence after Android process loss. Validate that separately
using real `SharedPreferencesStorage` on an emulator/device.

## Android lifecycle and integration checks

Until instrumentation coverage is implemented and verified, exercise relevant scenarios manually
with a disposable installation. Record device/API level, initial state, actions, expected behavior,
and actual result. Test both successful and denied/unavailable paths where the change affects them.

| Area | Scenarios to consider |
| --- | --- |
| Editors and navigation | Save and Cancel; background/resume; recreation with a draft open; return from a child screen; duplicate listeners or delayed callbacks |
| Process recovery | Background from the affected detail screen, terminate the background process, then return; distinguish this from rotation or an ordinary restart |
| Persistence | Mutate data, leave directly from that screen, reload in a fresh process, and verify values; avoid relying on a later visit to Main to save |
| Notifications and alarms | Fresh install, permission denial/grant, reminder-only setup, cold receiver delivery, snooze/completion, and next-alarm rescheduling |
| Recovery and time changes | Reboot, change clock/timezone, and return after missed events; verify required recovery rather than assuming it exists |

`GlobalState` holds process-local selections. A passing manager test cannot prove a restored Activity
has a valid selection, nor that Android delivered an alarm or persisted a preference write.
Record the process-termination method: force-stop and ordinary background process loss are different
conditions. If device checks are unavailable, explicitly list the behavior left unverified.

## Release verification and reporting

Follow [RELEASE.md](../RELEASE.md) and [scripts/release.ps1](../scripts/release.ps1) for release work.
The script's verification mode runs unit tests, builds a signed release APK, checks signature and
package metadata, copies the artifact, and reports a checksum. Release validation also includes the
manual upgrade and feature smoke checks described in `RELEASE.md`. Ordinary JVM test success does
not establish any of those artifact or installed-app results.

When handing off a change, report the commands and test scope, execution versus cached/dry-run status,
failures or skips, and device scenarios exercised. Separate observed results from assumptions and
state what could not be verified. Do not present historical reports as a fresh validation run.
