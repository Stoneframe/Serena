# Today Fragment chore-list investigation

## 1. Executive summary

There are confirmed timing and state-ordering defects in the Today chore list. The most serious are:

- **Skip/Postpone can affect the wrong chore or throw an exception** because the dialog retains a row position while the list can change.
- **Skip/Postpone do not cancel an already queued completion**, so that completion can subsequently spend effort and overwrite postponement.
- **Completion order changes effort accounting.** Pausing the screen can also change completion order because pending operations are flushed from a map.
- Duplicate descriptions produce inconsistent checked-state and pending-operation identity.

I freshly ran **64 existing tests: all passed, none skipped**. Additional in-memory probes against the compiled production classes reproduced the underlying failures. No Android UI scenarios were executed.

## 2. Complete table of user operations

“Refresh adapter” below means notifying it to redraw; its supplier recomputes the list rather than retaining a stable snapshot.

| User operation | Event flow and resulting behavior |
|---|---|
| Open Today | Creates the adapter with `getTodaysChores`; start/resume refresh it. Eligibility, ordering, and remaining effort determine the rows. Reading effort can reset its daily balance. |
| Tap an unchecked chore | Resolves the current item at the position, checks it visually, refreshes the adapter, and schedules completion after 2,000 ms. No completion or persistence yet. |
| Wait for completion | Removes the pending entry, calls `complete`, calculates/spends effort, reschedules the chore, unchecks/refreshes the adapter, then saves. |
| Tap the same chore while pending | Unchecks/refreshes it, removes the pending entry, and cancels its runnable. No domain mutation or save. |
| Rapidly tap the same chore repeatedly | Alternates between pending and cancelled. A new pending operation gets a new delay. |
| Tap another chore while one is pending | Creates another independent pending operation. Neither reserves effort when tapped. |
| Long-press a chore | Opens Skip/Postpone/Cancel. Captures the **position**, not the selected chore. Existing completion timers continue. |
| Choose Skip | Resolves that position against a newly computed list, reschedules the resolved chore without spending effort, refreshes, then saves. Does not cancel pending completion. |
| Choose Postpone | Resolves the position again, postpones the resolved chore until the manager’s tomorrow, refreshes, then saves. Does not cancel pending completion. |
| Cancel the long-press dialog | Closes it without a chore mutation. Any previously queued completion remains queued. |
| Press Refresh | Opens confirmation; opening it does not reset effort or cancel timers. |
| Confirm Refresh | Resets the effort allowance using `LocalDate.now()`, saves, then refreshes the adapter. Does not reset chore recurrence or cancel pending completions. |
| Reject/dismiss Refresh | No refresh mutation. Existing pending operations continue. |
| Scroll the list | Rebinds rows through the dynamic supplier. Checked-state uses equality comparisons. No explicit save, although queries can reset daily effort. |
| Navigate away, press Back, background, or otherwise pause | `onPause` immediately executes all pending completions and removes their scheduled callbacks. `onStop` repeats the flush, normally finding nothing. |
| Return after navigation/backgrounding | Recreates or refreshes the list from current state. Completed operations have no restored undo window. |
| Return after recreation/process loss | Normal lifecycle teardown flushes pending work. Abrupt loss before completion loses its transient checked/pending state; persisted domain data is reloaded through startup. |
| Add/edit/enable/disable/delete elsewhere | Editor Save/Delete persists changes; returning to Today recomputes eligibility/order. Normal navigation has already flushed Today’s pending completions. |
| Cancel an external chore edit | Reverts the editor checkpoint; returning recomputes the list. Cancel itself does not call durable save. |
| Change effort settings elsewhere | Saves weekday allowances and updates remaining effort through tracker setters; returning recalculates Today’s selection. |
| Replace stored data through Storage | Replaces Serena’s container. Managers follow the replacement, but previously captured objects do not automatically rebind. Normal Activity navigation flushes pending completions first. |
| Interact when empty or while rows change | Empty lists have no row actions; Refresh remains available. Changed lists can invalidate earlier positions or visually replace the item under a subsequent tap. |

Today has no direct chore Add/Edit/Delete, filter, or sort control. Those operations belong to the separate Chores screen.

