# RoutineManager bug review

Reviewed: 2026-09-25. Scope: `RoutineManager`, daily/weekly/fortnightly recurrence, routine editing, Today and overview screens, and routine alarm delivery. Findings below follow the current source paths; verification status is recorded at the end.

## How the manager is used

`Serena.getRoutineManager()` creates a manager backed by the current `RoutineContainer`. The routines screen creates and edits routines; the editor adds, removes, or checkpoints them, and the screen persists changes and schedules the next alarm. Today asks for the first pending occurrence from each enabled routine, completes selected occurrences through `procedureDone`, and saves. The overview screen asks for procedures on a chosen date. The alarm receiver asks for all pending occurrences, posts a notification, and schedules the next future occurrence. See [Serena](../app/src/main/java/stoneframe/serena/Serena.java#L94), [AllRoutinesFragment](../app/src/main/java/stoneframe/serena/gui/routines/AllRoutinesFragment.java#L136), [TodayFragment](../app/src/main/java/stoneframe/serena/gui/today/TodayFragment.java#L94), [RoutineOverviewActivity](../app/src/main/java/stoneframe/serena/gui/routines/RoutineOverviewActivity.java#L90), and [RoutineNotifierReceiver](../app/src/main/java/stoneframe/serena/gui/notifications/RoutineNotifierReceiver.java#L11).

## Findings

### 1. Completing a duplicate procedure can update the wrong routine (resolved)

**Trigger:** Save routine A, then routine B, each with a procedure having the same description and time. Once both occurrences are pending, complete B from Today (or use **Clear routines**).

**Status:** `procedureDone` finds the active routine containing the exact `Procedure` instance held by the pending occurrence. Completing B therefore updates B, even when A has a value-equal procedure. [Manager lookup](../app/src/main/java/stoneframe/serena/routines/RoutineManager.java#L112), [regression test](../app/src/test/java/stoneframe/serena/routines/DayRoutineTest.java#L84).

**Related UI issue:** Resolved. `PendingProcedure.equals` and `hashCode` use the `Procedure` instance identity and occurrence date/time. Occurrences backed by separate but value-equal procedures stay distinct, and newly created wrappers around the same procedure remain equal across manager queries. [Pending equality](../app/src/main/java/stoneframe/serena/routines/PendingProcedure.java#L37), [Today selection map](../app/src/main/java/stoneframe/serena/gui/today/TodayFragment.java#L464), [checkbox adapter](../app/src/main/java/stoneframe/serena/gui/util/SimpleCheckboxListAdapter.java#L109).

### 2. A daily routine can skip its earliest alarm tomorrow (high)

**Trigger:** Add a 20:00 procedure, then a 10:00 procedure to the same daily routine. Query the next occurrence after 20:00, or let the 20:00 alarm receiver schedule the following alarm.

**Actual result:** The next alarm is set for **20:00 tomorrow**, skipping **10:00 tomorrow**. `DayRoutine.getNextProcedureTime` sorts a stream for today's remaining occurrences, but its next-day fallback reads `data().procedures.get(0)` from insertion order. The notification receiver uses this result to schedule its one next alarm, so the 10:00 occurrence is first noticed when a later alarm or app launch refreshes pending procedures. [Daily fallback](../app/src/main/java/stoneframe/serena/routines/DayRoutine.java#L56), [procedure insertion](../app/src/main/java/stoneframe/serena/routines/DayRoutine.java#L85), [alarm scheduling](../app/src/main/java/stoneframe/serena/gui/notifications/Notifier.java#L45), [receiver](../app/src/main/java/stoneframe/serena/gui/notifications/RoutineNotifierReceiver.java#L17).

**Expected:** The fallback uses the earliest procedure time, regardless of insertion order. The current daily tests exercise a single procedure and a same-day next occurrence, but not this multi-procedure next-day boundary. [DayRoutineTest](../app/src/test/java/stoneframe/serena/routines/DayRoutineTest.java#L29).

### 3. Browsing before a fortnight's start week can crash the overview (medium)

**Trigger:** Create an enabled fortnight routine with start week Monday 2026-09-21. In Routine Overview, navigate to Monday 2026-09-14.

**Actual result:** `Weeks.weeksBetween` yields `-1`; Java's `-1 % 2 + 1` is `0`. `getWeek(0)` throws `IllegalArgumentException`, and the overview query has no error handling. The overview allows unrestricted backward navigation, and `RoutineManager.getProceduresForDate` queries every enabled routine, so even an otherwise empty fortnight routine can trigger this path. [Fortnight week selection](../app/src/main/java/stoneframe/serena/routines/FortnightRoutine.java#L116), [week validation](../app/src/main/java/stoneframe/serena/routines/FortnightRoutine.java#L44), [manager query](../app/src/main/java/stoneframe/serena/routines/RoutineManager.java#L66), [overview navigation](../app/src/main/java/stoneframe/serena/gui/routines/RoutineOverviewActivity.java#L59).

**Expected:** The overview handles dates before the configured start week without throwing, using the intended recurrence policy for historical dates or showing no procedures before the routine begins.

### 4. Routine alarms are not restored after a device reboot (high)

**Trigger:** Save a routine with a future procedure, reboot the device, and do not open Serena before that procedure time.

**Actual result:** The pending `AlarmManager` alarm is cleared on shutdown, and the app has no boot receiver to schedule it again. Routine alarms are scheduled on Main startup, after editing, and by the existing alarm receiver; none of those paths runs solely because the device booted. Thus the due routine does not trigger a notification at its scheduled time. Opening the app later makes the occurrence visible in Today and schedules a future alarm; it does not restore the missed notification time. [Main startup](../app/src/main/java/stoneframe/serena/gui/MainActivity.java#L214), [edit callback](../app/src/main/java/stoneframe/serena/gui/routines/AllRoutinesFragment.java#L235), [routine receiver](../app/src/main/java/stoneframe/serena/gui/notifications/RoutineNotifierReceiver.java#L11), [manifest receivers](../app/src/main/AndroidManifest.xml#L85). Android documents that [alarms are cleared when a device shuts down](https://developer.android.com/develop/background-work/services/alarms).

**Expected:** After boot, the app reloads saved state and schedules the next routine alarm without requiring a manual launch. This integration effect follows from source and the Android alarm contract; it was not checked on a device.

## Verification limits

The routine JVM suites passed after the procedure-identity and equality change: 37 tests passed across `DayRoutineTest`, `WeekRoutineTest`, and `FortnightRoutineTest`. The regression test checks that equal-looking occurrences backed by separate procedure instances stay distinct, refreshed results for the same procedure remain equal, and completion updates the routine containing that procedure. The Today adapter's rendered checkbox and undo behavior was not exercised on Android. The daily insertion-order alarm case, fortnight pre-start-date case, and reboot behavior remain unverified by dedicated tests or device checks.
