package stoneframe.chorelist;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import stoneframe.chorelist.model.FortnightRoutine;
import stoneframe.chorelist.model.PendingProcedure;
import stoneframe.chorelist.model.Procedure;

public class FortnightRoutineTest
{
    private final DateTime now = new DateTime(2024, 1, 1, 0, 0);

    private FortnightRoutine routine;

    @Before
    public void before()
    {
        routine = new FortnightRoutine("Fortnight Routing", now.toLocalDate(), now);
    }

    @Test
    public void getNextProcedureTime_NoProcedures_returnNull()
    {
        DateTime next = routine.getNextProcedureTime(now);

        assertNull(next);
    }

    @Test
    public void getNextProcedureTime_Monday10Week1AndNowIsDay1_returnCorrectTime()
    {
        routine.getWeek1()
            .getMonday()
            .addProcedure(new Procedure("Monday 1", new LocalTime(10, 0)));

        DateTime next = routine.getNextProcedureTime(now);

        assertEquals(new DateTime(2024, 1, 1, 10, 0), next);
    }

    @Test
    public void getNextProcedureTime_Monday12Week1AndNowIsDay1_returnCorrectTime()
    {
        routine.getWeek1()
            .getMonday()
            .addProcedure(new Procedure("Monday 1", new LocalTime(12, 0)));

        DateTime next = routine.getNextProcedureTime(now);

        assertEquals(new DateTime(2024, 1, 1, 12, 0), next);
    }

    @Test
    public void getNextProcedureTime_Tuesday15Week1AndNowIsDay1_returnCorrectTime()
    {
        routine.getWeek1()
            .getTuesday()
            .addProcedure(new Procedure("Tuesday 1", new LocalTime(15, 0)));

        DateTime next = routine.getNextProcedureTime(now);

        assertEquals(new DateTime(2024, 1, 2, 15, 0), next);
    }

    @Test
    public void getNextProcedureTime_Wednesday16Week2AndNowIsDay1_returnCorrectTime()
    {
        routine.getWeek2()
            .getWednesday()
            .addProcedure(new Procedure("Wednesday 1", new LocalTime(16, 0)));

        DateTime next = routine.getNextProcedureTime(now);

        assertEquals(new DateTime(2024, 1, 10, 16, 0), next);
    }

    @Test
    public void getNextProcedureTime_Monday10Week1AndSunday10Week2AndNowIsDay3_returnCorrectTime()
    {
        routine.getWeek1()
            .getMonday()
            .addProcedure(new Procedure("Monday 1", new LocalTime(10, 0)));

        routine.getWeek2()
            .getSunday()
            .addProcedure(new Procedure("Sunday 1", new LocalTime(10, 0)));

        DateTime next = routine.getNextProcedureTime(now.plusDays(2));

        assertEquals(new DateTime(2024, 1, 14, 10, 0), next);
    }

    @Test
    public void getNextProcedureTime_Monday10Week1AndSunday10Week2AndNowIsDay12_returnCorrectTime()
    {
        routine.getWeek1()
            .getMonday()
            .addProcedure(new Procedure("Monday 1", new LocalTime(10, 0)));

        routine.getWeek2()
            .getSunday()
            .addProcedure(new Procedure("Sunday 1", new LocalTime(10, 0)));

        DateTime next = routine.getNextProcedureTime(now.plusDays(11));

        assertEquals(new DateTime(2024, 1, 14, 10, 0), next);
    }

    @Test
    public void getNextProcedureTime_Sunday10Week1AndNowIsDay9_returnCorrectTime()
    {
        routine.getWeek1()
            .getSunday()
            .addProcedure(new Procedure("Sunday 1", new LocalTime(10, 0)));

        DateTime next = routine.getNextProcedureTime(now.plusDays(8));

        assertEquals(new DateTime(2024, 1, 21, 10, 0), next);
    }

    @Test
    public void getNextProcedureTime_procedureMon10w1AndNowIsDay1Hour11_returnCorrectTime()
    {
        Procedure procedure = new Procedure("Mon1W1", new LocalTime(10, 0));

        routine.getWeek1().getMonday().addProcedure(procedure);

        PendingProcedure pendingProcedure = routine.getPendingProcedure(now.plusHours(11));

        routine.procedureDone(pendingProcedure);

        DateTime next = routine.getNextProcedureTime(now.plusHours(11));

        assertEquals(new DateTime(2024, 1, 15, 10, 0), next);
    }

    @Test
    public void getNextProcedureTime_procedure_returnCorrectTime()
    {
        DateTime testNow = now.plusDays(11);

        routine.getWeek2().getFriday().addProcedure(new Procedure("Fri1W2", new LocalTime(10, 0)));
        routine.getWeek2().getFriday().addProcedure(new Procedure("Fri2W2", new LocalTime(11, 0)));

        DateTime next1 = routine.getNextProcedureTime(testNow.plusHours(9).plusMinutes(0));
        DateTime next2 = routine.getNextProcedureTime(testNow.plusHours(10).plusMinutes(1));

        assertEquals(new DateTime(2024, 1, 12, 10, 0), next1);
        assertEquals(new DateTime(2024, 1, 12, 11, 0), next2);
    }

