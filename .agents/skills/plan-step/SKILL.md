---
name: plan-step
description: Inspect a feature's implementation, identify the next small unfinished step, and create a self-contained implementation plan suitable for a less capable implementation model. Use when given a feature description or asked to plan a bounded step.
---

# Plan the next feature step

Compare the feature description with the current implementation and plan
the next cohesive, independently reviewable step. If the user specifies a
step, check its implementation status before planning further work.

Assume the implementation model has the repository and the plan, but no
prior conversation context. Resolve consequential technical choices during
planning rather than leaving them for the implementation model.

## Establish feature context

- Identify the feature description, accepted decisions, any explicitly
  selected step or scope limits, and behavior that must remain unchanged.
- If the feature goal is missing or materially ambiguous, ask for the
  information needed to inspect it. A feature description alone is enough
  to begin; do not require the user to select a step first.

## Inspect the affected system

- Follow applicable repository instructions and consult only relevant
  project documentation.
- Inspect the affected implementation, callers, tests, and existing patterns.
  Reuse valid findings from earlier work after checking current affected code.
- Establish current behavior from evidence. Distinguish observed facts,
  implementation findings, and assumptions.
- Compare relevant feature requirements and acceptance criteria with the
  current code, callers, and tests. Identify implemented, partially
  implemented, and missing behavior, including incomplete integration.
  Treat plans, commit messages, and documentation as supporting context,
  not proof of implementation. Test presence is not proof of a passing run.
- Search for suitable existing functionality before planning new helpers,
  abstractions, or dependencies.
- Run targeted checks when needed to resolve uncertainty affecting the plan.
  Do not routinely run builds or suites for a planning request.

## Select the next step

- Choose one cohesive, independently reviewable change from the unfinished
  requirements. Consider prerequisites, existing functionality, risk, and
  how the step can be verified. Explain why it is the next useful change.
- If the user selected a step, plan its unfinished portion. If it is already
  implemented, report that finding and select the next unfinished step
  within the feature, unless an explicit scope restriction prevents it.
  Respect that restriction and explain the remaining work without expanding it.
- Do not plan to recreate existing functionality or substitute a
  verification-only plan for the next implementation step when feature
  work remains. Reuse completed behavior as a prerequisite.
- Define the selected step's boundary directly when the requirements and
  evidence support it. Ask only when a consequential product decision or
  scope conflict cannot be resolved from the available context.
- Surface dependencies on unfinished feature work without silently adding
  that work. If the step cannot be implemented coherently within its
  boundary, explain the dependency and propose the smallest necessary
  scope adjustment.
- If no implementation work remains, report the evidence and outstanding
  verification limits instead of inventing another step or claiming that
  unverified behavior has passed.

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
and acceptance criteria. Briefly identify the implemented behavior being
reused, the remaining gap, and why this step was selected. Include accepted
decisions needed by an agent without the conversation history.

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

Return the plan, or the implementation-status finding if no eligible
unfinished step remains. Do not implement application changes for a
planning-only request. Create or update a plan file only when requested.

Keep the plan within the selected step. Do not add a roadmap for the remaining
feature or require an additional approval pause when implementation is
already explicitly authorized.
