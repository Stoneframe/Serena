# Serena code-style analysis

Analysis date: 2026-09-21. This report distinguishes observed conventions from enforcement and recommendations. The working guide is [code-style.md](code-style.md). This report preserves the evidence behind that guide, not a claim that every proposed rule is already followed.

## 1. Executive summary

Serena has a recognizable, largely informal code style. The strongest conventions are:

- Four-space indentation without tabs.
- Java opening braces on separate lines, including classes, methods, control flow, and multiline lambdas.
- Feature-oriented packages under `stoneframe.serena`, with Android UI code under `gui`.
- PascalCase type names, lowerCamelCase methods and ordinary fields, and UPPER_SNAKE_CASE constants.
- Explicit imports, usually grouped by origin with static imports first.
- Small getters/setters, constructor-injected dependencies, and frequent `private final` fields.
- JUnit 4 tests named `method_scenario_expectedResult`, using handwritten test doubles.
- Android resource filenames in snake_case, shared semantic colors/dimensions, and dotted style names.
- Four-space Groovy indentation with same-line opening braces.

These are observed conventions, not a formatter-enforced standard. Important exceptions include compact Java bodies, inline guard clauses, inconsistent nullability annotation placement, mixed XML ID naming, and legacy assertion imports.

Read-only scans covered 180 production Java files, 16 test/helper Java files, and 104 resource XML files. Representative contents were inspected across domain features, UI, storage, tests, and resources. No files were modified during the analysis; this document subsequently records its findings.

For compact citations below:

- `J/` means `app/src/main/java/stoneframe/serena/`.
- `T/` means `app/src/test/java/stoneframe/serena/`.
- `R/` means `app/src/main/res/`.

Line numbers refer to the working tree inspected on the analysis date. All paths are relative to the repository root, after expanding these prefixes.

## 2. Tooling and enforcement

| Mechanism | Evidence | What it establishes |
|---|---|---|
| Dedicated formatters/style linters | No Spotless, Checkstyle, PMD, Google Java Format, or equivalent configuration found in repository configuration/build scripts. | No shared automated Java formatting enforcement found. |
| EditorConfig | No `.editorconfig` found. | No repository-defined cross-editor indentation, newline, or line-length policy found. |
| Android Gradle Plugin | `build.gradle:9`; `app/build.gradle:1` | Android build/lint infrastructure is available. This does not establish enforcement of the observed Java style. |
| Custom Android lint configuration | No lint configuration/baseline or custom lint block found in inspected configuration. | No repository-specific lint policy found. |
| Local IDE XML formatting | `.idea/codeStyles/Project.xml:3–114` | Contains XML continuation indentation of four spaces and attribute arrangement rules. |
| IDE configuration selection | `.idea/codeStyles/codeStyleConfig.xml:3` | Names preferred style `Default`; the presence of `Project.xml` does not prove its rules are actively applied. |
| IDE inspections | `.idea/inspectionProfiles/Project_Default.xml:4–5` | Locally disables `FieldCanBeLocal` and `ManualMinMaxCalculation`. |
| IDE settings sharing | `.gitignore:19–24`; no tracked `.idea` files found | IDE settings are local, ignored files—not shared repository enforcement. |
| Release verification | `scripts/release.ps1:339–341` | Runs `git diff --check` for `app/build.gradle` and `CHANGELOG.md`, then Gradle `test` and `assembleRelease`. The whitespace check is narrowly scoped. |
| Local suppressions | `J/gui/notifications/Notifier.java:223`; `R/layout/activity_task_settings.xml:40,58` | Java `@SuppressLint` and XML `tools:ignore` selectively suppress inspections. |
| Documentation at inspection time | `AGENTS.md:1`; `docs/code-style.md:1`; `docs/testing.md:1` | These files were effectively blank. The working principles supplied in the conversation were followed. |

No checked-in CI style gate was found. No build, formatter, or lint task was executed for this analysis; doing so would produce files and was unnecessary for the read-only style review.

## 3. Evidence-based findings

