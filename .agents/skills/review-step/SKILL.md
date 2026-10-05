---
name: review-step
description: Independently review a completed implementation step, identify defects and worthwhile structural improvements, and produce self-contained correction or refactoring plans suitable for a less capable implementation model. Use when asked to review changes.
---

# Review an implemented step

Review the change from the perspective of a developer who did not implement it.

Determine whether the resulting behavior satisfies the requirement, preserves
relevant existing behavior, and has an appropriate code structure.

Report actionable findings and produce concrete implementation plans where
needed. For a review-only request, leave project files unchanged.

## Establish the review scope

- Read the requested step, relevant feature requirements, acceptance criteria,
  and applicable instructions.
- Identify the review target: working-tree changes, a commit, or a commit range.
  Check Git status and include relevant staged, unstaged, and untracked files.
- Distinguish the selected change from unrelated or pre-existing work.
  Use an available starting snapshot or baseline where possible.
- If scope or baseline ambiguity could materially affect the findings,
  establish the boundary with the user. Continue independent inspection
  where useful.
- Review the selected step. Review the cumulative feature change when requested,
  without assuming unfinished future steps should already be implemented.

## Adopt an independent perspective

- Treat the requirement as the intended outcome and the implementation as
  a candidate solution.
- Inspect current code and tests before accepting the implementer's explanation
  or reported conclusions.
- Use the original plan to understand context and intended scope, but assess
  its assumptions and approach independently.
- Do not assume that matching the plan proves correctness or that reported
  passing checks establish all relevant behavior.
- Reuse factual inspection results after validating their continued relevance.
  Reassess consequential conclusions rather than inheriting them.
- A fresh conversation is preferable when practical. In the implementation
  conversation, deliberately evaluate the result as another developer's work.
- Do not require another agent or delegate review unless requested.

## Inspect the affected system

- Read applicable repository instructions and relevant documentation.
  Verify dated documentation findings against current code.
- Inspect every file in the review target, including new files.
- Read complete affected methods and enough surrounding code to understand
  their contracts and responsibilities.
- Trace relevant callers, dependencies, state transitions, and failure handling.
  Follow the behavior beyond the changed lines where necessary.
- Inspect relevant tests, fixtures, configuration, and integration boundaries.
- Establish the previous behavior when needed to identify regressions.
- Search for existing functionality before recommending new helpers,
  abstractions, or dependencies.
- Expand inspection when evidence reveals coupling, uncertainty, or risk.
  Avoid turning a bounded review into an unrelated repository audit.

## Assess correctness and completeness

Check the concerns relevant to the actual change:

- Required behavior and acceptance criteria.
- Regressions outside the intended behavior change.
- Boundary inputs, invalid inputs, and failure paths.
- State ownership, mutation, stale references, and operation ordering.
- Persistence, schema, and stored-data compatibility.
- Lifecycle, concurrency, resource cleanup, and recovery.
- Security and handling of sensitive data.
- Integration with affected callers and existing contracts.
- Unnecessary scope expansion or incomplete implementation.

Consider the most plausible ways the change could fail. Use this to guide
inspection rather than mechanically producing a fixed number of concerns.

Distinguish introduced defects from pre-existing problems. Report a pre-existing
problem when it materially affects the selected change, identifying its origin.

## Assess code structure and style

- Read applicable style guidance identified by global or repository instructions.
  Do not assume a particular project path applies everywhere.
- Distinguish required rules from recommendations and existing legacy exceptions.
- Flag relevant violations introduced by the change. Preserve unrelated legacy
  formatting and conventions.
- Assess responsibility boundaries, cohesion, duplication, coupling,
  abstraction choices, control flow, state ownership, and testability.
- Suggest refactoring when it produces a concrete improvement supported by
  the inspected code.
- Explain the current structural problem, proposed structure, expected benefit,
  affected scope, and behavior that must remain unchanged.
- Do not recommend abstractions solely for hypothetical future needs.
- Do not treat personal preferences or method-length targets as sufficient
  reasons for refactoring.
- Identify improvements requiring broader work, but keep them separate from
  corrections needed for the selected step.

Classify findings as:

- Required corrections: defects, regressions, or violated requirements.
- Recommended refactoring: structural improvements with a clear benefit.
- Optional improvements: useful changes that can reasonably wait.

Refactoring necessary to correct a defect safely belongs in the correction plan.
Independently beneficial refactoring belongs in a separate proposal and plan.

## Verify consequential concerns

- Run targeted tests, builds, static analysis, or reproductions when they can
  resolve meaningful uncertainty.
