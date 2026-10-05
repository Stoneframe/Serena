# Serena agent guide

## Working rules

- Follow **Inspect → Plan → Implement → Verify** for each task or iterative step, scaling each phase
  to its complexity and risk. Small changes can still carry high risk.
- Start with Git status, affected code, and relevant tests/callers. For bounded, low-risk changes, stop
  inspection once behavior, a suitable existing pattern, and verification are clear. Expand for unresolved
  coupling or risk. Prefer behavior/tests, implementation, configuration, documentation, comments, then
  assumptions; point out conflicting evidence.
- Record starting working-tree status and pre-existing edits in affected files, including untracked files,
  so the current step's changes can be reviewed separately. Preserve all pre-existing work.
- Check for nested `AGENTS.md` files when entering a subtree; read applicable root-to-target instructions,
  with the most specific governing conflicts. Repeat discovery when the target or instruction files change.
- Reuse findings from earlier steps in the conversation when still applicable. Check current affected code
  before editing; repeat broader inspection when changes, failures, or new evidence invalidate findings.
- Search for existing functionality and patterns before introducing code, helpers, abstractions, or
  dependencies. Reuse or extend suitable solutions.
- For a small step, a 1–3 sentence plan covering the change, affected files, verification, and any material
  risk is sufficient; trivial changes may use an implicit plan. For complex/high-risk work, also state
  current behavior and relevant edge cases. Create detailed execution plans only when explicitly requested
  or transferring implementation to another agent; include concrete steps, assumptions, and verification
  commands. Routine completion reports need no additional plan. Proceed with an authorized step without
  a separate plan-approval pause.
- Make the smallest sufficient conceptual change. Preserve unrelated behavior and user edits; avoid
  unrelated refactoring, renaming, formatting, abstractions, or dependency changes.
- Prefer short, focused methods, aiming for roughly 5 substantive logical lines when natural.
  Wrapped statements count once; blank and brace-only lines do not count. Extract meaningful
  responsibilities, never solely to meet a length target; readability and cohesion take precedence.
- Use `rg` and scoped searches; widen only when needed. Batch independent reads/searches and keep output
  focused. Read only applicable documentation sections; reuse unchanged instructions and findings.
- For release work, use the [serena-release skill](.agents/skills/serena-release/SKILL.md).
- Keep keystores, `keystore.properties`, credentials, and local SDK configuration out of version control
  and secrets out of logs/responses. Preserve ignored local configuration.

## Iterative feature work

- Use the feature description as context and the requested step as scope. Consider feature compatibility
  without producing a full implementation plan unless requested.
- When asked to suggest the next step, inspect and propose one cohesive, independently reviewable change;
  do not edit files or routinely run builds/test suites. Run checks when needed to resolve uncertainty
  affecting the proposal. When asked to implement it, plan, implement, and verify that step, then stop for review.
- Keep the step complete and verifiable. Do not implement later steps or add speculative scaffolding.

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

- Choose the smallest checks that can catch the likely failure: affected test classes for local domain
  behavior, a debug build for Android code/resource integration, and lint for relevant Android concerns.
  Documentation-only changes need accuracy, links, commands, and diff checks, without app builds/tests.
- Broaden verification for shared behavior, failures, unresolved integration risk, or feature completion.
  Do not defer necessary high-risk checks. Reuse successful results while code, tests, configuration,
  dependencies, and environment remain unchanged; rerun when edits or new evidence invalidate them.
  Combine compatible Gradle tasks in one invocation.
- Test relevant boundaries/failure paths and add useful regression coverage. Fix findings and rerun
  affected checks. After substantial or high-risk changes (persistence/schema, lifecycle, concurrency,
  security, release, user data), perform a separate critical review of the resulting diff, independent
  of the plan, for assumptions, scope, regressions, state/concurrency errors, and missing tests.
- Separate environment failures from code failures; do not change dependencies or signing merely
  to bypass them. Distinguish fresh checks from cached/up-to-date results and dry runs.
- JVM tests do not prove Android persistence, lifecycle, or alarms: `MockStorage.save()` is a no-op.
  Perform relevant device/emulator checks or report them as unverified.

## Before handing off

- Review each file changed in the current step against its starting state, including untracked files;
  ordinary `git diff` and `git diff --check` omit untracked files. Inspect earlier changes when they overlap
  or affect correctness; review the cumulative feature diff at completion. If no reliable starting state
  is available, review the complete affected diff. Remove unrelated changes you introduced and report
  relevant overlapping work.
- Briefly report what changed, verification/results, and limits or blockers. Distinguish observations
  from inference; do not claim completion solely from reading code.