    @Test
    public void getPendingProcedures_noProceduresAndNowDay1Hour0_listIsEmpty()
    {
        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(now);

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void getPendingProcedures_proceduresMonday10Week1AndNowDay1Hour0_listIsEmpty()
    {
        routine.getWeek1()
            .getMonday()
            .addProcedure(new Procedure("Monday 1", new LocalTime(10, 0)));

        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(now);

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void getPendingProcedures_proceduresMon10w1AndNowDay2Hour0_listHasProcedureMon10w1()
    {
        routine.getWeek1().getMonday().addProcedure(new Procedure("Mon1W1", new LocalTime(10, 0)));

        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(now.plusDays(1));

        assertEquals(1, pendingProcedures.size());
        assertEquals("Mon1W1", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void getPendingProcedures_proceduresMon10w2AndNowDay2Hour0_listHasProcedureMon10w1()
    {
        routine.getWeek2().getMonday().addProcedure(new Procedure("Mon1W2", new LocalTime(10, 0)));

        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(now.plusDays(1));

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void getPendingProcedures_proceduresMon10w2AndNowDay9Hour0_listHasProcedureMon10w1()
    {
        routine.getWeek2().getMonday().addProcedure(new Procedure("Mon1W2", new LocalTime(10, 0)));

        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(now.plusDays(8));

        assertEquals(1, pendingProcedures.size());
        assertEquals("Mon1W2", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void getPendingProcedures_proceduresMon13w1AndMon10w2AndNowDay16Hour0_listHasProceduresMon13w2Mon10w1()
    {
        routine.getWeek1().getMonday().addProcedure(new Procedure("Mon13W1", new LocalTime(13, 0)));

        routine.getWeek2().getMonday().addProcedure(new Procedure("Mon10W2", new LocalTime(10, 0)));

        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(now.plusDays(15));

        assertEquals(3, pendingProcedures.size());
        assertEquals("Mon13W1", pendingProcedures.get(0).getDescription());
        assertEquals("Mon10W2", pendingProcedures.get(1).getDescription());
        assertEquals("Mon13W1", pendingProcedures.get(2).getDescription());
    }

    @Test
    public void getPendingProcedures_proceduresMon13w1AndMon13w2AndNowDay1Hour15_listHasProceduresMon13w2()
    {
        routine.getWeek1().getMonday().addProcedure(new Procedure("Mon13W1", new LocalTime(13, 0)));
        routine.getWeek2().getMonday().addProcedure(new Procedure("Mon13W2", new LocalTime(13, 0)));

        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(getDateTime(1, 15));

        assertEquals(1, pendingProcedures.size());
        assertEquals("Mon13W1", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void procedureDone_procedureMon15w1IsDoneAndNowIsDay2Hour0_pendingProceduresIsEmpty()
    {
        Procedure procedure = new Procedure("Mon1W1", new LocalTime(15, 0));

        routine.getWeek1().getMonday().addProcedure(procedure);

        PendingProcedure pendingProcedure = routine.getPendingProcedure(now.plusDays(1));

        routine.procedureDone(pendingProcedure);

        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(now.plusDays(1));

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void procedureDone_proceduresMon15w1IsDoneMon20w1AndNowIsDay1Hour17_pendingProceduresIsEmpty()
    {
        Procedure procedure1 = new Procedure("Mon1W1", new LocalTime(15, 0));
        Procedure procedure2 = new Procedure("Mon2W1", new LocalTime(20, 0));

        routine.getWeek1().getMonday().addProcedure(procedure1);
        routine.getWeek1().getMonday().addProcedure(procedure2);

        PendingProcedure pendingProcedure = routine.getPendingProcedure(now.plusHours(17));

        routine.procedureDone(pendingProcedure);

        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(now.plusHours(17));

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void procedureDone_proceduresMon15w1IsDoneMon20w1AndNowIsDay1Hour23_pendingProceduresHasMon2w1()
    {
        Procedure procedure1 = new Procedure("Mon1W1", new LocalTime(15, 0));
        Procedure procedure2 = new Procedure("Mon2W1", new LocalTime(20, 0));

        routine.getWeek1().getMonday().addProcedure(procedure1);
        routine.getWeek1().getMonday().addProcedure(procedure2);

        PendingProcedure pendingProcedure = routine.getPendingProcedure(now.plusHours(22));

        routine.procedureDone(pendingProcedure);

        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(now.plusHours(22));

        assertEquals(1, pendingProcedures.size());
        assertEquals("Mon2W1", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void procedureDone_proceduresMon15w2IsDoneMon20w2AndNowIsDay8Hour23_pendingProceduresHasMon2w2()
    {
        Procedure procedure1 = new Procedure("Mon2W2", new LocalTime(15, 0));
        Procedure procedure2 = new Procedure("Mon2W2", new LocalTime(20, 0));

        routine.getWeek2().getMonday().addProcedure(procedure1);
        routine.getWeek2().getMonday().addProcedure(procedure2);

        PendingProcedure pendingProcedure = routine.getPendingProcedure(getDateTime(8, 22));

        routine.procedureDone(pendingProcedure);

        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(getDateTime(8, 22));

        assertEquals(1, pendingProcedures.size());
        assertEquals("Mon2W2", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void procedureDone_proceduresMon15w1IsDoneMon10w2AndNowIsDay9Hour0_pendingProceduresHasMon1w2()
    {
        Procedure procedure1 = new Procedure("Mon1W1", new LocalTime(15, 0));
        Procedure procedure2 = new Procedure("Mon1W2", new LocalTime(10, 0));

        routine.getWeek1().getMonday().addProcedure(procedure1);
        routine.getWeek2().getMonday().addProcedure(procedure2);

        PendingProcedure pendingProcedure = routine.getPendingProcedure(getDateTime(9, 0));

        routine.procedureDone(pendingProcedure);

        List<PendingProcedure> pendingProcedures = routine.getPendingProcedures(getDateTime(9, 0));

        assertEquals(1, pendingProcedures.size());
        assertEquals("Mon1W2", pendingProcedures.get(0).getDescription());
    }

    @Test
    public void procedureDone_proceduresSun22w1Mon10w2AndNowIsDay7Hour23_getNextProcedureTimeIsMon10w2()
    {
        Procedure procedure1 = new Procedure("Sun22W1", new LocalTime(22, 0));
        Procedure procedure2 = new Procedure("Mon10W2", new LocalTime(10, 0));

        routine.getWeek1().getSunday().addProcedure(procedure1);
        routine.getWeek2().getMonday().addProcedure(procedure2);

        PendingProcedure pendingProcedure = routine.getPendingProcedure(getDateTime(7, 23));

        routine.procedureDone(pendingProcedure);

        DateTime nextProcedureTime = routine.getNextProcedureTime(getDateTime(7, 23));

        assertEquals(getDateTime(8, 10), nextProcedureTime);
    }

    @Test
    public void procedureDone_proceduresSun22w2Mon10w1AndNowIsDay15Hour1_getNextProcedureTimeIsMon10w2()
    {
        Procedure procedure1 = new Procedure("Sun22W1", new LocalTime(22, 0));
        Procedure procedure2 = new Procedure("Mon10W2", new LocalTime(10, 0));

        routine.getWeek2().getSunday().addProcedure(procedure1);
        routine.getWeek1().getMonday().addProcedure(procedure2);

        PendingProcedure pendingProcedure = routine.getPendingProcedure(getDateTime(14, 23));

        routine.procedureDone(pendingProcedure);

        DateTime nextProcedureTime = routine.getNextProcedureTime(getDateTime(14, 23));

        assertEquals(getDateTime(15, 10), nextProcedureTime);
    }

    @Test
    public void procedureDone_proceduresSun23w1Mon10w2AndNowIsDay8Hour1_getNextProcedureTimeIsMon10w2()
    {
        Procedure procedure1 = new Procedure("Sun23W1", new LocalTime(23, 0));
        Procedure procedure2 = new Procedure("Mon10W2", new LocalTime(10, 0));

        routine.getWeek1().getSunday().addProcedure(procedure1);
        routine.getWeek2().getMonday().addProcedure(procedure2);

        PendingProcedure pendingProcedure = routine.getPendingProcedure(getDateTime(8, 1));

        routine.procedureDone(pendingProcedure);

        DateTime nextProcedureTime = routine.getNextProcedureTime(getDateTime(8, 1));

        assertEquals(getDateTime(8, 10), nextProcedureTime);
    }

    @Test
    public void procedureDone_proceduresSun23w2Mon10w1AndNowIsDay15Hour1_getNextProcedureTimeIsMon10w2()
    {
        Procedure procedure1 = new Procedure("Sun23W1", new LocalTime(23, 0));
        Procedure procedure2 = new Procedure("Mon10W2", new LocalTime(10, 0));

        routine.getWeek2().getSunday().addProcedure(procedure1);
        routine.getWeek1().getMonday().addProcedure(procedure2);

        PendingProcedure pendingProcedure = routine.getPendingProcedure(getDateTime(15, 1));

        routine.procedureDone(pendingProcedure);

        DateTime nextProcedureTime = routine.getNextProcedureTime(getDateTime(15, 1));

        assertEquals(getDateTime(15, 10), nextProcedureTime);
    }

    @NonNull
    private DateTime getDateTime(int day, int hours)
    {
        return now.plusDays(day - 1).plusHours(hours);
    }
}
