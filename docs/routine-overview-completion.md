# Complete procedures from Routine Overview

## Purpose

Allow users to complete procedures from Routine Overview, including future occurrences, after
explicitly confirming the action.

## Scope

Apply to procedure rows in Routine Overview, opened from the Routine fragment, for daily, weekly,
and fortnightly routines.

## Behavior

- Each row represents a procedure occurrence on the displayed date.
- Tapping an uncompleted occurrence opens a confirmation popup asking whether to mark it as done.
  The popup identifies the selected procedure and explains that earlier uncompleted occurrences
  in the same routine will also be completed.
- Opening the popup changes no completion status.
- Confirming marks the selected occurrence and all earlier uncompleted occurrences in its routine
  as completed, including occurrences on earlier dates.
- Completion is allowed for past occurrences, occurrences later today, and occurrences on future dates.
- Declining or dismissing the popup leaves every completion status unchanged.
- Later occurrences in the selected routine and all occurrences in other routines retain their status.
- Tapping an already completed occurrence has no effect.
- After confirmation, the overview immediately displays the updated completion status using its
  existing completed-row appearance, while keeping the displayed date unchanged.
- Completion persists after leaving the overview and restarting the app. Completed occurrences
  do not reappear as pending when they become due.

## Compatibility

Preserve routine definitions, recurrence schedules, date navigation, and existing saved data.
Retain the existing rule that occurrences in the same routine at the same scheduled date and time
share completion status.

## Acceptance criteria

- **AC-1:** Tapping an uncompleted row opens a confirmation popup identifying the occurrence and
  explaining the effect on earlier occurrences. No status changes before confirmation.
- **AC-2:** Confirming completes the selected occurrence, whether it is past, later today, or on a
  future date, using the displayed date.
- **AC-3:** Confirmation also completes every earlier uncompleted occurrence in that routine,
  including across date and recurrence boundaries.
- **AC-4:** Occurrences later than the selected occurrence remain unchanged. Occurrences in the same
  routine at exactly its scheduled date and time are completed together.
- **AC-5:** Other routines remain unaffected, including those containing procedures with identical
  descriptions and times.
- **AC-6:** Declining or dismissing the popup leaves all completion statuses unchanged.
- **AC-7:** Tapping an already completed occurrence leaves all completion statuses unchanged.
- **AC-8:** Confirmed changes appear immediately without changing the displayed date and persist
  after navigating away or restarting the app.
- **AC-9:** An occurrence completed in advance does not reappear as pending when it becomes due.
