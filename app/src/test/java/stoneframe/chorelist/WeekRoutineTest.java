package stoneframe.chorelist;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import stoneframe.chorelist.model.Procedure;
import stoneframe.chorelist.model.WeekRoutine;

public class WeekRoutineTest
{
    private final DateTime now = new DateTime(2024, 1, 1, 0, 0);

    private WeekRoutine routine;

    @Before
    public void before()
    {
        routine = new WeekRoutine("Week Routing", now);
    }

    @Test
    public void getNextProcedureTime_Monday10AndNowIsDay1_returnCorrectTime()
    {
        routine.getWeekDay(DateTimeConstants.MONDAY)
            .addProcedure(new Procedure("Procedure", new LocalTime(10, 0)));

        DateTime next = routine.getNextProcedureTime(now);

        assertEquals(new DateTime(2024, 1, 1, 10, 0), next);
    }

    @Test
    public void getNextProcedureTime_Monday10AndNowIsDay1Hour12_returnCorrectTime()
    {
        routine.getWeekDay(DateTimeConstants.MONDAY)
            .addProcedure(new Procedure("Procedure", new LocalTime(10, 0)));

        DateTime next = routine.getNextProcedureTime(now.plusHours(12));

        assertEquals(new DateTime(2024, 1, 8, 10, 0), next);
    }

    @Test
    public void getPendingProcedures_NoProcedures_returnEmptyList()
    {
        List<Procedure> pendingProcedures = routine.getPendingProcedures(now);

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void getPendingProcedures_Monday10AndNowIsDay1Hour0_returnEmptyList()
    {
        routine.getWeekDay(DateTimeConstants.MONDAY)
            .addProcedure(new Procedure("Procedure", new LocalTime(10, 0)));

        List<Procedure> pendingProcedures = routine.getPendingProcedures(now);

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void getPendingProcedures_Monday10AndNowIsDay2Hour0_listContainsMonday10Procedure()
    {
        routine.getWeekDay(DateTimeConstants.MONDAY)
            .addProcedure(new Procedure("Procedure", new LocalTime(10, 0)));

        List<Procedure> pendingProcedures = routine.getPendingProcedures(now.plusDays(2));

        assertEquals(1, pendingProcedures.size());
        assertEquals("Procedure", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void getPendingProcedures_Monday10AndNowIsDay1Hour10_listContainsMonday10Procedure()
    {
        routine.getWeekDay(DateTimeConstants.MONDAY)
            .addProcedure(new Procedure("Procedure", new LocalTime(10, 0)));

        List<Procedure> pendingProcedures = routine.getPendingProcedures(now.plusHours(10));

        assertEquals(1, pendingProcedures.size());
        assertEquals("Procedure", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void getPendingProcedures_Monday15AndFriday23NowIsDay3Hour0_listContainsMonday10Procedure()
    {
        routine.getWeekDay(DateTimeConstants.MONDAY)
            .addProcedure(new Procedure("Procedure 1", new LocalTime(15, 0)));

        routine.getWeekDay(DateTimeConstants.FRIDAY)
            .addProcedure(new Procedure("Procedure 2", new LocalTime(23, 0)));

        List<Procedure> pendingProcedures = routine.getPendingProcedures(now.plusDays(3));

        assertEquals(1, pendingProcedures.size());
        assertEquals("Procedure 1", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void getPendingProcedures_Monday15AndFriday23NowIsDay6Hour0_listContainsMonday10AndFriday23Procedures()
    {
        routine.getWeekDay(DateTimeConstants.MONDAY)
            .addProcedure(new Procedure("Procedure 1", new LocalTime(15, 0)));

        routine.getWeekDay(DateTimeConstants.FRIDAY)
            .addProcedure(new Procedure("Procedure 2", new LocalTime(23, 0)));

        List<Procedure> pendingProcedures = routine.getPendingProcedures(now.plusDays(6));

        assertEquals(2, pendingProcedures.size());
        assertEquals("Procedure 1", pendingProcedures.get(0).getDescription());
        assertEquals("Procedure 2", pendingProcedures.get(1).getDescription());
    }

    @Test
    public void getPendingProcedures_Monday15AndFriday23NowIsDay9Hour0_listContainsFriday23AndMonday10Procedures()
    {
        routine.getWeekDay(DateTimeConstants.MONDAY)
            .addProcedure(new Procedure("Procedure 1", new LocalTime(15, 0)));

        routine.getWeekDay(DateTimeConstants.FRIDAY)
            .addProcedure(new Procedure("Procedure 2", new LocalTime(23, 0)));

        List<Procedure> pendingProcedures = routine.getPendingProcedures(now.plusDays(9));

        assertEquals(2, pendingProcedures.size());
        assertEquals("Procedure 2", pendingProcedures.get(0).getDescription());
        assertEquals("Procedure 1", pendingProcedures.get(1).getDescription());
    }

    @Test
    public void procedureDone_Monday10IsDoneAndNowIsDay2Hour0_listIsEmpty()
    {
        Procedure procedure = new Procedure("Procedure", new LocalTime(10, 0));

        routine.getWeekDay(DateTimeConstants.MONDAY).addProcedure(procedure);

        routine.procedureDone(procedure, now.plusDays(2));

        List<Procedure> pendingProcedures = routine.getPendingProcedures(now.plusDays(2));

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void procedureDone_Monday10IsDoneAndTuesday15AndNowIsDay3Hour0_listIsEmpty()
    {
        Procedure procedure1 = new Procedure("Procedure 1", new LocalTime(10, 0));
        Procedure procedure2 = new Procedure("Procedure 2", new LocalTime(15, 0));

        routine.getWeekDay(DateTimeConstants.MONDAY).addProcedure(procedure1);
        routine.getWeekDay(DateTimeConstants.TUESDAY).addProcedure(procedure2);

        routine.procedureDone(procedure1, now.plusDays(2));

        List<Procedure> pendingProcedures = routine.getPendingProcedures(now.plusDays(2));

        assertEquals(1, pendingProcedures.size());
        assertEquals("Procedure 2", pendingProcedures.get(0).getDescription());
    }
}