Confidence describes confidence in the observation, not whether the recommended rule is already enforced.

| Area | Observed convention | Evidence | Confidence | Recommended rule |
|---|---|---|---|---|
| Project structure | One Android application module; feature packages and separate GUI/storage packages | `settings.gradle:1`; `J/tasks/TaskManager.java:1`; `J/gui/tasks/TaskSettingsActivity.java:1` | High | Keep new files in the existing feature/responsibility package structure. |
| File organization | Named top-level types match filenames; related helpers/listeners may be nested | `J/notes/NoteEditor.java:8,97`; `J/Editor.java:46` | High | Use one primary top-level type per file; nest tightly related helpers. |
| Type/member naming | PascalCase types, lowerCamelCase members, uppercase constants | `J/tasks/Task.java:10–14`; `J/sleep/Sleep.java:14–28` | High | Retain these naming forms for new code. |
| Naming exceptions | PascalCase persisted fields; UI type prefixes and suffixes coexist | `J/Container.java:14–30`; `J/gui/balancers/BalanceActivity.java:47–61` | High | Preserve compatibility-sensitive names; use descriptive lowerCamelCase for new fields. |
| Java indentation/braces | Four spaces, predominantly separate-line braces | `J/tasks/TaskManager.java:18–28`; `J/routines/Day.java:64–67` | High | Use four spaces and separate-line Java braces. |
| Wrapping | Four-space continuation is common; chains usually break before `.` | `J/tasks/TaskData.java:13–18`; `J/tasks/TaskManager.java:34–36` | High | Follow this wrapping pattern; treat any numeric line limit as a new decision. |
| Imports | Explicit imports and origin groups; static imports first | `J/gui/MainActivity.java:3–55`; `T/reminders/ReminderManagerTest.java:3–10` | High | Use explicit imports, separate static imports, and preserve local grouping. |
| Import exceptions | Wildcard and unsorted project imports exist | `J/storages/JsonConverter.java:10–14`; `J/Container.java:3–5` | High | Avoid new wildcard imports; sort within established groups. |
| Annotations | Standalone override annotations; nullability placement varies | `J/tasks/Task.java:69–71`; `J/tasks/TaskManager.java:60,170,203` | High | Standardize new declaration annotations above declarations and parameter annotations inline. |
| Fields/access | Private dependencies, `final` references, package-private model mutation | `J/tasks/TaskManager.java:20–27`; `J/checklists/Checklist.java:9–34` | High | Use narrow access and `final` for references that do not change. |
| Control flow | Early returns, enhanced loops, streams, and method references coexist | `J/tasks/TaskManager.java:49–55,93–102`; `J/notes/NoteEditor.java:83–94` | High | Choose the clearest form; retain early guards and readable chains. |
| Null handling | Nullable annotations, explicit checks, sentinels, and unannotated nullable returns coexist | `J/tasks/TaskManager.java:60–69`; `J/tasks/Task.java:12,44–51`; `J/routines/Day.java:49` | High | Make new null contracts explicit; preserve existing sentinel semantics. |
| Exceptions/logging | Checked propagation, runtime wrapping, validation fallbacks, sparse Android logging | `J/storages/SharedPreferencesStorage.java:43–45`; `J/gui/tasks/TaskSettingsActivity.java:94–114`; `J/gui/MyExceptionHandler.java:26` | High | Catch specific failures and preserve causes; do not invent a universal logging convention. |
| Tests | JUnit 4, descriptive underscore names, `@Before`, handwritten doubles | `T/chores/ChoreTest.java:18–49`; `T/mocks/MockTimeService.java:10–35` | High | Continue JUnit 4 and behavior-oriented names with deterministic fixtures. |
| XML formatting | Four spaces; multiline layout attributes; compact value/selector entries | `R/layout/activity_task_settings.xml:13–22`; `R/color/button_primary_background.xml:3–5` | High | Use multiline layouts and concise simple value entries. |
| Resource names | Snake_case filenames; mixed IDs; dotted PascalCase styles | `R/layout/fragment_all_chores.xml:13,22,29`; `R/values/styles.xml:51–79` | High | Preserve existing IDs and local ID conventions; use snake_case resource filenames. |
| Resource reuse | Shared semantic tokens coexist with literals | `R/values/dimens.xml:8–15`; `R/values/colors.xml:4–41`; `R/layout/activity_task_settings.xml:35` | High | Reuse shared tokens and styles where applicable. |
| Gradle | Four spaces, same-line braces, literal dependency versions, mixed quotes | `build.gradle:3–24`; `app/build.gradle:9–75` | High | Preserve Groovy DSL structure; prefer single quotes unless interpolation is needed. |
| Markdown | ATX headings, lists, fenced examples, inline code; inconsistent prose wrapping | `RELEASE.md:1–46`; `CHANGELOG.md:1–20`; `docs/architecture.md:1–24` | Medium | Use headings, blank-line-separated blocks, and language-tagged fences. |

