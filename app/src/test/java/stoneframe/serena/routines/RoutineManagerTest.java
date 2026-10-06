package stoneframe.serena.routines;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import stoneframe.serena.mocks.MockTimeService;

public class RoutineManagerTest
{
    private static final LocalDate INITIAL_DATE = new LocalDate(2024, 1, 1);
    private static final LocalDate SELECTED_DATE = new LocalDate(2024, 1, 15);

    private RoutineContainer container;
    private MockTimeService timeService;
    private RoutineManager routineManager;
    private Procedure earlier;
    private Procedure selected;
    private Procedure simultaneous;
    private Procedure later;

    @Before
    public void setUp()
    {
        container = new RoutineContainer();
        timeService = new MockTimeService(INITIAL_DATE.toLocalDateTime(LocalTime.MIDNIGHT));
        routineManager = new RoutineManager(() -> container, timeService);
        earlier = new Procedure("Earlier", new LocalTime(9, 0));
        selected = new Procedure("Selected", new LocalTime(10, 0));
        simultaneous = new Procedure("Simultaneous", new LocalTime(10, 0));
        later = new Procedure("Later", new LocalTime(11, 0));
    }

    @Test
    public void procedureDone_pastOccurrence_usesSuppliedDate()
    {
        List<Routine<?>> routines = createRoutines();
        timeService.setNow(SELECTED_DATE.plusDays(1).toLocalDateTime(new LocalTime(12, 0)));

        for (Routine<?> routine : routines)
        {
            routineManager.procedureDone(getOccurrence(routine, selected, SELECTED_DATE));

            assertCompletionBoundary(routine);
        }
    }

    @Test
    public void procedureDone_laterTodayOccurrence_completesBeforeScheduledTime()
    {
        List<Routine<?>> routines = createRoutines();
        timeService.setNow(SELECTED_DATE.toLocalDateTime(new LocalTime(9, 0)));

        for (Routine<?> routine : routines)
        {
            routineManager.procedureDone(getOccurrence(routine, selected, SELECTED_DATE));

            assertCompletionBoundary(routine);
        }
    }

    @Test
    public void procedureDone_futureOccurrence_usesSuppliedDate()
    {
        List<Routine<?>> routines = createRoutines();
        timeService.setNow(INITIAL_DATE.plusDays(2).toLocalDateTime(new LocalTime(12, 0)));

        for (Routine<?> routine : routines)
        {
            routineManager.procedureDone(getOccurrence(routine, selected, SELECTED_DATE));

            assertCompletionBoundary(routine);
        }
    }

    @Test
    public void procedureDone_occurrenceAtCurrentTime_completesOccurrence()
    {
        List<Routine<?>> routines = createRoutines();
        timeService.setNow(SELECTED_DATE.toLocalDateTime(selected.getTime()));

        for (Routine<?> routine : routines)
        {
            routineManager.procedureDone(getOccurrence(routine, selected, SELECTED_DATE));

            assertCompletionBoundary(routine);
        }
    }

    @Test
    public void procedureDone_weeklyOccurrence_completesAcrossWeekBoundary()
    {
        WeekRoutine routine = (WeekRoutine)createRoutine(Routine.WEEK_ROUTINE, selected, later);
        Procedure sunday = new Procedure("Sunday", new LocalTime(18, 0));
        routine.getWeek().getSunday().addProcedure(sunday);

        routineManager.procedureDone(getOccurrence(routine, selected, SELECTED_DATE));

        assertTrue(routine.isDone(SELECTED_DATE.minusDays(1), sunday));
        assertTrue(routine.isDone(SELECTED_DATE.minusWeeks(1), selected));
        assertFalse(routine.isDone(SELECTED_DATE, later));
        assertFalse(routine.isDone(SELECTED_DATE.plusDays(6), sunday));
        assertFalse(routine.isDone(SELECTED_DATE.plusWeeks(1), selected));
    }