## 3. Timing and state-transition analysis

### Tap, undo, and delayed completion

The ordinary sequence is:

`Unchecked → tap → Checked/pending`

`Checked/pending → second tap → Unchecked/cancelled`

`Checked/pending → timer or pause → Complete → Refresh → Save`

The undo window ends when completion executes, not at an independently enforced timestamp. `Handler` schedules work on its associated thread using uptime; execution can be delayed by queued work or deep sleep. Consequently, “exactly two seconds” is not a separate atomic boundary. [Android Handler documentation](https://developer.android.com/reference/android/os/Handler#postDelayed(java.lang.Runnable,long))

In ordinary UI execution, these callbacks run sequentially. This is primarily an **event-ordering problem**, not evidence of simultaneous threads modifying the list.

- If the second tap handler runs first, it cancels completion.
- If completion runs first, the original chore is generally gone.
- A subsequent tap at its old location can select a replacement row. The precise touch/redraw behavior requires device verification.
- Multiple pending chores calculate their effort independently **when committed**, using the then-current list.

### Long-press, Skip, Postpone, and Cancel

The dialog leaves two things unresolved until its button is pressed:

1. Which chore currently occupies the saved position.
2. Which date the manager currently considers today.

Pending completions can run while the dialog is open. They can remove rows, change effort allowance, and alter selection. Choosing Cancel only cancels the dialog; it does not undo an earlier completion tap.

### Refresh

Refresh changes the allowance, not chore schedules. Therefore it can reveal additional eligible chores but does not restore already completed chores.

A pending completion before Refresh can still consume the newly reset allowance afterward. Conversely, a completion that runs first can have its spent effort reset by Refresh. This ordering follows the current commands; whether Refresh should also resolve pending selections is an unspecified product rule.

### Leaving and returning

`onPause` flushes pending operations synchronously. Each runnable is removed from the handler before execution, and each successful completion saves.

Thus, under a normal lifecycle:

- Leaving early shortens the undo window.
- `onStop` does not ordinarily complete the same chores again.
- Editing/deleting elsewhere does not ordinarily race with an old Today completion.
- The absence of `onDestroyView` cleanup alone does **not** prove that completion callbacks survive normal teardown.

The fragment lifecycle places pause before later stop/destruction transitions. [Android fragment lifecycle](https://developer.android.com/guide/fragments/lifecycle)

### Midnight and clock changes

The tap does not capture a completion date. The manager reads today when completion executes.

A controlled probe produced:

`Tap-time state:       2024-01-01 23:59:59`

`Completion-time state: 2024-01-02 00:00:01`

`Daily chore next date: 2024-01-03`

`Tuesday effort left:  8, from an allowance of 10`

The completion therefore belongs to Tuesday according to implementation. Whether it should instead belong to the tap date needs an explicit requirement.

There is also no dedicated midnight refresh in Today. The displayed rows can remain stale until another event refreshes/rebinds them, while `getItem(position)` already computes the new day’s list.

Normal production `RealTimeService.getToday()` itself calls `LocalDate.now()`. The mixed-clock hypothesis is therefore **not automatically a production clock disagreement**, although it breaks consistent injected-clock testing and allows boundary discrepancies between separate reads.

## 4. Confirmed and suspected bugs

### F1 — Wrong chore or exception after a dialog’s list changes

**Severity: High. Confidence: High; source-proven, underlying sequence reproduced.**

Example:

1. Today shows `[A, B, C]`.
2. Tap A.
3. Long-press B at position 1.
4. A’s completion runs; the list becomes `[B, C]`.
5. Choose Skip or Postpone.

The callback retrieves position 1 and changes **C**.

Opening the dialog for the old final position can instead produce `IndexOutOfBoundsException`. Both the changed lookup and exception were reproduced against the current manager.

Evidence: `TodayFragment.java:115`, `:122`, `:129`.

### F2 — Skip/Postpone leaves conflicting completion queued

**Severity: High. Confidence: High; source-proven and domain sequence reproduced.**

Tap a chore, long-press it, and choose Postpone before its completion runs. Postpone saves successfully, but the pending runnable remains.

The runnable subsequently:

- Spends effort.
- Calls reschedule.
- Clears postponement.
- Saves again.

For a daily chore, the next date may coincidentally match tomorrow, concealing the unwanted effort charge. For a longer recurrence, completion can replace “tomorrow” with a substantially later occurrence.

Skip has the same unwanted subsequent effort charge.

Evidence: `TodayFragment.java:120`, `:127`, `:517`; `Chore.java:132`.

### F3 — Effort accounting depends on completion order

**Severity: Medium. Confidence: High; reproduced.**

With allowance 10 and only two eligible chores, A costing 3 and B costing 4:

| Completion order | Remaining effort |
|---|---:|
| A, then B | 3 |
| B, then A | 0 |

When B is last in a multi-item list, the manager charges `remaining − effort of preceding chores`: **7 instead of B’s actual 4**.

This matters to timing because:

- Normal delayed callbacks usually follow their queued order.
- Pause flushes a map whose iteration does not preserve tap order.
- Refresh and other operations change the list used to calculate the charge.

The arithmetic difference was reproduced; the particular order chosen during an Android lifecycle flush was not.

Evidence: `ChoreManager.java:123`; `TodayFragment.java:427`.

### F4 — Duplicate descriptions disagree about which chore is checked

**Severity: Medium. Confidence: High; reproduced.**

`Chore.equals()` compares descriptions, but the class has no matching `hashCode()` override.

The adapter uses list membership/equality; pending operations use a hash map. With two distinct chores renamed to the same description, the probe showed:

`equals: true`

`Adapter-style checked membership for B after checking A: true`

`Pending-map membership for B: false`

A row can therefore look checked although it has no pending completion. Tapping it can start a completion rather than undo the apparent selection.

Duplicate creation is partly prevented by `containsChore`, but renaming an already stored chore permits duplicates.

Evidence: `Chore.java:64`; `ChoreEditor.java:73`; `SimpleCheckboxListAdapter.java:98`; `TodayFragment.java:466`.

### F5 — Duplicate Serena listener registration

**Severity: Low. Confidence: High; source-proven.**

Today registers once in `onStart` and again in `onResume`; Serena’s listener list accepts duplicates. One notification refreshes the adapters twice while resumed.

Pause and stop each remove one entry, so ordinary full lifecycle cycles do not inherently accumulate registrations indefinitely.

Evidence: `TodayFragment.java:279`, `:293`; `Serena.java:75`.

### F6 — Sunday effort-setting save uses Monday’s allowance

**Severity: Medium. Confidence: High; reproduced.**

`WeeklyEffortTracker.setSunday()` assigns `remaining = mon`.

With Monday 10 and Sunday 30, saving Sunday as 30 on an initialized Sunday changed remaining effort to **10**. `EffortActivity` calls this setter on every Save, so this directly changes the list seen when returning to Today.

Evidence: `WeeklyEffortTracker.java:122`; `EffortActivity.java:118`.

### F7 — Zero recurrence interval can freeze completion or Skip

**Severity: High. Confidence: High from source; not executed to avoid a nonterminating operation.**

The editor accepts integer zero as an interval length. For a due chore, `IntervalRepetition.reschedule()` then loops without advancing the date.

The freeze occurs when Skip executes, the completion timer fires, or pause flushes a pending completion. Postpone can temporarily hide the chore without fixing its interval.

Evidence: `EditChoreActivity.java:290`; `EditTextCriteria.java:28`; `IntervalRepetition.java:75`.

An imported weekday recurrence with no selected weekdays has a related unbounded search. The normal editor does require a selected weekday.

### Remaining risks and unresolved behavior

| Issue | Assessment |
|---|---|
| Midnight changes the item behind a displayed position | Strong source evidence; actual wrong-row interaction/redraw needs Android reproduction. Medium severity. |
| Midnight completion uses the new day | Reproduced behavior; defect status depends on intended date semantics. |
| Abrupt process loss before completion | Pending operation is not persisted, so it cannot be recovered. Normal backgrounding usually flushes first. |
| Chore dialog survives pause/recreation | Chore dialogs are not tracked/dismissed like reminder dialogs. Stale-dialog or window behavior needs device verification. |
| Clock moves backward | Reading effort for a different date resets the allowance; returning to the original date resets it again. Potential unintended replenishment. |
| Delayed callback acts on replaced container data | Possible for retained objects, but no ordinary navigation sequence was established that bypasses the existing pause flush. |

## 5. Evidence references

Production paths below are under `app/src/main/java/stoneframe/serena/`.

| Evidence | Location |
|---|---|
| Row actions and deferred dialog lookup | `gui/today/TodayFragment.java:106` |
| Refresh confirmation and reset | `TodayFragment.java:241` |
| Lifecycle flush and listeners | `TodayFragment.java:279` |
| Pending-operation execution and tapping | `TodayFragment.java:427` |
| Dynamic adapter queries and checked state | `gui/util/SimpleCheckboxListAdapter.java:51` |
| Selection, completion, effort calculation | `chores/ChoreManager.java:58` |
| Equality, postponement, rescheduling | `chores/Chore.java:64` |
| Sunday setter and date-based reset | `chores/efforttrackers/WeeklyEffortTracker.java:122` |
| Recurrence loop | `chores/IntervalRepetition.java:75` |
| External Save/Delete persistence | `gui/EditActivity.java:156` |
| Aggregate persistence | `storages/SharedPreferencesStorage.java:48` |

## 6. Verification and recommended regression tests

Executed:

``@@BT@powershell
.\gradlew.bat :app:testDebugUnitTest --tests "stoneframe.serena.chores.*" --tests "stoneframe.serena.SerenaTest" --rerun --console=plain
``@@BT@

Fresh results:

| Test class | Passed |
|---|---:|
| ChoreComparatorTest | 8 |
| ChoreManagerTest | 1 |
| ChoreTest | 9 |
| SerenaTest | 46 |
| **Total** | **64** |

The existing coverage includes selection, basic completion/Skip, ordering, and interval rescheduling. It does not exercise Today’s Android handler/dialog interactions. The standard `MockEffortTracker` ignores spending/reset, and storage mocks do not establish persistence.

Additional JShell probes used compiled production classes, `MockTimeService`, a real `WeeklyEffortTracker`, and synthetic chores. An initial probe had a classpath failure; the corrected run produced the results reported above.

Priority regression scenarios:

1. Keep a B/C dialog open while A completes; verify intended identity and no bounds exception.
2. Tap then Skip/Postpone before expiry; verify exactly the chosen operation occurs.
3. Complete multiple chores in different orders, including pause flush, with budgets below/equal to/above total effort.
4. Test repeated taps immediately before and after runnable dispatch using a controlled scheduler.
5. Cross midnight during tap, dialog, Refresh, and completion; explicitly define the intended action date.
6. Rename an existing chore to a duplicate description; verify independent checks and cancellation.
7. Verify one listener callback per change while resumed, including pause/resume without stop.
8. Verify Sunday settings, same-day unchanged saves, and forward/backward date changes.
9. Reject zero recurrence intervals and handle invalid imported recurrence safely.
10. Test Save/load round trips and process recovery separately from mock-storage assertions.

## 7. Android/manual scenarios still requiring verification

Use disposable data on a recorded device/API level:

- Execute the stale-dialog sequences for both wrong-item selection and the old final position.
- Tap then immediately Skip/Postpone; repeat with daily and weekly recurrence.
- Complete multiple chores, then immediately navigate away; compare effort with waiting for timers.
- Test taps near expiry while the main thread is busy and while rows redraw.
- Leave Today open across midnight; test a dialog held open across midnight.
- Background, lock/unlock, recreate, and restore with pending operations or dialogs.
- Compare normal background process loss with abrupt termination before the timer fires.
- Verify saved recurrence and effort after a fresh process starts.
- Exercise returning from chore editing, deletion, effort settings, and storage replacement.

These have not been verified on a device, and passing JVM tests does not establish their Android behavior.

## 8. File-change statement

The investigation did not modify production code or tests. This report is the only new working-tree file. `git diff --check` passed for the tracked changes, and running Gradle regenerated build outputs and test reports.