## 4. Detailed findings

### Java

**Package and type organization — consistent in the inspected sample.**

Packages follow feature names such as `tasks`, `notes`, `routines`, and `checklists`. UI counterparts live under `gui.<feature>`, and serialization/migration code under `storages.json` and `storages.versions`.

Representative examples are `stoneframe.serena.tasks.TaskManager`, `stoneframe.serena.gui.tasks.TaskSettingsActivity`, and `stoneframe.serena.storages.versions.UpgradeScriptVersion29` (`J/tasks/TaskManager.java:1,18`; `J/gui/tasks/TaskSettingsActivity.java:1,21`; `J/storages/versions/UpgradeScriptVersion29.java:1,8`).

One primary type per file is the normal organization. Nested listener interfaces and helper classes are established exceptions: `NoteEditor.NoteEditorListener` and `Editor.PropertyUtil<TProperty>` (`J/notes/NoteEditor.java:97`; `J/Editor.java:46`).

**Names and role suffixes — common but informal.**

Types use PascalCase without an `I` prefix for interfaces: `SerenaChangedListener`, `TextChangedListener`, and `UpgradeScript`. Members and parameters use lowerCamelCase: `maximumNumberOfTasksPerDay`, `getTaskEditor`, and `noteManager`.

The recurring role vocabulary is:

- `Manager`: feature operations, such as `TaskManager`.
- `Editor`: editing operations, such as `NoteEditor`.
- `Data` and `Container`: state holders, such as `TaskData` and `Container`.
- `Listener`: event contracts or adapters, such as `NoteEditorListener` and `TextChangedListener`.
- Android role suffixes: `Activity`, `Fragment`, `Adapter`, and `Dialog`.

Evidence: `J/tasks/TaskManager.java:65,83`; `J/tasks/TaskData.java:5–18`; `J/notes/NoteEditor.java:8–17`; `J/gui/util/TextChangedListener.java:8–14`.

Callback methods mix Android-style `on...` names and domain event names: `onSerenaChanged()` versus `titleChanged()` (`J/SerenaChangedListener.java:5`; `J/notes/NoteEditor.java:99`). Preserve the contract’s vocabulary rather than renaming every callback to one pattern.

UI fields vary between `titleEditText` and `textViewName` (`J/gui/notes/EditNoteActivity.java:25`; `J/gui/balancers/BalanceActivity.java:47`). Neither prefix nor suffix placement is universal.

**Constants, generic parameters, and enums — partly consistent; enum evidence absent.**

Constants normally use UPPER_SNAKE_CASE, for example `MAXIMUM_DEADLINE` and `DEFAULT_MIN_HOURS_SLEEP_PER_DAY` (`J/tasks/Task.java:12`; `J/sleep/Sleep.java:17`). `SerenaTest.MAX_EFFORT` is uppercase but instance-final rather than static-final (`T/SerenaTest.java:44`).

Generic parameters include `T`, `TProperty`, and `Listener`; there is no single convention (`J/gui/util/CheckboxListAdapter.java:24`; `J/Editor.java:13,46`).

