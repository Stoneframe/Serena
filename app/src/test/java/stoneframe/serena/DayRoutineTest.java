//package stoneframe.chorelist;
//
//import static junit.framework.TestCase.assertEquals;
//
//import org.joda.time.LocalDateTime;
//import org.joda.time.LocalTime;
//import org.junit.Before;
//import org.junit.Test;
//
//import stoneframe.chorelist.model.routines.DayRoutine;
//import stoneframe.chorelist.model.routines.PendingProcedure;
//import stoneframe.chorelist.model.routines.Procedure;
//
//public class DayRoutineTest
//{
//    private final LocalDateTime now = new LocalDateTime(2024, 1, 1, 12, 0);
//
//    private DayRoutine routine;
//
//    @Before
//    public void before()
//    {
//        routine = new DayRoutine("Day Routing", now);
//    }
//
//    @Test
//    public void procedureDone_testCase1_getNextProcedureReturnsCorrect()
//    {
//        Procedure procedure1 = new Procedure("Time09", new LocalTime(9, 0));
//        Procedure procedure2 = new Procedure("Time14", new LocalTime(14, 0));
//        Procedure procedure3 = new Procedure("Time23", new LocalTime(23, 0));
//
//        routine.addProcedure(procedure1);
//        routine.addProcedure(procedure2);
//        routine.addProcedure(procedure3);
//
//        PendingProcedure pendingProcedure = routine.getPendingProcedure(now.plusHours(20));
//
//        routine.procedureDone(pendingProcedure);
//
//        LocalDateTime nextProcedureTime = routine.getNextProcedureTime(now.plusHours(20));
//
//        assertEquals(
//            now.toLocalDate().plusDays(1).toLocalDateTime(procedure1.getTime()),
//            nextProcedureTime);
//    }
//}
