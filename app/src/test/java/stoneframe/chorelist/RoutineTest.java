package stoneframe.chorelist;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class RoutineTest
{
//    @Test
//    public void getAllProcedures_noProceduresAdded_listIsEmpty()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        // ACT
//        List<Procedure> procedures = routine.getAllProcedures();
//
//        // ASSERT
//        assertEquals(procedures, Collections.emptyList());
//    }
//
//    @Test
//    public void getAllProcedures_proceduresAddedInNonChronologicalOrder_listIsSortedChronologically()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        routine.addProcedure(new Procedure("procedure1", new LocalTime(0, 30)));
//        routine.addProcedure(new Procedure("procedure2", new LocalTime(0, 10)));
//        routine.addProcedure(new Procedure("procedure3", new LocalTime(0, 20)));
//
//        // ACT
//        List<Procedure> procedures = routine.getAllProcedures();
//
//        // ASSERT
//        assertEquals("procedure2", procedures.get(0).getDescription());
//        assertEquals("procedure3", procedures.get(1).getDescription());
//        assertEquals("procedure1", procedures.get(2).getDescription());
//    }
//
//    @Test
//    public void addProcedure_procedureIsAdded_getAllProceduresContainsAddedProcedure()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        Procedure procedure = new Procedure("procedure", new LocalTime());
//
//        // ACT
//        routine.addProcedure(procedure);
//
//        // ASSERT
//        assertTrue(routine.getAllProcedures().contains(procedure));
//    }
//
//    @Test
//    public void removeProcedure_existingProcedureIsRemoved_getAllProceduresDoesNotContainRemovedProcedure()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        Procedure procedure = new Procedure("procedure", new LocalTime());
//
//        routine.addProcedure(procedure);
//
//        // ACT
//        routine.removeProcedure(procedure);
//
//        // ASSERT
//        assertFalse(routine.getAllProcedures().contains(procedure));
//    }
//
//    @Test
//    public void getNextProcedureTime_nextProcedureIsInTheFuture_returnNextProcedure()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        routine.addProcedure(new Procedure("procedure", new LocalTime(18, 0)));
//
//        // ACT
//        DateTime nextProcedureTime = routine.getNextProcedureTime(new DateTime(1, 1, 1, 12, 0));
//
//        // ASSERT
//        assertNotNull(nextProcedureTime);
//        assertEquals(new DateTime(1, 1, 1, 18, 0), nextProcedureTime);
//    }
//
//    @Test
//    public void getNextProcedureTime_procedureIsInThePast_returnProcedureTimeNextDay()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        routine.addProcedure(new Procedure("procedure", new LocalTime(6, 0)));
//
//        // ACT
//        DateTime nextProcedureTime = routine.getNextProcedureTime(new DateTime(1, 1, 1, 12, 0));
//
//        // ASSERT
//        assertNotNull(nextProcedureTime);
//        assertEquals(new DateTime(1, 1, 2, 6, 0), nextProcedureTime);
//    }
//
//    @Test
//    public void getPreviousProcedure_procedureIsInThePast_returnPreviousProcedure()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        routine.addProcedure(new Procedure("procedure", new LocalTime(6, 0)));
//
//        // ACT
//        Procedure previosusProcedure = routine.getPreviosusProcedure(new LocalTime(12, 0));
//
//        // ASSERT
//        assertNotNull(previosusProcedure);
//        assertEquals("procedure", previosusProcedure.getDescription());
//    }
//
//    @Test
//    public void getPendingRoutines_uncompletedProcedureInThePast_listContainsProcedure()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        routine.addProcedure(new Procedure("procedure", new LocalTime(6, 0)));
//
//        // ACT
//        List<Procedure> pendingProcedures = routine.getPendingProcedures(new LocalTime(12, 0));
//
//        // ASSERT
//        assertTrue(pendingProcedures.stream()
//            .anyMatch(p -> p.getDescription().equals("procedure")));
//    }
//
//    @Test
//    public void getPendingRoutines_procedureInTheFuture_listIsEmpty()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        routine.addProcedure(new Procedure("procedure", new LocalTime(18, 0)));
//
//        // ACT
//        List<Procedure> pendingProcedures = routine.getPendingProcedures(new LocalTime(12, 0));
//
//        // ASSERT
//        assertTrue(pendingProcedures.isEmpty());
//    }
//
//    @Test
//    public void getPendingRoutines_allProceduresInThePastAndAddedNonChronologically_listSortedChronologically()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        routine.addProcedure(new Procedure("procedure1", new LocalTime(9, 0)));
//        routine.addProcedure(new Procedure("procedure2", new LocalTime(6, 0)));
//        routine.addProcedure(new Procedure("procedure3", new LocalTime(3, 0)));
//
//        // ACT
//        List<Procedure> pendingProcedures = routine.getPendingProcedures(new LocalTime(12, 0));
//
//        // ASSERT
//        assertEquals("procedure3", pendingProcedures.get(0).getDescription());
//        assertEquals("procedure2", pendingProcedures.get(1).getDescription());
//        assertEquals("procedure1", pendingProcedures.get(2).getDescription());
//    }
//
//    @Test
//    public void completeProcedure_procedureInThePastAndIsCompleted_getPendingProceduresIsEmpty()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        Procedure procedure = new Procedure("procedure", new LocalTime(6, 0));
//
//        routine.addProcedure(procedure);
//
//        // ACT
//        routine.completeProcedure(procedure);
//
//        // ASSERT
//        assertTrue(routine.getPendingProcedures(new LocalTime(12, 0)).isEmpty());
//    }
//
//    @Test
//    public void getPendingProcedures_twoProceduresInThePastAndSecondIsCompleted_listIsEmpty()
//    {
//        // ARRANGE
//        Routine routine = new Routine("routine", new LocalTime(0));
//
//        Procedure procedure1 = new Procedure("procedure1", new LocalTime(6, 0));
//        Procedure procedure2 = new Procedure("procedure2", new LocalTime(10, 0));
//
//        routine.addProcedure(procedure1);
//        routine.addProcedure(procedure2);
//
//        routine.completeProcedure(procedure2);
//
//        // ACT
//        List<Procedure> pendingProcedures = routine.getPendingProcedures(new LocalTime(12, 0));
//
//        // ASSERT
//        assertTrue(pendingProcedures.isEmpty());
//    }
}