No Java enum declarations were found in the production/test scan. States such as `AWAKE` and `ASLEEP` use integer constants (`J/sleep/Sleep.java:14–15`). An enum naming rule would therefore be a recommendation, not a discovered convention.

**Indentation, braces, and whitespace — dominant convention with explicit exceptions.**

The representative Java form is:

```java
public void complete(Task task)
{
    if (task.isDone())
    {
        return;
    }

    task.setDone(true, timeService.getToday());
}
```

Excerpt from `J/tasks/TaskManager.java:93–100`, with the remaining method body omitted.

Four-space indentation and separate-line braces recur in tasks, routines, notes, storage, UI, and tests. Methods and logical steps are usually separated by one blank line. Fields are sometimes separated individually and sometimes grouped by purpose (`J/notes/NoteEditor.java:10–15`; `J/gui/tasks/TaskSettingsActivity.java:23–31`).

Exceptions include:

- Same-line method brace: `J/notes/NoteEditor.java:49`.
- Entire constructor on one line: `J/Editor.java:19`.
- Unbraced inline guard: `J/routines/Day.java:49`.
- Inline conditional returns: `J/sleep/Sleep.java:92–93`.

Spaces normally surround assignment/binary operators and follow commas. Cast spacing is less conventional: `(Fragment)clazz` and `(int)((...))` appear without a separating space (`J/gui/MainActivity.java:147`; `J/sleep/Sleep.java:95`). `T/mocks/TestContext.java:20` contains `0 ,0`, an isolated spacing inconsistency.

**Wrapping and line length — common but informal.**

Arguments and parameters often wrap one per line with four additional spaces:

```java
TaskData(
    String description,
    LocalDate deadline,
    LocalDate ignoreBefore,
    LocalDate completed,
    boolean isDone)
```

Source: `J/tasks/TaskData.java:13–18`.

Chains commonly break before the dot:

```java
List<Task> sortedTasks = getContainer().tasks.stream()
    .sorted(getTaskComparator())
    .collect(Collectors.toList());
```

Source: `J/tasks/TaskManager.java:34–36`; also `J/storages/json/ContainerJsonConverter.java:25–30`.

Continuation indentation is not universal: the migration registration list uses a deeper indent (`J/storages/JsonConverter.java:20–24`).

Across physical source lines:

| Scope | Lines over 100 characters | Lines over 120 | Maximum |
|---|---:|---:|---:|
| Production Java | 45 | 5 | 195 |
| Test/helper Java | 20 | 1 | 123 |

This supports “keep lines reasonably short,” but does not prove a configured 100- or 120-column limit. Long generic declarations are notable exceptions (`J/gui/routines/EditRoutineActivity.java:14`).

**Imports and annotations — common grouping, inconsistent details.**

Static imports precede ordinary imports. Ordinary imports commonly group Android, AndroidX, third-party packages, Java library packages, and project packages, with blank lines between groups (`J/gui/MainActivity.java:3–55`; `T/reminders/ReminderManagerTest.java:3–10`).

Explicit imports dominate. The scan found a wildcard in `J/storages/JsonConverter.java:14`. Alphabetical ordering is common within groups, but `J/Container.java:3–5` and `J/storages/JsonConverter.java:10–13` contradict a strict rule.

`@Override` normally appears above the declaration. Nullability appears both above declarations and within signatures:

```java
public @Nullable Integer getMaximumNumberOfTasksPerDay()
```

Source: `J/tasks/TaskManager.java:60`; compare standalone `@NonNull` at lines 203–204 and `J/tasks/Task.java:69–71`.

A single annotation-placement rule would be a useful prospective standard.

**Modifiers, member order, constructors, and access — common but informal.**

The recurring arrangement is fields, constructor, public operations, then private helpers. Modifier order follows forms such as `private static final` and `private final` (`J/sleep/Sleep.java:17–20`; `J/tasks/TaskManager.java:20–24`).

Dependencies commonly become final fields initialized through constructors. Local variables and parameters are not universally final; tests sometimes use final date fixtures (`T/chores/ChoreTest.java:29–30`).

Models frequently expose public getters and package-private constructors/setters:

