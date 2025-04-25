package stoneframe.serena.routines;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import stoneframe.serena.mocks.TestContext;

public class WeekRoutineTest
{
    private final LocalDate today = new LocalDate(2024, 1, 1);

    private TestContext context;

    private RoutineManager routineManager;

    @Before
    public void before()
    {
        context = new TestContext().setCurrentTime(today);

        routineManager = context.getRoutineManager();
    }

    @Test
    public void getNextProcedureTime_getNextBeforeTimeOfProcedure_returnTimeOfProcedure()
    {
        // ARRANGE
        WeekRoutine routine = routineManager.createWeekRoutine();

        WeekRoutineEditor routineEditor = routineManager.getWeekRoutineEditor(routine);

        routineEditor.addProcedure(
            DateTimeConstants.MONDAY,
            new Procedure("Procedure", new LocalTime(10, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today);

        LocalDateTime nextProcedureTime = routineManager.getNextProcedureTime();

        // ASSERT
        assertEquals(today.toLocalDateTime(new LocalTime(10, 0)), nextProcedureTime);
    }

    @Test
    public void getNextProcedureTime_getNextAfterTimeOfProcedure_returnTimeOfProcedureNextWeek()
    {
        // ARRANGE
        WeekRoutine routine = routineManager.createWeekRoutine();

        WeekRoutineEditor routineEditor = routineManager.getWeekRoutineEditor(routine);

        routineEditor.addProcedure(
            DateTimeConstants.MONDAY,
            new Procedure("Procedure", new LocalTime(10, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today.toLocalDateTime(new LocalTime(12, 0)));

        LocalDateTime nextProcedureTime = routineManager.getNextProcedureTime();

        // ASSERT
        assertEquals(today.plusWeeks(1).toLocalDateTime(new LocalTime(10, 0)), nextProcedureTime);
    }

    @Test
    public void getPendingProcedures_noProceduresAdded_returnEmptyList()
    {
        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void getPendingProcedures_getPendingBeforeTimeOfProcedure_returnEmptyList()
    {
        // ARRANGE
        WeekRoutine routine = routineManager.createWeekRoutine();

        WeekRoutineEditor routineEditor = routineManager.getWeekRoutineEditor(routine);

        routineEditor.addProcedure(
            DateTimeConstants.MONDAY,
            new Procedure("Procedure", new LocalTime(10, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today);

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void getPendingProcedures_getPendingTwoDaysAfterProcedureAdded_listContainsProcedure()
    {
        // ARRANGE
        WeekRoutine routine = routineManager.createWeekRoutine();

        WeekRoutineEditor routineEditor = routineManager.getWeekRoutineEditor(routine);

        routineEditor.addProcedure(
            DateTimeConstants.MONDAY,
            new Procedure("Procedure", new LocalTime(10, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today.plusDays(2));

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertEquals(1, pendingProcedures.size());
        assertEquals("Procedure", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void getPendingProcedures_getPendingAtTimeOfProcedureAdded_listContainsProcedure()
    {
        // ARRANGE
        WeekRoutine routine = routineManager.createWeekRoutine();

        WeekRoutineEditor routineEditor = routineManager.getWeekRoutineEditor(routine);

        routineEditor.addProcedure(
            DateTimeConstants.MONDAY,
            new Procedure("Procedure", new LocalTime(10, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today.toLocalDateTime(new LocalTime(10, 0)));

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertEquals(1, pendingProcedures.size());
        assertEquals("Procedure", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void getPendingProcedures_getPendingBetweenTimesOfTwoAddedProcedures_listContainsFirstProcedure()
    {
        // ARRANGE
        WeekRoutine routine = routineManager.createWeekRoutine();

        WeekRoutineEditor routineEditor = routineManager.getWeekRoutineEditor(routine);

        routineEditor.addProcedure(
            DateTimeConstants.MONDAY,
            new Procedure("Procedure 1", new LocalTime(15, 0)));

        routineEditor.addProcedure(
            DateTimeConstants.FRIDAY,
            new Procedure("Procedure 2", new LocalTime(23, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today.plusDays(3));

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertEquals(1, pendingProcedures.size());
        assertEquals("Procedure 1", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void getPendingProcedures_getPendingAfterTwoAddedProcedures_listContainsBothProcedures()
    {
        // ARRANGE
        WeekRoutine routine = routineManager.createWeekRoutine();

        WeekRoutineEditor routineEditor = routineManager.getWeekRoutineEditor(routine);

        routineEditor.addProcedure(
            DateTimeConstants.MONDAY,
            new Procedure("Procedure 1", new LocalTime(15, 0)));

        routineEditor.addProcedure(
            DateTimeConstants.FRIDAY,
            new Procedure("Procedure 2", new LocalTime(23, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today.plusDays(6).toLocalDateTime(new LocalTime(0, 0)));

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertEquals(2, pendingProcedures.size());
        assertEquals("Procedure 1", pendingProcedures.get(0).getDescription());
        assertEquals("Procedure 2", pendingProcedures.get(1).getDescription());
    }

    @Test
    public void getPendingProcedures_getPendingAfterTwoProceduresAndTheFirstAgain_listContainsBothProceduresAndFirstTwice()
    {
        // ARRANGE
        WeekRoutine routine = routineManager.createWeekRoutine();

        WeekRoutineEditor routineEditor = routineManager.getWeekRoutineEditor(routine);

        routineEditor.addProcedure(
            DateTimeConstants.MONDAY,
            new Procedure("Procedure 1", new LocalTime(15, 0)));

        routineEditor.addProcedure(
            DateTimeConstants.FRIDAY,
            new Procedure("Procedure 2", new LocalTime(23, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today.plusDays(9));

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertEquals(3, pendingProcedures.size());
        assertEquals("Procedure 1", pendingProcedures.get(0).getDescription());
        assertEquals("Procedure 2", pendingProcedures.get(1).getDescription());
        assertEquals("Procedure 1", pendingProcedures.get(2).getDescription());
    }

    @Test
    public void procedureDone_markPendingProcedureAsDone_getPendingProceduresIsEmpty()
    {
        // ARRANGE
        WeekRoutine routine = routineManager.createWeekRoutine();

        WeekRoutineEditor routineEditor = routineManager.getWeekRoutineEditor(routine);

        routineEditor.addProcedure(
            DateTimeConstants.MONDAY,
            new Procedure("Procedure", new LocalTime(10, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today.plusDays(2));

        PendingProcedure pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void procedureDone_markFirstPendingProcedureAsDone_getPendingProceduresContainsSecondPendingProcedure()
    {
        // ARRANGE
        WeekRoutine routine = routineManager.createWeekRoutine();

        WeekRoutineEditor routineEditor = routineManager.getWeekRoutineEditor(routine);

        routineEditor.addProcedure(
            DateTimeConstants.MONDAY,
            new Procedure("Procedure 1", new LocalTime(10, 0)));

        routineEditor.addProcedure(
            DateTimeConstants.TUESDAY,
            new Procedure("Procedure 2", new LocalTime(15, 0)));

        routineEditor.save();

        // ACT
        context.setCurrentTime(today.plusDays(2));

        PendingProcedure pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        assertEquals(1, pendingProcedures.size());
        assertEquals("Procedure 2", pendingProcedures.get(0).getDescription());
    }
}
