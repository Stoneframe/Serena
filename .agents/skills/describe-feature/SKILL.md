---
name: describe-feature
description: Create or refine and save a self-contained feature description focused on intended behavior, scope, and observable acceptance criteria. Use when defining a feature goal before implementation planning.
---

# Describe a feature

Turn a feature idea into a clear description that can serve as the goal
throughout implementation and review.

Describe what the feature must do and under which conditions. Leave
implementation choices and sequencing to later planning.

Assume the implementer has the repository and the feature description,
but no prior conversation context.

## Establish intent and scope

- Identify the user problem, intended outcome, and relevant users or actors.
- Capture explicit requirements and accepted product decisions from the
  request and available conversation.
- When refining an existing description, preserve accepted requirements
  unless the user requests a change. Surface conflicts rather than silently
  removing or weakening requirements.
- Distinguish required behavior from examples, suggestions, and assumptions.
- Keep the description within the requested feature. Do not add adjacent
  capabilities merely because they seem useful.

## Understand existing behavior

- Follow applicable repository instructions.
- Inspect relevant code, tests, configuration, and documentation when needed
  to establish current behavior, interactions, or compatibility requirements.
- Keep inspection proportional to the feature. Do not perform a broad
  architecture review or routinely run builds and test suites.
- Use targeted checks when necessary to resolve uncertainty that materially
  affects the description.
- Distinguish observed behavior, findings from code, and assumptions.
  Do not claim runtime behavior was verified through code inspection alone.
- If the user's description of current behavior conflicts with evidence,
  explain the difference and clarify the intended change where necessary.
- Use the existing system to understand context, not to dictate the proposed
  feature's design. Current limitations do not automatically become requirements.

## Resolve behavioral decisions

- Identify ambiguities that would lead to materially different user outcomes.
- Ask focused questions about consequential product decisions that cannot
  be inferred from accepted requirements.
- Group related questions and continue drafting independent parts while
  answers are pending.
- Offer behavioral options and their tradeoffs when that helps the user decide.
  Do not turn clarification into implementation design.
- Avoid asking about routine technical choices or speculative edge cases.
- Do not invent requirements, quantitative targets, or product decisions.
  Label provisional assumptions and unresolved questions explicitly.
- Do not present a description as ready to guide implementation while a
  consequential behavioral decision remains unresolved.

## Describe the intended behavior

For each relevant interaction, make clear:

- What triggers it and which conditions must hold.
- What the user or external actor provides or does.
- What result is observable.
- What information or state changes, and what must remain unchanged.
- What happens for relevant invalid inputs, unavailable capabilities,
  cancellation, or failures.

Cover boundaries and alternative paths that materially affect this feature.
Do not mechanically enumerate every possible concern.

Define terms when different interpretations would change behavior.
Replace vague requirements such as "handle errors appropriately" or
"work seamlessly" with concrete outcomes.

Describe data retention, restart behavior, permissions, timing, or other
operational conditions when they affect the required experience. State
the outcome without choosing the technical mechanism.

For example:

- Behavioral requirement: "A created task remains available after the app
  restarts."
- Implementation instruction to exclude: "Call Serena.save() from the
  broadcast receiver."

Preserve explicitly requested product details such as screen locations,
message formats, notification text, and interaction rules. These can be
behavioral requirements even when they are precise.

Exclude proposed classes, methods, internal fields, storage mechanisms,
libraries, algorithms, file changes, pseudocode, and implementation order.

If the user supplies implementation constraints, retain them in the
surrounding task context and explain that they belong in later planning.
Do not silently discard them or turn them into behavioral requirements.

## Define acceptance criteria

- Give criteria stable identifiers such as AC-1 and AC-2 so implementation
  steps and reviews can refer to them.
- State the relevant condition or action and its expected observable result.
- Cover the main outcome and the important boundaries or failure paths.
- Make completion assessable without knowing the chosen implementation.
- Ensure every material requirement is assessable through the criteria.
  Combine related rules where this remains clear.
- Avoid prescribing test frameworks, test classes, commands, or internal
  assertions. Those belong in implementation plans.
- Keep criteria consistent with the behavior section. Do not introduce new
  requirements only in acceptance criteria.

Use concrete examples where they remove ambiguity. Do not treat illustrative
example values as universal requirements unless the user intended that.

## Produce the description

Use the following structure, adapting its detail to the feature.
Omit sections that add no useful information.

### Purpose

Explain the user problem and intended outcome.

### Scope

Describe the included capabilities and explicit exclusions needed to
prevent likely misunderstandings.

### Behavior

Describe the required interactions, rules, outcomes, and relevant
alternative or failure paths.

### Compatibility

State relevant existing behavior, user data, and external contracts that
must be preserved. Distinguish intended changes from preservation requirements.

### Acceptance criteria

Provide identified, observable conditions for feature completion.

### Open questions and assumptions

Include only when needed. Distinguish unresolved decisions from provisional
assumptions and explain which affect readiness for implementation.

Keep the description self-contained. Include accepted decisions needed
to understand it without conversation history.

Keep implementation status, technical findings, step plans, and roadmaps
outside the feature description so it remains a stable statement of the goal.

## Review and finish

Before returning the description, check that:

- It reflects the user's intent without expanding scope.
- Terms and rules are consistent and do not contradict each other.
- Material interactions have clear outcomes.
- Acceptance criteria could expose an implementation that fails the goal.
- Assumptions and unanswered decisions are visible.
- Implementation instructions have not entered the description.

Write the complete feature description to a Markdown file by default,
unless the user explicitly requests a response without file changes.
Use a user-specified path; otherwise update the existing file when refining
a description. For a new description, follow the repository's documentation
conventions, defaulting to a descriptive kebab-case filename under `docs/`.
Preserve unrelated content and do not overwrite an unrelated file.

Review the written file for consistency with the accepted requirements and
check the diff, including new files. Print the complete description in the
final response so it can be read directly in the terminal or chat, and include
a link to the saved file. Briefly identify any consequential questions that
remain unresolved. For a response-only request, omit the file link.

Do not implement application changes or create an implementation plan
as part of this skill.