```java
public String getName()
void setName(String name)
```

Declaration excerpts: `J/checklists/Checklist.java:14–26`; similarly `J/tasks/Task.java:14,24–31`.

This is not a universal encapsulation rule. `TaskData` has package-visible fields; `Container` has public fields; `TransactionType` has protected fields (`J/tasks/TaskData.java:7–11`; `J/Container.java:14–30`; `J/balancers/TransactionType.java:7–8`).

Getter/setter pairs are commonly adjacent, but no strict member sorting rule is evident. `BalanceActivity` places several lifecycle/listener methods before `onCreate` (`J/gui/balancers/BalanceActivity.java:72–232`).

**Null handling — inconsistent conventions.**

Examples include:

- Explicit `@Nullable` parameters/returns: `J/tasks/TaskManager.java:60–69`.
- Null as absence: `J/storages/SharedPreferencesStorage.java:34–38`.
- Unannotated null return: `J/routines/Day.java:47–49`.
- Sentinel dates: `J/tasks/Task.java:12,44–51`.

Recommend explicit null contracts for new APIs, without presenting annotation coverage as complete or converting existing sentinel representations as a style cleanup.

**Method size, control flow, and collections — mixed approaches.**

Small methods are frequent in models/editors. Larger UI setup methods also exist: `BalanceActivity.onCreate` starts at line 232, with the next method at line 363 (`J/gui/balancers/BalanceActivity.java`). There is no evidence of a method-length limit.

Early returns, conventional loops, enhanced loops, streams, lambdas, and method references all appear:

- Guard returns: `J/tasks/TaskManager.java:93–110`.
- Enhanced loop: `J/tasks/TaskManager.java:49–55`.
- Date loop and `continue`: `J/routines/Day.java:81–88`.
- Method references: `J/notes/NoteEditor.java:83–94`.

Collection variables commonly use interface types and diamond constructors: `List<Task> selectedTasks = new ArrayList<>()` (`J/tasks/TaskManager.java:47`).

Collection return contracts differ: an unmodifiable result in `TaskManager`, a collected list in `Day`, and the backing list in `Checklist` (`J/tasks/TaskManager.java:34–38`; `J/routines/Day.java:42–44`; `J/checklists/Checklist.java:19–21`). Do not document all getters as returning snapshots or read-only collections.

**Exceptions, logging, and comments — inconsistent or sparse.**

JSON helpers/migrations propagate checked exceptions; storage wraps them while preserving the cause; input parsing catches specific exceptions and returns a validation result (`J/storages/json/JsonUtils.java:21`; `J/storages/SharedPreferencesStorage.java:43–45`; `J/gui/tasks/TaskSettingsActivity.java:94–114`).

A broad `catch (Exception)` returning false is an exception to the narrower pattern (`J/gui/MainActivity.java:157–159`).

Logging is sparse. Examples use Android `Log.e` and `Log.d` with literal tags (`J/gui/MyExceptionHandler.java:26`; `J/gui/routines/AllRoutinesFragment.java:224,229`). There is insufficient evidence for a standard `TAG` field convention.

Javadocs are rare; the Java scan found documentation-shaped suppression comments rather than a substantial API-documentation practice (`J/gui/util/CheckboxListAdapter.java:21–23`; `T/chores/ChoreTest.java:11`). A useful new rule is to explain non-obvious contracts and reasons, without requiring comments on obvious accessors.

No TODO/FIXME markers were found in Java. Resources contain a placeholder TODO (`R/values/strings.xml:11`). No consistent marker format or ownership convention can be inferred.

### Tests

**Framework and organization — consistent foundation.**

Tests use JUnit 4 `@Test` and `@Before`, public test classes/methods, and mostly mirror production packages (`T/reminders/ReminderManagerTest.java:1–29`; `T/chores/ChoreTest.java:1–27`). JUnit 4 is declared at `app/build.gradle:73–74`.

Setup methods are named both `before()` and `setUp()`. No `@After` teardown annotations were found.

**Naming and phases — common but informal.**

