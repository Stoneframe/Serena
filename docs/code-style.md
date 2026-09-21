# Serena code style

This is the working style guide for new and substantially changed code.
Use it alongside nearby implementation patterns; do not reformat unrelated code.
See the [code-style analysis](code-style-analysis.md) for evidence, confidence levels, and historical exceptions.

Most rules reflect common repository conventions. Rules labeled **Recommendation** resolve inconsistent
practice or add guidance that was not previously enforced; apply them to new code without bulk cleanup.
No repository-wide Java formatter or style linter was found in the 2026-09-21 inspection.
Android lint is available through the build, but it does not enforce this guide.
Local `.idea/` settings are ignored by Git. Do not assume IDE defaults or an external formatter define Serena's style.

## Project and package structure

- Use the existing single `app` module and the `stoneframe.serena` package tree.
- Use feature packages such as `tasks`, `notes`, and `routines` for feature code, and `gui.<feature>` for Android screens.
- Use `storages.json` for JSON conversion and `storages.versions` for versioned migrations.
- Use `app/src/main/java` for production Java, `app/src/test/java` for unit tests, and `app/src/main/res` for resources.
- Do not introduce package moves, module splits, or new abstractions merely to satisfy style preferences.

## Java naming and file organization

- Use one primary top-level type per file, with the filename matching that type.
- Use nested types for tightly related listener contracts and helpers.
- Use lowercase package names, PascalCase type names, and lowerCamelCase methods, fields, parameters, and local variables.
- Use UPPER_SNAKE_CASE for constants; do not treat every final reference as a constant.
- Use role suffixes where appropriate: `Manager`, `Editor`, `Data`, `Container`, `Listener`, `Activity`, `Fragment`, and `Adapter`.
- Use descriptive verbs for operations and existing `get...`, `set...`, `is...`, and `has...` accessor forms.
- Use the callback contract's vocabulary, such as `onSerenaChanged()` or `titleChanged()`; neither prefix is universal.
- Use the surrounding screen's UI field naming. **Recommendation:** prefer names such as `titleEditText` in new screens.
- Do not add an `I` prefix to interfaces or rename generic parameters solely for uniformity.
  The repository uses both short and descriptive generic names; no enum naming convention was established.

## Indentation, braces, whitespace, and wrapping

- Use four spaces for indentation and no tabs.
- Use opening braces on separate lines for Java types, methods, control-flow blocks, and multiline lambdas.
- Use a space after control-flow keywords, around binary and assignment operators, and after commas.
- Use one blank line between methods and between logical steps; group closely related fields.
- Use four additional spaces for wrapped arguments, parameters, and expression chains.
- Use one argument or parameter per line when wrapping clarifies a long call or declaration.
- Use a leading dot on each continued method-chain line.
- **Recommendation:** use braces even for single-statement guards; compact legacy bodies are not a required pattern.
- **Recommendation:** use 120 columns as a soft Java line-length target, allowing exceptions for long identifiers,
  URLs, or declarations that become less readable when wrapped. No numeric limit was configured previously.
- Do not apply Java brace placement to Groovy or rewrite surrounding whitespace solely for consistency.

```java
if (task.isDone())
{
    return;
}

List<Task> sortedTasks = tasks.stream()
    .sorted(getTaskComparator())
    .collect(Collectors.toList());
```

## Imports and annotations

- Use explicit imports; do not add wildcard imports.
- Use static imports first, separated from ordinary imports by a blank line.
- Use the file's existing import groups. Common groups are Android, AndroidX, third-party libraries,
  Java libraries, and project packages; keep blank lines between groups.
- **Recommendation:** sort imports alphabetically within those groups.
- Use `@Override` above overridden method declarations.
- **Recommendation:** place declaration annotations, including AndroidX nullability annotations, above declarations;
  keep parameter annotations inline. Existing return annotations also appear inside signatures.
- Do not move annotations that intentionally describe a nested type use, or broaden inspection suppressions for convenience.

## Modifiers, fields, constructors, access, and nullability

- Use modifier order such as `private static final` and `private final`.
- Use fields before constructors, then operations and helpers where practical; keep related accessors together.
  Do not reorder entire existing classes to enforce a rigid member sequence.
- Use constructor initialization for dependencies in ordinary classes, and use `final` for fields whose references do not change.
- Android framework components may initialize lifecycle-bound state in `onCreate`, `onStart`, or another appropriate lifecycle callback.
- Do not require `final` on every local variable or parameter; existing practice varies.
- Use the narrowest access needed. Preserve established package-private model constructors and mutation methods.
- Use getters/setters where they fit the existing model/editor pattern; do not mechanically encapsulate every data field.
- **Recommendation:** make null contracts explicit on new APIs using existing AndroidX annotations and suitable validation.
- Do not infer non-nullness from a missing annotation or replace existing null/sentinel representations as a style change.

