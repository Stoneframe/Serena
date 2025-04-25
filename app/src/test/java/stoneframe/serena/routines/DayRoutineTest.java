package stoneframe.serena.routines;

import static junit.framework.TestCase.assertEquals;

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

        PendingProcedure pendingProcedure = routineManager.getPendingProcedures().get(0);

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

        PendingProcedure pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        LocalDateTime nextProcedureTime = routineManager.getNextProcedureTime();

        assertEquals(today.toLocalDateTime(new LocalTime(23, 0)), nextProcedureTime);
    }
}