The dominant form is:

```java
snooze_pendingReminder_setsTimeToNineMinutesFromNow()
```

Source: `T/reminders/ReminderManagerTest.java:29`; compare `T/chores/ChoreTest.java:27`.

Some names omit a scenario segment (`T/storages/versions/UpgradeScriptVersion29Test.java:12`) or use generic `testCase1` labels (`T/routines/DayRoutineTest.java:30`).

Arrange/Act/Assert is explicitly labeled in chore/routine tests and represented by blank lines in reminder/migration tests:

```java
// ARRANGE
// ACT
// ASSERT
```

Evidence: `T/chores/ChoreTest.java:32–49`; `T/reminders/ReminderManagerTest.java:31–36`. Requiring labels in every short test would exceed the observed convention.

Names can become stale: `SleepTest` names mention seventy/eighty while assertions expect 100 (`T/sleep/SleepTest.java:32–46`). The maintainability rule should require names to match assertions.

**Fixtures, doubles, and assertions — established approach with legacy variation.**

Handwritten doubles under `mocks` implement production interfaces. `MockTimeService` is a mutable clock; `TestContext` assembles reusable fixtures (`T/mocks/MockTimeService.java:10–35`; `T/mocks/TestContext.java:29–55`). No mocking-framework dependency was found.

Fixed dates are common, but not universal: reminder tests fix `now`, while `SleepTest.setUp()` uses `LocalDateTime.now()` (`T/reminders/ReminderManagerTest.java:20`; `T/sleep/SleepTest.java:20`).

Assertions mostly use static `org.junit.Assert` imports; routine tests retain `junit.framework.TestCase` imports (`T/reminders/ReminderManagerTest.java:3–4`; `T/routines/DayRoutineTest.java:3`). Expected-exception tests use `@Test(expected = IllegalArgumentException.class)` (`T/sleep/SleepTest.java:174,180`).

`T/routines/RoutineTest.java:11` onward contains commented-out tests. These are a legacy pattern, not an example to adopt.

### Android XML/resources

**Indentation and element formatting — common but informal.**

Layouts normally use four-space nesting and one attribute per line:

```xml
<CheckBox
    android:id="@+id/limitTasksCheckBox"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="Limit tasks per day" />
```

Source: `R/layout/activity_task_settings.xml:18–22`.

Small values and selectors stay on one line (`R/values/dimens.xml:3–15`; `R/color/button_primary_background.xml:3–5`).

Space before `/>` and XML declarations are inconsistent: compare `R/layout/fragment_all_chores.xml:1,19,26` with `R/layout/activity_task_settings.xml:1,22`.

**Attribute ordering — recognizable preference, inconsistent execution.**

A common layout order is namespaces, ID, style, width/height, placement/spacing, appearance/content, and tooling attributes. `R/layout/activity_task_settings.xml:60–67` illustrates ID before style and dimensions.

Counterexamples include `layout_below` preceding width/height and style preceding ID (`R/layout/fragment_all_chores.xml:21–25,45–49`).

The local IDE arrangement broadly supports structured ordering, but does not establish a shared, enforced attribute sequence.

**Names — consistent filenames, mixed IDs.**

Layout names describe component/use: `activity_task_settings.xml`, `fragment_all_chores.xml`, and `dialog_sleep_settings.xml`. Menu IDs include `action_remove` and `activity_storage` (`R/menu/edit_menu.xml:4`; `R/menu/main.xml:5`).

View IDs mix `filterEditText`, `all_tasks`, and `sort_by_button` even within one layout (`R/layout/fragment_all_chores.xml:13,22,29`). A universal snake_case ID rule would be a new policy, not an accurate description.

Styles use dotted PascalCase namespaces such as `Widget.Serena.Button.Primary` and `TextAppearance.Serena.Supporting` (`R/layout/activity_task_settings.xml:43,62`; `R/values/styles.xml:79`).

**Dimensions, colors, styles, and strings — reuse exists but is incomplete.**

