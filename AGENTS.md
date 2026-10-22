# Serena agent guide

## Working rules

- Check for nested `AGENTS.md` files when entering a subtree; read applicable root-to-target instructions,
  with the most specific governing conflicts. Repeat discovery when the target or instruction files change.
- Use `rg` and scoped searches; widen only when needed. Batch independent reads/searches and keep output
  focused. Read only applicable documentation sections.
- For release work, use the [serena-release skill](.agents/skills/serena-release/SKILL.md).
- Treat keystores, `keystore.properties`, and local SDK configuration as machine-specific configuration.
  Preserve ignored local configuration.

## Project guidance

- When locating components or adding files, follow the
  [repository structure](docs/project-guide.md#repository-structure).
- For build setup, compatibility, verification commands, or reports, use the
  [build and verification guide](docs/project-guide.md#build-and-verification).
- Follow applicable required rules in [docs/code-style.md](docs/code-style.md) for new/substantially changed
  code; recommendations are guidance. Preserve surrounding legacy conventions.
- Use [docs/architecture.md](docs/architecture.md) to resolve unclear component boundaries or state ownership.
  Verify relevant dated findings against current code; its proposals do not authorize broader work.

## State invariants

- `Serena` owns a shared mutable `Container`; managers use suppliers, but selections/editors may retain
  stale references after `Serena.load()`. Queries may mutate state; returned models are not necessarily copies.
- `Revertible.save()` creates an in-memory checkpoint; `Serena.save()` persists through storage.
  Manager mutations need not persist.
- Preserve stored-data compatibility: `SharedPreferencesStorage` serializes the whole aggregate, and Gson
  fields, including private fields, may be schema. Do not rename persisted fields, alter sentinels,
  or restructure models for style.
- For saving/editing, schema, import/replacement, time, lifecycle, notification, or concurrency changes,
  follow the applicable sections of [docs/state-safety.md](docs/state-safety.md).

## Verification

- Do not create or extend tests for code in `stoneframe.serena.gui` or any of its subpackages.
  This is an exception to the global test-first requirements. Verify changes to this code with
  appropriate build, lint, or manual checks instead.
- Choose the smallest checks that can catch the likely failure: affected test classes for local domain
  behavior, a debug build for Android code/resource integration, and lint for relevant Android concerns.
- Combine compatible Gradle tasks in one invocation.
- Do not change signing merely to bypass tooling problems. Distinguish fresh checks from
  cached/up-to-date results and dry runs.
- JVM tests do not prove Android persistence, lifecycle, or alarms: `MockStorage.save()` is a no-op.
  Perform relevant device/emulator checks or report them as unverified.

## Before handing off

- Ordinary `git diff` and `git diff --check` omit untracked files. Inspect earlier changes when they overlap
  or affect correctness; review the cumulative feature diff at completion. If no reliable starting state
  is available, review the complete affected diff.
- Distinguish observations from inference; do not claim completion solely from reading code.
