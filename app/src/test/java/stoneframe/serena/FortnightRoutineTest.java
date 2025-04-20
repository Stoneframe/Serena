package stoneframe.serena;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNull;
import static junit.framework.TestCase.assertTrue;

import androidx.annotation.NonNull;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import stoneframe.serena.mocks.TestContext;
import stoneframe.serena.routines.FortnightRoutine;
import stoneframe.serena.routines.FortnightRoutineEditor;
import stoneframe.serena.routines.PendingProcedure;
import stoneframe.serena.routines.Procedure;
import stoneframe.serena.routines.RoutineManager;

/** @noinspection UnnecessaryLocalVariable*/
public class FortnightRoutineTest
{
    private final LocalDate initialDate = new LocalDate(2024, 1, 1);

    private TestContext context;

    private RoutineManager routineManager;

    @Before
    public void before()
    {
        context = new TestContext().setCurrentTime(initialDate);

        routineManager = context.getRoutineManager();
    }

    @Test
    public void getNextProcedureTime_notProceduresAdded_returnNull()
    {
        LocalDateTime next = routineManager.getNextProcedureTime();

        assertNull(next);
    }

    @Test
    public void getNextProcedureTime_getNextBeforeTimeOfProcedure1_returnTimeOfProcedure()
    {
        final LocalDate mondayWeek1 = initialDate;

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(mondayWeek1);

        LocalDateTime next = routineManager.getNextProcedureTime();

        // ASSERT
        assertEquals(t(mondayWeek1, 10), next);
    }

    @Test
    public void getNextProcedureTime_getNextBeforeTimeOfProcedure2_returnTimeOfProcedure()
    {
        final LocalDate mondayWeek1 = initialDate;
        final LocalDate tuesdayWeek1 = initialDate.plusDays(1);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.TUESDAY, new LocalTime(12, 0));

        // ACT
        context.setCurrentTime(mondayWeek1);

        LocalDateTime next = routineManager.getNextProcedureTime();