Shared spacing names include `space_sm`, `space_md`, and `space_lg`; colors use semantic names such as `text_primary` and `status_error` (`R/values/dimens.xml:8–10`; `R/values/colors.xml:15–25`).

Dimensions use `dp`, and text sizes in styles use `sp` (`R/values/dimens.xml:3`; `R/values/styles.xml:58,64`). Qualified resources retain the same names for overrides (`R/values-w820dp/dimens.xml:5`; `R/values-v21/styles.xml:2`).

Literal dimensions and hardcoded text remain widespread in sampled layouts and menus (`R/layout/fragment_all_chores.xml:16–19,34`; `R/menu/main.xml:7`). Reusing tokens and string resources is therefore a future recommendation, not a consistently followed rule.

Legacy camelCase color aliases are explicitly retained for compatibility (`R/values/colors.xml:38–41`).

### Gradle/Groovy

**Formatting and quoting — consistent indentation, mixed quoting.**

Both build scripts use four spaces and same-line opening braces:

```groovy
buildscript {
    repositories {
        jcenter()
        google()
    }
}
```

Excerpt from `build.gradle:3–7`, with the remaining block content omitted.

Dependencies and plugin declarations use single quotes, while application metadata uses double quotes, including strings without interpolation (`app/build.gradle:1,12–19,60–74`). Prefer single quotes for new literal strings and double quotes when interpolation is needed, but identify this as normalization.

**Organization and versions — consistent with the existing small build.**

Root block order is `buildscript`, `allprojects`, then `clean` (`build.gradle:3,16,23`). App order is plugin application, signing-property setup, `android`, repositories, dependencies (`app/build.gradle:1–75`).

Production dependencies precede test dependencies, but they are not alphabetized or cleanly grouped by vendor (`app/build.gradle:60–74`). Versions are literal strings; no version catalog or central version map was found.

The Android block contains SDK/application/version metadata, signing, build types, and namespace. No explicit Java `compileOptions` policy appears in the inspected script. Do not infer a configured Java style or language policy from IDE defaults.

### Documentation

**Markdown structure — common but informal, based on a small sample.**

Existing substantial documents use ATX headings, blank lines between blocks, inline code for paths/commands, and lists (`RELEASE.md:1–46`; `CHANGELOG.md:1–20`).

Examples use language-tagged fences:

````markdown
```powershell
.\scripts\release.ps1 -VersionName 1.8 -Verify
```
````

Source: `RELEASE.md:44–46`.

`docs/architecture.md` adds tables and source references (`docs/architecture.md:18–24`). List markers and paragraph wrapping vary: release prose is manually wrapped, whereas architecture prose often uses long physical lines.

There is no demonstrated Markdown formatter or linter, and the blank style/testing documents provided no additional precedent at inspection time.

## 5. Inconsistencies and decisions

| Conflict | Practical rule for new code |
|---|---|
| Separate-line Java braces versus compact bodies | Use separate-line braces and braced control-flow bodies; leave unrelated existing code alone. |
| Mostly short lines without a configured limit | Use 120 columns as a proposed soft ceiling, with exceptions for identifiers, URLs, and awkward generic declarations. This is a new decision. |
| Four-space versus deeper continuation indentation | Use four additional spaces for newly wrapped Java expressions and parameters. |
| Explicit imports versus one wildcard and unsorted groups | Use explicit imports and sort within the surrounding file’s groups. |
| Nullability above declarations versus inline return annotations | Put new declaration annotations above declarations; keep parameter annotations inline. |
| lowerCamelCase fields versus PascalCase persisted fields | Use lowerCamelCase for new ordinary fields; preserve serialized names unless handled as a deliberate compatibility change. Gson conversion makes this more than cosmetic (`J/storages/json/ContainerJsonConverter.java:25–30`). |
| UI type prefixes versus suffixes | Match the surrounding screen; prefer descriptive names such as `titleEditText` in new screens. |
| Mutable lists versus snapshots/unmodifiable results | Make each API’s collection contract explicit; do not infer it from a getter name. |
| JUnit assertion families and setup names | Use `org.junit.Assert` and `setUp()` for new tests. |
| Explicit AAA comments versus blank-line phases | Separate phases visibly; add comments when they improve navigation. |
| Fixed clocks versus real current time | Use fixed dates or the existing controllable clock for new time-sensitive tests. |
| Mixed XML IDs | Preserve existing IDs and follow the local layout convention; do not claim one repository-wide form is established. |
| Shared resources versus hardcoded values | Reuse applicable styles/tokens; place new user-facing text in string resources. |
| Groovy single versus double quotes | Use single quotes for literals and double quotes for interpolation. |
| Sparse comments and commented-out tests | Explain non-obvious contracts; do not add dead code as comments. |

