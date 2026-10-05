---
name: implement-step
description: Implement a provided plan for a bounded feature step, follow documented code style, and run targeted verification before handing the diff to the user for review.
---

# Implement a planned step

Execute the supplied implementation plan within its selected scope.
Prepare a complete, verified change for user review.

For small, low-risk changes, keep implementer review minimal: perform one
brief diff check after verification. Leave detailed code review to the user.

## Confirm the starting point

- Read the supplied plan and applicable repository instructions.
- Check Git status and record pre-existing changes in affected files,
  including untracked files, so this step can be reviewed separately.
- Read the affected code and relevant tests. Confirm that the plan's named
  symbols, dependencies, and assumptions still match the repository.
- Reuse established planning findings where still valid. Expand inspection
  only to resolve uncertainty affecting implementation or verification.
- If no usable plan is available, request it or identify the missing
  information. Do not silently create a feature-wide implementation plan.

## Follow documented code style

- Read the applicable sections of `docs/code-style.md`, resolving the path
  from the repository root. Follow any more specific applicable instructions.
- Apply required style rules to new and substantially changed code.
  Treat explicitly labeled recommendations as guidance.
- Reuse existing patterns where they agree with the documented requirements.
- Preserve unrelated legacy formatting, names, and behavior.
- Apply style while writing the change. Do not introduce a separate
  style-review pass or run repository-wide formatting.

## Implement the plan

- Follow the plan's chosen approach and ordered actions. Reuse the identified
  functionality and preserve its specified contracts and boundaries.
- Resolve routine implementation details directly, using documented style
  and suitable existing patterns.
- Keep changes limited to the selected step. Avoid unrelated cleanup,
  speculative scaffolding, dependency changes, or work for later steps.
- Correct clear local defects within the authorized scope. Report material
  departures from the plan and their reasons.
- If a consequential discrepancy requires a product decision or broader
  scope, explain it and request the necessary decision. Continue independent
  authorized work where useful.
- Do not follow an instruction that conflicts with current code, data
  compatibility, or applicable safeguards without resolving the conflict.

## Verify the change

- Run the plan's relevant verification and any additional checks required
  by the actual change or repository instructions.
- Verify acceptance criteria, relevant boundaries, and failure behavior.
  Add or update meaningful tests where the plan or discovered behavior
  requires them.
- Fix failures introduced by the change and rerun affected checks.
  Distinguish unrelated failures and environment problems from regressions.
- Reuse successful results while their relevant inputs remain unchanged.
  Broaden checks only when shared behavior, failures, or unresolved risk
  justify it.
- Do not reduce behavioral verification because the user will review the code.
- Report unavailable checks accurately; do not claim unverified behavior
  works or describe cached results as a fresh execution.

## Perform a brief diff check

For a small, low-risk step, inspect the changes once against the starting
state to confirm:

- The intended files and planned actions are present.
- Pre-existing work is preserved.
- No accidental edits, obvious errors, or obvious violations of documented
  style were introduced.
- No unrelated changes or unfinished implementation remain.

Include new and untracked files. Use the repository's whitespace check
where applicable.

Do not perform a separate systematic code review, repeatedly reread the diff,
or delegate review unless requested.

Small size alone does not establish low risk. For substantial or high-risk
changes, follow the additional safeguards and critical-review requirements
in the applicable repository instructions.

If verification or the diff check reveals a defect, fix it and repeat only
the checks affected by that correction.

## Hand off

Stop when the selected step is implemented, relevant verification is complete,
and the brief diff check is finished.

Briefly report:

- What behavior changed.
- Which checks ran and their results.
- Material deviations, verification limits, or concrete concerns for review.

Leave detailed review of small, low-risk changes to the user. Do not continue
polishing, refactoring, or expanding the feature without a concrete reason
within the selected scope.