## Methods, control flow, collections, exceptions, logging, and comments

- Use focused methods and extract named operations when they clarify intent; do not impose arbitrary method-length limits.
- Use early returns for clear guard conditions and choose loops, streams, lambdas, or method references for readability.
- Use collection interface types such as `List<Task>` where appropriate, with diamond constructors such as `new ArrayList<>()`.
- **Recommendation:** make collection return contracts clear when callers need to know whether a result is live,
  copied, or unmodifiable. Do not silently change an existing contract during cleanup.
- **Recommendation:** catch specific exceptions and preserve the cause when wrapping them.
  Use validation fallbacks only where that is the intended contract; do not copy broad silent catches.
- In Android-facing code, use the existing Android `Log` calls when logging is needed.
- Keep domain packages free of Android logging unless an existing API already requires that dependency.
- There is no repository-wide mandatory logger or `TAG` field convention.
- **Recommendation:** use comments or Javadocs for non-obvious intent, nullability, compatibility, and API contracts.
  Do not add boilerplate comments to obvious accessors or retain dead code as comments.
- Do not treat placeholder TODOs or suppression comments as documentation templates; no TODO/FIXME format was established.

## Unit tests, fixtures, assertions, and naming

- Use JUnit 4 and mirror the relevant production package under `app/src/test/java`.
- Use descriptive `*Test` classes and `method_scenario_expectedResult` method names that agree with the assertions.
- Use `@Before` for shared per-test setup. **Recommendation:** name new setup methods `setUp()`.
- **Recommendation:** use static `org.junit.Assert` imports for new assertions rather than legacy `junit.framework.TestCase`.
- Use visible Arrange/Act/Assert phases, separated by blank lines; add phase comments when they aid navigation.
- Use existing handwritten doubles such as `MockTimeService` and reusable `TestContext` fixtures where suitable.
- **Recommendation:** use fixed dates or controllable clocks for new time-sensitive tests instead of the current wall clock.
- Use JUnit 4's existing expected-exception pattern where only the exception type matters.
- Do not introduce a mocking framework, redundant teardown, or commented-out tests solely for stylistic symmetry.

## Android XML and resource naming/formatting

- Use four-space nesting and one attribute per line for multiline layout elements.
- Use compact single-line entries for simple values and selectors.
- **Recommendation:** order layout attributes as namespaces, ID, style, width/height, placement/spacing,
  appearance/content, then tooling attributes; use a space before `/>`. Existing order and spacing vary.
- Use snake_case resource filenames, with descriptive prefixes such as `activity_`, `fragment_`, and `dialog_`.
- **Recommendation:** use descriptive lowerCamelCase IDs for new views.
- Preserve existing IDs. When extending a legacy layout with mixed naming, follow the convention used by nearby related views.
- Use dotted style names in established families such as `Widget.Serena.*` and `TextAppearance.Serena.*`.
- Use semantic color/dimension names and existing tokens such as `text_primary`, `status_error`, and `space_md`.
- Use `dp` for dimensions and `sp` for text sizes; keep qualified overrides under the same resource names.
- **Recommendation:** reuse applicable styles and shared values, and put new user-facing text in string resources.
  Existing hardcoded strings and dimensions are not a requirement to repeat them.
- Do not rename IDs, remove compatibility color aliases, or reorder selector states as a formatting change.

## Gradle/Groovy formatting and dependencies

- Use four-space indentation and same-line opening braces in Groovy.
- **Recommendation:** use single quotes for literal strings and double quotes when interpolation is needed.
- Use the existing root order: `buildscript`, `allprojects`, then `clean`.
- Use the existing app order: plugin application, signing-property setup, `android`, repositories, then dependencies.
- Use production dependencies before test dependencies and the existing literal version declaration approach.
- Do not alphabetize the entire dependency list, introduce a version catalog, or change dependency scopes as style cleanup.
- Do not infer Java language settings from IDE defaults; inspect build configuration before using new language features.

## Markdown/documentation conventions

- Use ATX headings, short paragraphs, and blank lines before and after lists, tables, and code fences.
- Use inline code for paths and identifiers, and language-tagged fences for code or command examples.
- Use relative links between repository documents; keep detailed evidence and line citations in the analysis report.
- Do not force a prose wrapping width that has not been established or duplicate the analysis in everyday guidance.

## Legacy exceptions and compatibility cautions

- Do not rename PascalCase persisted fields such as `Container.Version` or `Container.TaskContainer` for style.
  Gson uses model fields; even private-field changes can affect existing serialized data.
- Use deliberate compatibility handling and relevant verification when a task actually requires schema or identifier changes.
- Do not convert integer states to enums, alter sentinel dates, or change collection ownership solely to modernize style.
- Use this guide for new and substantially changed code while preserving unrelated legacy formatting and behavior.
- Do not run repository-wide reformatting. Review the final diff for unrelated edits and verify the actual change.