None of these findings justifies large-scale reformatting. Apply agreed rules to new or substantively changed code.

## 6. Historical candidate rules

This section records the original proposal. The current normative authority is `docs/code-style.md`.

The following combines strong observed conventions with the prospective decisions identified above:

- Use existing feature packages and place Android UI code under the corresponding `gui` package.
- Use one primary top-level type per file; use nested types for closely related listeners and helpers.
- Use PascalCase type names, lowerCamelCase ordinary members/parameters, and UPPER_SNAKE_CASE constants.
- Use established role names such as `Manager`, `Editor`, `Data`, `Container`, and `Listener`.
- Do not rename persisted fields or resource identifiers solely for style consistency.
- Use four spaces for indentation and no tabs.
- Use separate-line Java braces and braces around control-flow bodies.
- Use blank lines between methods and logical steps; keep related fields together.
- Use four additional spaces for wrapped Java arguments, parameters, and expression chains.
- Use 120 columns as a soft line-length target, allowing justified exceptions.
- Use explicit imports, static imports first, and blank lines between established import groups.
- Use annotations above declarations and inline on parameters.
- Use the narrowest practical access level and `final` for fields whose references do not change.
- Use explicit nullability and collection-mutability contracts where callers need them.
- Use clear early returns, loops, streams, and method references according to readability.
- Do not impose arbitrary method-length limits; extract named operations when they clarify a method.
- Use specific exception catches and preserve causes when wrapping exceptions.
- Use comments to explain non-obvious intent or contracts; do not retain dead code as comments.
- Use JUnit 4 tests named `method_scenario_expectedResult`, with names that match their assertions.
- Use `org.junit.Assert`, deterministic fixtures, and existing handwritten doubles.
- Use visibly separated Arrange/Act/Assert phases; use labels when helpful.
- Use four-space XML indentation and one attribute per line for multiline layout elements.
- Use snake_case resource filenames, established style namespaces, and the surrounding layout’s ID convention.
- Use shared dimensions, semantic colors, styles, and string resources where applicable.
- Use four-space Groovy indentation, same-line braces, and single-quoted literals unless interpolation is needed.
- Use Markdown headings, blank-line-separated blocks, inline code for identifiers, and language-tagged code fences.
- Do not combine functional changes with unrelated reformatting.

## 7. Limitations

- The analysis was static, read-only inspection. No tests, builds, lint tasks, or IDE formatting actions were run.
- Generated code, build outputs, `.gradle`, and generated IDE output were excluded. Only relevant local IDE style/inspection settings were inspected.
- File inventory and whitespace/import/marker scans covered the source trees; detailed semantic reading used representative samples rather than every method.
- Line statistics count physical lines, including comments and blank lines. They are descriptive, not measurements of compliance with an existing standard.
- No enums were found. Enum naming, exhaustive generic-parameter naming, and a universal logging/comment policy have insufficient evidence.
- Gradle findings necessarily come from two build scripts and one settings script. Documentation findings rely mainly on three substantial Markdown documents.
- Local IDE settings are ignored and may differ between developers; their effective activation was not verified.
- Prior verification claims in `docs/architecture.md` were not treated as tests performed during this analysis.
- Final Git inspection during the read-only analysis showed no tracked diff and the same pre-existing untracked guidance/style/testing files. This report was subsequently saved to `docs/code-style.md` at the user’s request; application code was not changed.