    @Test
    public void procedureDone_fortnightOccurrence_completesAcrossBothWeeks()
    {
        FortnightRoutine routine = (FortnightRoutine)createRoutine(Routine.FORTNIGHT_ROUTINE, selected, later);
        Procedure sunday = new Procedure("Week two Sunday", new LocalTime(18, 0));
        routine.getWeek2().getSunday().addProcedure(sunday);

        routineManager.procedureDone(getOccurrence(routine, selected, SELECTED_DATE));

        assertTrue(routine.isDone(SELECTED_DATE.minusDays(1), sunday));
        assertTrue(routine.isDone(INITIAL_DATE, selected));
        assertFalse(routine.isDone(SELECTED_DATE, later));
        assertFalse(routine.isDone(SELECTED_DATE.plusDays(13), sunday));
        assertFalse(routine.isDone(SELECTED_DATE.plusWeeks(2), selected));
    }

    @Test
    public void procedureDone_equalProceduresInOtherRoutine_onlyCompletesSelectedRoutine()
    {
        Routine<?> routine = createRoutine(Routine.DAY_ROUTINE, selected);
        Procedure otherProcedure = selected.copy();
        Routine<?> otherRoutine = createRoutine(Routine.DAY_ROUTINE, otherProcedure);

        routineManager.procedureDone(getOccurrence(routine, selected, SELECTED_DATE));

        assertTrue(routine.isDone(SELECTED_DATE, selected));
        assertFalse(otherRoutine.isDone(INITIAL_DATE, otherProcedure));
        assertFalse(otherRoutine.isDone(SELECTED_DATE, otherProcedure));
    }

    @Test
    public void procedureDone_repeatedOrOlderOccurrence_doesNotMoveCompletionBackward()
    {
        for (Routine<?> routine : createRoutines())
        {
            routineManager.procedureDone(getOccurrence(routine, selected, SELECTED_DATE));

            routineManager.procedureDone(getOccurrence(routine, selected, SELECTED_DATE));
            routineManager.procedureDone(getOccurrence(routine, simultaneous, SELECTED_DATE));
            routineManager.procedureDone(getOccurrence(routine, selected, INITIAL_DATE));

            assertCompletionBoundary(routine);
        }
    }

    @Test
    public void procedureDone_stalePendingOccurrence_doesNotUndoFutureCompletion()
    {
        List<Routine<?>> routines = createRoutines();
        timeService.setNow(INITIAL_DATE.plusDays(2).toLocalDateTime(new LocalTime(12, 0)));

        for (Routine<?> routine : routines)
        {
            ProcedureOccurrence stale = routineManager.getPendingProcedures().stream()
                .filter(p -> p.getRoutine() == routine && p.getProcedure() == selected)
                .findFirst()
                .get();
            routineManager.procedureDone(getOccurrence(routine, selected, SELECTED_DATE));

            routineManager.procedureDone(stale);

            assertCompletionBoundary(routine);
        }
    }

    @Test
    public void procedureDone_repeatedPendingOccurrence_preservesCompletion()
    {
        List<Routine<?>> routines = createRoutines();
        timeService.setNow(SELECTED_DATE.toLocalDateTime(selected.getTime()));

        for (Routine<?> routine : routines)
        {
            ProcedureOccurrence pending = routineManager.getPendingProcedures().stream()
                .filter(p -> p.getRoutine() == routine && p.getProcedure() == selected)
                .filter(p -> p.getDateTime().toLocalDate().equals(SELECTED_DATE))
                .findFirst()
                .get();

            routineManager.procedureDone(pending);
            routineManager.procedureDone(pending);

            assertCompletionBoundary(routine);
        }
    }

    @Test
    public void procedureDone_removedRoutine_doesNotMutateOldReference()
    {
        for (Routine<?> routine : createRoutines())
        {
            ProcedureOccurrence occurrence = getOccurrence(routine, selected, SELECTED_DATE);
            routineManager.removeRoutine(routine);

            routineManager.procedureDone(occurrence);

            assertFalse(routine.isDone(INITIAL_DATE, selected));
            assertFalse(routine.isDone(SELECTED_DATE, selected));
        }
    }

