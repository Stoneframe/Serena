---
name: plan-step
description: Create a self-contained implementation plan for a selected small feature step, suitable for a less capable implementation model. Use when asked to plan that bounded step.
---

# Plan a selected step

Turn a selected subset of a feature into a concrete implementation plan.
Use the feature description as context; keep the plan within the selected step.

Assume the implementation model has the repository and the plan, but no
prior conversation context. Resolve consequential technical choices during
planning rather than leaving them for the implementation model.

## Establish scope

- Identify the selected step, relevant feature requirements, accepted
  decisions, and behavior that must remain unchanged.
- If the selected step is missing or materially ambiguous, establish its
  boundary with the user before producing the implementation plan.
- Preserve the selected scope. Surface dependencies on unfinished feature
  work without silently adding that work.
- If the step cannot be implemented coherently within its boundary, explain
  the dependency and propose the smallest necessary scope adjustment.

## Inspect the affected system

- Follow applicable repository instructions and consult only relevant
  project documentation.
- Inspect the affected implementation, callers, tests, and existing patterns.
  Reuse valid findings from earlier work after checking current affected code.
- Establish current behavior from evidence. Distinguish observed facts,
  implementation findings, and assumptions.
- Search for suitable existing functionality before planning new helpers,
  abstractions, or dependencies.
- Run targeted checks when needed to resolve uncertainty affecting the plan.
  Do not routinely run builds or suites for a planning request.

## Design for the implementation model

- Choose one approach compatible with the existing system and selected scope.
  Explain consequential choices briefly.
- Name exact file paths and relevant classes, methods, resources, or
  configuration entries. Mark proposed new files and symbols as new.
- Give ordered, concrete actions with clear dependencies and expected results.
  Avoid instructions such as “handle appropriately” or “update as needed.”
- Specify relevant contracts, state transitions, failure behavior, and
  compatibility constraints.
- Identify existing functionality to reuse and explain how it fits the change.
- Include pseudocode, signatures, or small examples where they materially
  reduce ambiguity. Avoid writing a speculative full implementation.
- Explain relevant boundaries and edge cases with concrete inputs and
  expected behavior.
- Resolve technical uncertainty through inspection where possible. Clearly
  identify any remaining product decision requiring user input.
- Avoid unrelated cleanup, speculative scaffolding, and changes intended
  only to make later feature steps easier.

## Produce the plan

Use the following structure, scaling detail to the selected step.

### Objective and context

Describe the selected behavior, relevant feature context, current behavior,
and acceptance criteria. Include accepted decisions needed by an agent
without the conversation history.

### Scope and constraints

State what this step changes, what it preserves, and any important
dependencies or repository constraints.

### Affected files and existing functionality

List each affected file, its role in the step, and relevant symbols or
patterns to reuse. Provide precise code locations using symbol names;
do not rely solely on line numbers.

### Implementation actions

Provide an ordered sequence. For each action, identify:

- The file and symbol to change or create.
- The concrete change and chosen approach.
- Relevant contracts, boundaries, and failure behavior.
- Any prerequisite action or expected intermediate result.

Keep related changes together so the model can understand their purpose.

### Verification and completion

Specify:

- Relevant tests to add or update.
- Concrete scenarios, test inputs, and expected results.
- Repository-compatible verification commands and what they establish.
- Any manual checks or environment limitations.
- The observable conditions that make this step complete.

Use meaningful verification that could expose an incorrect implementation.
Do not present proposed checks as already executed or passing.

### Unresolved decisions

Include this section only when necessary. Distinguish blocking decisions
from assumptions that can safely be stated in the plan.

Do not present a plan as ready for implementation while a consequential
choice remains unresolved.

## Finish

Return the plan without implementing application changes for a planning-only
request. Create or update a plan file only when requested.

Keep the plan within the selected step. Do not add a roadmap for the remaining
feature or require an additional approval pause when implementation is
already explicitly authorized.
