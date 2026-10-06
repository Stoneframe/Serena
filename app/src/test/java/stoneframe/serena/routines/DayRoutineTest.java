package stoneframe.serena.routines;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import stoneframe.serena.mocks.TestContext;

public class DayRoutineTest
{
    private final LocalDate today = new LocalDate(2024, 1, 1);

    private TestContext context;

    private RoutineManager routineManager;

    @Before
    public void before()
    {
        context = new TestContext();

        routineManager = context.getRoutineManager();
    }

    @Test
    public void procedureDone_testCase1_getNextProcedureTimeReturnsCorrect()
    {
        // ARRANGE
        DayRoutine routine = routineManager.createDayRoutine();

        DayRoutineEditor routineEditor = routineManager.getDayRoutineEditor(routine);

        routineEditor.addProcedure(new Procedure("Time09", new LocalTime(9, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today.toLocalDateTime(new LocalTime(10, 0)));

        ProcedureOccurrence pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        LocalDateTime nextProcedureTime = routineManager.getNextProcedureTime();

        assertEquals(today.plusDays(1).toLocalDateTime(new LocalTime(9, 0)), nextProcedureTime);
    }

    @Test
    public void procedureDone_testCase2_getNextProcedureTimeReturnsCorrect()
    {
        // ARRANGE
        DayRoutine routine = routineManager.createDayRoutine();

        DayRoutineEditor routineEditor = routineManager.getDayRoutineEditor(routine);

        routineEditor.addProcedure(new Procedure("Time09", new LocalTime(9, 0)));
        routineEditor.addProcedure(new Procedure("Time14", new LocalTime(14, 0)));
        routineEditor.addProcedure(new Procedure("Time23", new LocalTime(23, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today.toLocalDateTime(new LocalTime(20, 0)));

        ProcedureOccurrence pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        LocalDateTime nextProcedureTime = routineManager.getNextProcedureTime();

        assertEquals(today.toLocalDateTime(new LocalTime(23, 0)), nextProcedureTime);
    }

    @Test
    public void procedureDone_equalProceduresInDifferentRoutines_completesOwningRoutine()
    {
        // ARRANGE
        DayRoutine firstRoutine = routineManager.createDayRoutine();
        Procedure firstProcedure = new Procedure("Same procedure", new LocalTime(9, 0));
        DayRoutineEditor firstEditor = routineManager.getDayRoutineEditor(firstRoutine);
        firstEditor.addProcedure(firstProcedure);
        firstEditor.save();

        DayRoutine secondRoutine = routineManager.createDayRoutine();
        Procedure secondProcedure = new Procedure("Same procedure", new LocalTime(9, 0));
        DayRoutineEditor secondEditor = routineManager.getDayRoutineEditor(secondRoutine);
        secondEditor.addProcedure(secondProcedure);
        secondEditor.save();

        context.setCurrentTime(today.toLocalDateTime(new LocalTime(10, 0)));

        ProcedureOccurrence firstPendingProcedure = routineManager.getFirstPendingProcedures().stream()
            .filter(p -> p.getProcedure() == firstProcedure)
            .findFirst()
            .get();
        ProcedureOccurrence secondPendingProcedure = routineManager.getFirstPendingProcedures().stream()
            .filter(p -> p.getProcedure() == secondProcedure)
            .findFirst()
            .get();

        assertFalse(firstPendingProcedure.equals(secondPendingProcedure));

        ProcedureOccurrence refreshedSecondPendingProcedure = routineManager.getPendingProcedures().stream()
            .filter(p -> p.getProcedure() == secondProcedure)
            .findFirst()
            .get();

        assertEquals(secondPendingProcedure, refreshedSecondPendingProcedure);
        assertEquals(secondPendingProcedure.hashCode(), refreshedSecondPendingProcedure.hashCode());

        // ACT
        routineManager.procedureDone(secondPendingProcedure);

        // ASSERT
        assertTrue(secondRoutine.isDone(today, secondProcedure));
        assertFalse(firstRoutine.isDone(today, firstProcedure));
    }
}