    @Test
    public void procedureDone_replacedContainer_doesNotMutateOldOrCurrentRoutines()
    {
        List<Routine<?>> oldRoutines = createRoutines();
        List<ProcedureOccurrence> oldOccurrences = routineManager.getProceduresForDate(SELECTED_DATE);
        container = new RoutineContainer();
        List<Routine<?>> currentRoutines = createRoutines();

        for (Routine<?> routine : oldRoutines)
        {
            ProcedureOccurrence occurrence = oldOccurrences.stream()
                .filter(p -> p.getRoutine() == routine && p.getProcedure() == selected)
                .findFirst()
                .get();

            routineManager.procedureDone(occurrence);

            assertFalse(routine.isDone(INITIAL_DATE, selected));
            assertFalse(routine.isDone(SELECTED_DATE, selected));
        }

        for (Routine<?> routine : currentRoutines)
        {
            assertFalse(routine.isDone(INITIAL_DATE, selected));
            assertFalse(routine.isDone(SELECTED_DATE, selected));
        }
        routineManager.procedureDone(getOccurrence(currentRoutines.get(0), selected, SELECTED_DATE));
        assertTrue(currentRoutines.get(0).isDone(SELECTED_DATE, selected));
    }

    @Test
    public void procedureDone_completedInAdvance_staysAbsentWhenDueAndLaterOccurrencesBecomePending()
    {
        List<Routine<?>> routines = createRoutines();
        LocalDateTime scheduledTime = SELECTED_DATE.toLocalDateTime(selected.getTime());

        for (Routine<?> routine : routines)
        {
            routineManager.procedureDone(getOccurrence(routine, selected, SELECTED_DATE));

            timeService.setNow(scheduledTime.minusMillis(1));
            assertNoPendingOccurrences(routine);
            timeService.setNow(scheduledTime);
            assertNoPendingOccurrences(routine);
            timeService.setNow(scheduledTime.plusMillis(1));
            assertNoPendingOccurrences(routine);

            LocalDate nextDate = getNextOccurrenceDate(routine);
            timeService.setNow(nextDate.toLocalDateTime(selected.getTime()));
            assertTrue(routineManager.getPendingProcedures().stream()
                .anyMatch(p -> p.getRoutine() == routine && p.getProcedure() == selected
                    && p.getDateTime().toLocalDate().equals(nextDate)));
        }
    }

    @Test
    public void procedureDone_nullOccurrence_throwsBeforeMutation()
    {
        createRoutines();
        timeService.setNow(SELECTED_DATE.plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT));
        List<ProcedureOccurrence> before = routineManager.getPendingProcedures();

        assertThrows(NullPointerException.class, () -> routineManager.procedureDone(null));