        // ASSERT
        assertEquals(t(tuesdayWeek1, 12), next);
    }

    @Test
    public void getNextProcedureTime_getNextBeforeTimeOfProcedure3_returnTimeOfProcedure()
    {
        final LocalDate mondayWeek1 = initialDate;
        final LocalDate wednesdayWeek2 = initialDate.plusDays(9);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 2, DateTimeConstants.WEDNESDAY, new LocalTime(16, 0));

        // ACT
        context.setCurrentTime(mondayWeek1);

        LocalDateTime next = routineManager.getNextProcedureTime();

        // ASSERT
        assertEquals(t(wednesdayWeek2, 16), next);
    }

    @Test
    public void getNextProcedureTime_getNextBetweenTwoProceduresTimes1_returnTimeOfSecondProcedure()
    {
        final LocalDate wednesdayWeek1 = initialDate.plusDays(2);
        final LocalDate sundayWeek2 = initialDate.plusDays(13);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));
        addProcedure(routine, 2, DateTimeConstants.SUNDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(wednesdayWeek1);

        LocalDateTime next = routineManager.getNextProcedureTime();

        // ASSERT
        assertEquals(t(sundayWeek2, 10), next);
    }

    @Test
    public void getNextProcedureTime_getNextBetweenTwoProceduresTimes2_returnTimeOfSecondProcedure()
    {
        final LocalDate fridayWeek2 = initialDate.plusDays(2);
        final LocalDate sundayWeek2 = initialDate.plusDays(13);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));
        addProcedure(routine, 2, DateTimeConstants.SUNDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(fridayWeek2);

        LocalDateTime next = routineManager.getNextProcedureTime();

        // ASSERT
        assertEquals(t(sundayWeek2, 10), next);
    }

    @Test
    public void getNextProcedureTime_getNextAfterTimeOfProcedure1_returnTimeOfProcedureTwoWeeksLater()
    {
        final LocalDate tuesdayWeek2 = initialDate.plusDays(8);
        final LocalDate sundayWeek3 = initialDate.plusDays(20);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.SUNDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(tuesdayWeek2);

        LocalDateTime next = routineManager.getNextProcedureTime();

        // ASSERT
        assertEquals(t(sundayWeek3, 10), next);
    }

    @Test
    public void getNextProcedureTime_getNextAfterTimeOfProcedure2_returnTimeOfProcedureTwoWeeksLater()
    {
        final LocalDate mondayWeek1 = initialDate;
        final LocalDate mondayWeek3 = initialDate.plusDays(14);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(t(mondayWeek1, 11));

        LocalDateTime next = routineManager.getNextProcedureTime();

        // ASSERT
        assertEquals(t(mondayWeek3, 10), next);
    }

    @Test
    public void getPendingProcedures_noProceduresAdded_listIsEmpty()
    {
        final LocalDate mondayWeek1 = initialDate;

        // ARRANGE
        context.setCurrentTime(mondayWeek1);

        // ACT
        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void getPendingProcedures_currentTimeIsBeforeProcedureTime1_listIsEmpty()
    {
        final LocalDate mondayWeek1 = initialDate;

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(mondayWeek1);

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void getPendingProcedures_currentTimeIsBeforeProcedureTime2_listIsEmpty()
    {
        final LocalDate tuesdayWeek1 = initialDate.plusDays(1);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 2, DateTimeConstants.MONDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(tuesdayWeek1);

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void getPendingProcedures_currentTimeIsEqualToProcedureTime_listHasProcedure()
    {
        final LocalDate mondayWeek1 = initialDate;

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(t(mondayWeek1, 10));

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertEquals(1, pendingProcedures.size());
        assertEquals(t(mondayWeek1, 10), pendingProcedures.get(0).getDateTime());
    }

    @Test
    public void getPendingProcedures_currentTimeIsAfterProcedureTime_listHasProcedure()
    {
        final LocalDate mondayWeek2 = initialDate.plusDays(7);
        final LocalDate tuesdayWeek2 = initialDate.plusDays(8);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 2, DateTimeConstants.MONDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(tuesdayWeek2);

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertEquals(1, pendingProcedures.size());
        assertEquals(t(mondayWeek2, 10), pendingProcedures.get(0).getDateTime());
    }

    @Test
    public void getPendingProcedures_currentTimeAfterTwoProcedures_listHasBothProcedures()
    {
        final LocalDate mondayWeek1 = initialDate;
        final LocalDate mondayWeek2 = initialDate.plusDays(7);
        final LocalDate wednesdayWeek2 = initialDate.plusDays(9);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));
        addProcedure(routine, 2, DateTimeConstants.MONDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(wednesdayWeek2);

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertEquals(2, pendingProcedures.size());
        assertEquals(t(mondayWeek1, 10), pendingProcedures.get(0).getDateTime());
        assertEquals(t(mondayWeek2, 10), pendingProcedures.get(1).getDateTime());
    }

    @Test
    public void getPendingProcedures_currentTimeTwoWeeksAfterProcedure_listHasProcedureTwice()
    {
        final LocalDate mondayWeek1 = initialDate;
        final LocalDate mondayWeek3 = initialDate.plusDays(14);
        final LocalDate wednesdayWeek3 = initialDate.plusDays(16);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(wednesdayWeek3);

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertEquals(2, pendingProcedures.size());
        assertEquals(t(mondayWeek1, 10), pendingProcedures.get(0).getDateTime());
        assertEquals(t(mondayWeek3, 10), pendingProcedures.get(1).getDateTime());
    }

    @Test
    public void getPendingProcedures_currentTimeBetweenTwoProcedures_listHasFirstProcedures()
    {
        final LocalDate mondayWeek1 = initialDate;
        final LocalDate wednesdayWeek1 = initialDate.plusDays(2);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));
        addProcedure(routine, 2, DateTimeConstants.MONDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(wednesdayWeek1);

        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        // ASSERT
        assertEquals(1, pendingProcedures.size());
        assertEquals(t(mondayWeek1, 10), pendingProcedures.get(0).getDateTime());
    }

    @Test
    public void procedureDone_markPendingProcedureDone_pendingProceduresIsEmpty()
    {
        final LocalDate tuesdayWeek1 = initialDate.plusDays(1);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(tuesdayWeek1);

        PendingProcedure pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void procedureDone_currentTimeIsBetweenTwoProceduresAndMarkFirstAsDone_pendingProceduresIsEmpty()
    {
        final LocalDate mondayWeek1 = initialDate;

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));
        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(20, 0));

        // ACT
        context.setCurrentTime(t(mondayWeek1, 15));

        PendingProcedure pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        assertTrue(pendingProcedures.isEmpty());
    }

    @Test
    public void procedureDone_currentTimeIsAfterTwoProceduresAndMarkFirstAsDone1_pendingProceduresHasSecondProcedure()
    {
        final LocalDate mondayWeek1 = initialDate;

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));
        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(20, 0));

        // ACT
        context.setCurrentTime(t(mondayWeek1, 21));

        PendingProcedure pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        assertEquals(1, pendingProcedures.size());
        assertEquals(t(mondayWeek1, 20), pendingProcedures.get(0).getDateTime());
    }

    @Test
    public void procedureDone_currentTimeIsAfterTwoProceduresAndMarkFirstAsDone2_pendingProceduresHasSecondProcedure()
    {
        final LocalDate mondayWeek2 = initialDate.plusDays(7);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 2, DateTimeConstants.MONDAY, new LocalTime(10, 0));
        addProcedure(routine, 2, DateTimeConstants.MONDAY, new LocalTime(20, 0));

        // ACT
        context.setCurrentTime(t(mondayWeek2, 21));

        PendingProcedure pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        assertEquals(1, pendingProcedures.size());
        assertEquals(t(mondayWeek2, 20), pendingProcedures.get(0).getDateTime());
    }

    @Test
    public void procedureDone_currentTimeIsAfterTwoProceduresAndMarkFirstAsDone3_pendingProceduresHasSecondProcedure()
    {
        final LocalDate mondayWeek2 = initialDate.plusDays(7);
        final LocalDate tuesdayWeek2 = initialDate.plusDays(8);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.MONDAY, new LocalTime(10, 0));
        addProcedure(routine, 2, DateTimeConstants.MONDAY, new LocalTime(20, 0));

        // ACT
        context.setCurrentTime(t(tuesdayWeek2, 21));

        PendingProcedure pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        List<PendingProcedure> pendingProcedures = routineManager.getPendingProcedures();

        assertEquals(1, pendingProcedures.size());
        assertEquals(t(mondayWeek2, 20), pendingProcedures.get(0).getDateTime());
    }

    @Test
    public void procedureDone_currentTimeIsBetweenTwoProcedures1_nextProcedureTimeIsSecondProcedure()
    {
        final LocalDate sundayWeek1 = initialDate.plusDays(6);
        final LocalDate mondayWeek2 = initialDate.plusDays(7);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.SUNDAY, new LocalTime(10, 0));
        addProcedure(routine, 2, DateTimeConstants.MONDAY, new LocalTime(20, 0));

        // ACT
        context.setCurrentTime(t(sundayWeek1, 15));

        PendingProcedure pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        LocalDateTime nextProcedureTime = routineManager.getNextProcedureTime();

        assertEquals(t(mondayWeek2, 20), nextProcedureTime);
    }

    @Test
    public void procedureDone_currentTimeIsAfterProcedure_nextProcedureTimeIsSameProcedureTwoWeeksLater()
    {
        final LocalDate mondayWeek2 = initialDate.plusDays(7);
        final LocalDate sundayWeek3 = initialDate.plusDays(20);

        // ARRANGE
        FortnightRoutine routine = routineManager.createFortnightRoutine();

        addProcedure(routine, 1, DateTimeConstants.SUNDAY, new LocalTime(10, 0));

        // ACT
        context.setCurrentTime(t(mondayWeek2, 15));

        PendingProcedure pendingProcedure = routineManager.getPendingProcedures().get(0);

        routineManager.procedureDone(pendingProcedure);

        // ASSERT
        LocalDateTime nextProcedureTime = routineManager.getNextProcedureTime();

        assertEquals(t(sundayWeek3, 10), nextProcedureTime);
    }

    private void addProcedure(FortnightRoutine routine, int week, int day, LocalTime time)
    {
        FortnightRoutineEditor routineEditor = routineManager.getFortnightRoutineEditor(routine);

        routineEditor.addProcedure(week, day, new Procedure("", time));

        routineEditor.save();
    }

    @NonNull
    private LocalDateTime t(LocalDate day, int hour)
    {
        return day.toLocalDateTime(new LocalTime(hour, 0));
    }
}