- Inspect whether tests exercise relevant failures and boundaries, rather than
  merely reproducing the implementation's logic.
- Do not treat missing tests alone as proof of incorrect behavior. Explain the
  concrete behavior left unverified and the useful coverage to add.
- Reuse trustworthy verification results while their relevant inputs remain
  unchanged. Rerun checks when results are unavailable, inadequate, or invalidated.
- Distinguish newly executed checks from cached results and the implementer's
  reported results.
- Separate environment failures from failures caused by the change.
- For review-only work, do not edit project code, tests, configuration, or
  dependencies to make checks pass.
- If reproducing a concern requires a project edit or unavailable environment,
  explain the limitation and provide a concrete verification scenario.

## Report actionable findings

Lead with findings, ordered by severity and practical impact.

For each required correction, provide:

- A concise title and priority.
- The affected file and precise code location.
- The input, condition, or sequence that triggers the problem.
- The incorrect behavior and its consequence.
- Supporting evidence and any material uncertainty.
- A concise correction direction.

Use critical priority for urgent failures such as likely data loss or a serious
security exposure; high for major broken behavior; medium for bounded defects;
and low for minor required corrections.

Keep structural recommendations and optional improvements separate from defects.
Give recommendations enough evidence and scope detail for the user to assess them.

Do not invent findings, impose a findings quota, or present speculative concerns
as confirmed defects. Report unresolved consequential concerns separately.

If no actionable defects are found, say so without implying exhaustive correctness.

## Produce implementation plans

Produce a self-contained correction plan when required corrections exist.

Choose a correction approach compatible with the requirement and current system.
Resolve technical choices through inspection rather than handing the implementer
several unresolved alternatives.

Assume the implementation model has the repository and the plan, but no prior
conversation context or review report. Include the necessary evidence, decisions,
and contracts in the plan itself.

Group related findings into one coherent correction step. If corrections require
multiple independent steps, explain the grouping and provide a detailed plan for
the first step, prioritizing severity and dependencies.

For a worthwhile independent refactoring, provide a separate bounded plan.
Label it as recommended work; do not silently incorporate it into required fixes.

Use the same plan structure as `plan-step`, without requiring that skill to run.

### Objective and context

Describe:

- The defect or structural problem being addressed.
- Current and intended behavior.
- Relevant requirements and accepted decisions.
- Observable acceptance criteria.

### Scope and constraints

State:

- Which findings this step addresses.
- What behavior and contracts must remain unchanged.
- Important compatibility requirements and dependencies.
- Whether the work is a required correction or recommended refactoring.

Keep the step independently reviewable. Avoid unrelated cleanup or future work.

### Affected files and existing functionality

Name exact paths and relevant classes, methods, resources, or configuration.

Mark proposed files and symbols as new. Identify suitable existing functionality
to reuse and explain how it fits the correction.

Use symbol names as well as code locations; do not rely solely on line numbers.

### Implementation actions

Provide ordered, concrete actions.

For each action, specify:

- The file and symbol to change or create.
- The chosen change and its purpose.
- Relevant contracts, state transitions, and failure behavior.
- Dependencies and expected intermediate results.

Include signatures, pseudocode, or small examples where they materially reduce
ambiguity. Avoid vague instructions such as “fix state handling” or “refactor
as needed.”

Do not write a speculative full implementation or leave consequential technical
decisions for the less capable model to discover.

### Verification and completion

Specify:

- Tests to add or update and the relevant test files.
- Concrete triggering scenarios, inputs, and expected results.
- For defects, how regression coverage would expose the original failure.
- For refactoring, how externally observable behavior will be preserved.
- Repository-compatible commands and what they establish.
- Necessary manual checks and known environment limits.
- Observable conditions for completion.

Distinguish proposed verification from checks already performed during review.

### Unresolved decisions

Include this section only when necessary.

Separate blocking product or scope decisions from safe, explicit assumptions.
Do not describe a plan as ready to implement while a consequential choice
remains unresolved.

## Finish

Return:

1. Prioritized required corrections.
2. Recommended refactoring and optional improvements, when present.
3. Correction and separate refactoring plans, when needed.
4. Review scope, verification results, and material limits.

Keep routine summaries concise. Give findings and implementation plans enough
detail to stand alone.

Stop when the affected behavior and relevant dependencies have been inspected,
consequential concerns have been resolved or identified as unverified, and
findings and plans are supported by evidence.

Do not apply fixes, expand the feature, or continue polishing as part of a
review-only request. The resulting plans can be passed to `implement-step`.