        assertEquals(before, routineManager.getPendingProcedures());
    }

    @Test
    public void getProceduresForDate_allRoutineTypes_retainsReferencesAndRequestedDate()
    {
        List<Routine<?>> routines = createRoutines();
        timeService.setNow(INITIAL_DATE.plusDays(2).toLocalDateTime(new LocalTime(12, 0)));

        List<ProcedureOccurrence> occurrences = routineManager.getProceduresForDate(SELECTED_DATE);

        assertEquals(12, occurrences.size());
        for (Routine<?> routine : routines)
        {
            ProcedureOccurrence occurrence = occurrences.stream()
                .filter(p -> p.getRoutine() == routine && p.getProcedure() == selected)
                .findFirst()
                .get();
            assertSame(routine, occurrence.getRoutine());
            assertSame(selected, occurrence.getProcedure());
            assertEquals(selected.getDescription(), occurrence.getDescription());
            assertEquals(SELECTED_DATE.toLocalDateTime(selected.getTime()), occurrence.getDateTime());
        }
    }

    @Test
    public void getProceduresForDate_unsortedAndEqualTimeProcedures_preservesChronologicalAndTieOrder()
    {
        Routine<?> firstRoutine = createRoutine(Routine.DAY_ROUTINE, later, selected, earlier, simultaneous);
        Procedure equalProcedure = selected.copy();
        Routine<?> secondRoutine = createRoutine(Routine.WEEK_ROUTINE, equalProcedure);

        List<ProcedureOccurrence> occurrences = routineManager.getProceduresForDate(SELECTED_DATE);

        assertEquals(5, occurrences.size());
        assertSame(earlier, occurrences.get(0).getProcedure());
        assertSame(selected, occurrences.get(1).getProcedure());
        assertSame(simultaneous, occurrences.get(2).getProcedure());
        assertSame(equalProcedure, occurrences.get(3).getProcedure());
        assertSame(later, occurrences.get(4).getProcedure());
        assertSame(firstRoutine, occurrences.get(1).getRoutine());
        assertSame(secondRoutine, occurrences.get(3).getRoutine());
        assertNotEquals(occurrences.get(1), occurrences.get(3));
    }

    @Test
    public void getProceduresForDate_disabledRoutines_excludesTheirOccurrences()
    {
        List<Routine<?>> routines = createRoutines();
        routines.get(0).setEnabled(false);
        routines.get(2).setEnabled(false);

        List<ProcedureOccurrence> occurrences = routineManager.getProceduresForDate(SELECTED_DATE);

        assertEquals(4, occurrences.size());
        for (ProcedureOccurrence occurrence : occurrences)
        {
            assertSame(routines.get(1), occurrence.getRoutine());
        }
    }

    @Test
    public void getProceduresForDate_noScheduledProcedures_returnsEmptyList()
    {
        assertTrue(routineManager.getProceduresForDate(SELECTED_DATE).isEmpty());
        createRoutine(Routine.WEEK_ROUTINE, selected);

        assertTrue(routineManager.getProceduresForDate(SELECTED_DATE.plusDays(1)).isEmpty());
    }

    @Test
    public void getProceduresForDate_completedAndFutureOccurrences_includesBothWithoutChangingState()
    {
        List<Routine<?>> routines = createRoutines();
        for (Routine<?> routine : routines)
        {
            routineManager.procedureDone(getOccurrence(routine, selected, INITIAL_DATE));
        }
        timeService.setNow(INITIAL_DATE.plusDays(2).toLocalDateTime(new LocalTime(12, 0)));
        List<ProcedureOccurrence> pendingBefore = routineManager.getPendingProcedures();

        List<ProcedureOccurrence> completedDate = routineManager.getProceduresForDate(INITIAL_DATE);
        List<ProcedureOccurrence> futureDate = routineManager.getProceduresForDate(SELECTED_DATE);

        assertEquals(12, completedDate.size());
        for (ProcedureOccurrence occurrence : completedDate)
        {
            assertEquals(occurrence.getProcedure() != later, occurrence.isDone());
        }
        assertEquals(12, futureDate.size());
        for (ProcedureOccurrence occurrence : futureDate)
        {
            assertFalse(occurrence.isDone());
        }
        assertEquals(pendingBefore, routineManager.getPendingProcedures());
    }

    @Test
    public void isDone_retainedOccurrence_reflectsCompletionAndKeepsItsOriginalDate()
    {
        createRoutine(Routine.DAY_ROUTINE, selected);
        ProcedureOccurrence retained = routineManager.getProceduresForDate(SELECTED_DATE).get(0);
        assertFalse(retained.isDone());

        routineManager.procedureDone(retained);
        ProcedureOccurrence future = routineManager.getProceduresForDate(SELECTED_DATE.plusDays(1)).get(0);

        assertTrue(retained.isDone());
        assertFalse(future.isDone());
        assertEquals(SELECTED_DATE.toLocalDateTime(selected.getTime()), retained.getDateTime());
    }

    @Test
    public void getProceduresForDate_shiftedFortnightAnchor_selectsBothWeeksAndRepeats()
    {
        FortnightRoutine routine = (FortnightRoutine)createRoutine(Routine.FORTNIGHT_ROUTINE, selected);
        LocalDate shiftedAnchor = INITIAL_DATE.plusWeeks(1);
        routine.setStartDate(shiftedAnchor);
        Procedure weekTwo = selected.copy();
        routine.getWeek2().getMonday().addProcedure(weekTwo);

        List<ProcedureOccurrence> firstWeek = routineManager.getProceduresForDate(shiftedAnchor);
        List<ProcedureOccurrence> secondWeek = routineManager.getProceduresForDate(shiftedAnchor.plusWeeks(1));
        List<ProcedureOccurrence> repeatedWeek = routineManager.getProceduresForDate(shiftedAnchor.plusWeeks(2));

        assertEquals(1, firstWeek.size());
        assertEquals(1, secondWeek.size());
        assertEquals(1, repeatedWeek.size());
        assertSame(selected, firstWeek.get(0).getProcedure());
        assertSame(weekTwo, secondWeek.get(0).getProcedure());
        assertSame(selected, repeatedWeek.get(0).getProcedure());
        assertSame(routine, secondWeek.get(0).getRoutine());
        assertEquals(shiftedAnchor.toLocalDateTime(selected.getTime()), firstWeek.get(0).getDateTime());
        assertEquals(shiftedAnchor.plusWeeks(1).toLocalDateTime(weekTwo.getTime()), secondWeek.get(0).getDateTime());
        assertEquals(shiftedAnchor.plusWeeks(2).toLocalDateTime(selected.getTime()), repeatedWeek.get(0).getDateTime());
    }

    @Test
    public void getProceduresForDate_midnightOccurrence_preservesExactTimestamp()
    {
        Procedure midnight = new Procedure("Midnight", LocalTime.MIDNIGHT);
        createRoutine(Routine.DAY_ROUTINE, midnight);

        List<ProcedureOccurrence> occurrences = routineManager.getProceduresForDate(SELECTED_DATE);

        assertEquals(1, occurrences.size());
        assertEquals(SELECTED_DATE.toLocalDateTime(LocalTime.MIDNIGHT), occurrences.get(0).getDateTime());
    }

    private ProcedureOccurrence getOccurrence(Routine<?> routine, Procedure procedure, LocalDate date)
    {
        return routineManager.getProceduresForDate(date).stream()
            .filter(p -> p.getRoutine() == routine && p.getProcedure() == procedure)
            .findFirst()
            .get();
    }

    private List<Routine<?>> createRoutines()
    {
        return Arrays.asList(
            createRoutine(Routine.DAY_ROUTINE, earlier, selected, simultaneous, later),
            createRoutine(Routine.WEEK_ROUTINE, earlier, selected, simultaneous, later),
            createRoutine(Routine.FORTNIGHT_ROUTINE, earlier, selected, simultaneous, later));
    }

    private Routine<?> createRoutine(int routineType, Procedure... procedures)
    {
        Routine<?> routine;
        switch (routineType)
        {
            case Routine.DAY_ROUTINE:
                routine = routineManager.createDayRoutine();
                for (Procedure procedure : procedures)
                {
                    ((DayRoutine)routine).addProcedure(procedure);
                }
                break;
            case Routine.WEEK_ROUTINE:
                routine = routineManager.createWeekRoutine();
                for (Procedure procedure : procedures)
                {
                    ((WeekRoutine)routine).getWeek().getMonday().addProcedure(procedure);
                }
                break;
            case Routine.FORTNIGHT_ROUTINE:
                routine = routineManager.createFortnightRoutine();
                for (Procedure procedure : procedures)
                {
                    ((FortnightRoutine)routine).getWeek1().getMonday().addProcedure(procedure);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown routine type: " + routineType);
        }
        routineManager.addRoutine(routine);
        return routine;
    }

    private void assertCompletionBoundary(Routine<?> routine)
    {
        assertTrue(routine.isDone(INITIAL_DATE, selected));
        assertTrue(routine.isDone(SELECTED_DATE, earlier));
        assertTrue(routine.isDone(SELECTED_DATE, selected));
        assertTrue(routine.isDone(SELECTED_DATE, simultaneous));
        assertFalse(routine.isDone(SELECTED_DATE, later));
        assertFalse(routine.isDone(SELECTED_DATE.plusWeeks(2), selected));
    }

    private void assertNoPendingOccurrences(Routine<?> routine)
    {
        assertFalse(routineManager.getPendingProcedures().stream().anyMatch(p -> p.getRoutine() == routine));
    }

    private LocalDate getNextOccurrenceDate(Routine<?> routine)
    {
        switch (routine.getRoutineType())
        {
            case Routine.DAY_ROUTINE:
                return SELECTED_DATE.plusDays(1);
            case Routine.WEEK_ROUTINE:
                return SELECTED_DATE.plusWeeks(1);
            case Routine.FORTNIGHT_ROUTINE:
                return SELECTED_DATE.plusWeeks(2);
            default:
                throw new IllegalArgumentException("Unknown routine type: " + routine.getRoutineType());
        }
    }
}
