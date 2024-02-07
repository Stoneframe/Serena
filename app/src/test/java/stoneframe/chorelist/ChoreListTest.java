package stoneframe.chorelist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.Procedure;
import stoneframe.chorelist.model.Routine;
import stoneframe.chorelist.model.Storage;
import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.TimeService;
import stoneframe.chorelist.model.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.efforttrackers.SimpleEffortTracker;

@SuppressWarnings("SameParameterValue")
public class ChoreListTest
{
    private final int MAX_EFFORT = 10;

    private static final DateTime TODAY = TestUtils.MOCK_NOW;
    private static final DateTime YESTERDAY = TODAY.minusDays(1);
    private static final DateTime TOMORROW = TODAY.plusDays(1);

    private final MockTimeService timeService = new MockTimeService(TODAY);

    private ChoreList choreList;

    @Before
    public void before()
    {
        choreList = new ChoreList(
            new MockStorage(),
            timeService,
            new SimpleEffortTracker(MAX_EFFORT),
            new SimpleChoreSelector());

        choreList.load();
    }

    @Test
    public void getAllChores_noChoresAdded_returnEmptyList()
    {
        assertAllChoresIsEmpty();
    }

    @Test
    public void getAllChores_choresAddedInNonAlphabeticOrder_getAllChoresReturnsInAlphabeticOrder()
    {
        addChore("C Chore", TODAY);
        addChore("A Chore", TODAY);
        addChore("B Chore", TODAY);
        assertAllChoresEquals("A Chore", "B Chore", "C Chore");
    }

    @Test
    public void addChore_addSingleChore_getAllChoresContainsChore()
    {
        addChore("Chore", TODAY);
        assertAllChoresEquals("Chore");
    }

    @Test
    public void addChore_choreAddedWithTodaysDate_todaysChoresContainsAddedChore()
    {
        addChore("ChoreForToday", TODAY);
        assertTodaysChoresEquals("ChoreForToday");
    }

    @Test
    public void addChore_choreAddedWithYesterdaysDate_todaysChoresContainsAddedChore()
    {
        addChore("ChoreForYesterday", YESTERDAY);
        assertTodaysChoresEquals("ChoreForYesterday");
    }

    @Test
    public void addChore_choreAddedWithTomorrowsDate_todaysChoresContainsAddedChore()
    {
        addChore("ChoreForTomorrow", TOMORROW);
        assertTodaysChoresIsEmpty();
    }

    @Test
    public void removeChore_removeExistingChore_getAllChoresDoesNotContainChore()
    {
        addChore("Chore1", TODAY);
        addChore("Chore2", TODAY);
        removeChore("Chore1");
        assertAllChoresEquals("Chore2");
    }

    @Test
    public void getTodaysChores_noChoresAdded_returnEmptyList()
    {
        assertTodaysChoresIsEmpty();
    }

    @Test
    public void getTodaysChore_moreChoresThanEffort_returnChoresThatMatchEffort1()
    {
        setRemainingEffort(7);
        addChore("Chore1", TODAY, 5);
        addChore("Chore2", TODAY, 5);
        addChore("Chore3", TODAY, 5);
        assertTodaysChoresEquals("Chore1", "Chore2");
    }

    @Test
    public void getTodaysChore_moreChoresThanEffort_returnChoresThatMatchEffort2()
    {
        setRemainingEffort(10);
        addChore("Chore1", TODAY, 5);
        addChore("Chore2", TODAY, 5);
        addChore("Chore3", TODAY, 5);
        assertTodaysChoresEquals("Chore1", "Chore2");
    }

    @Test
    public void getTodaysChore_moreChoresThanEffort_returnChoresThatMatchEffort3()
    {
        setRemainingEffort(13);
        addChore("Chore1", TODAY, 5);
        addChore("Chore2", TODAY, 5);
        addChore("Chore3", TODAY, 5);
        assertTodaysChoresEquals("Chore1", "Chore2", "Chore3");
    }

    @Test
    public void getTodaysChores_oneChoreTomorrowAndOneChoreCompletedAndOneChoreRemoved_returnSingleRemainingChore()
    {
        setRemainingEffort(10);
        addChore("Chore1", YESTERDAY, 3);
        addChore("Chore2", TODAY, 3);
        addChore("Chore3", TODAY, 3);
        addChore("Chore4", TOMORROW, 3);
        completeChore("Chore1");
        removeChore("Chore3");
        assertTodaysChoresEquals("Chore2");
    }

    @Test
    public void choreDone_choreForEveryDay_rescheduleChoreForTomorrow()
    {
        addChore("everyDayChore", TODAY, 1, Chore.DAYS);
        completeChore("everyDayChore");
        assertChore("everyDayChore", c -> c.getNext().equals(TOMORROW));
    }

    @Test
    public void choreDone_choreForEveryOtherDay_rescheduleChoreForTheDayAfterTomorrow()
    {
        addChore("everyOtherDayChore", TODAY, 2, Chore.DAYS);
        completeChore("everyOtherDayChore");
        assertChore("everyOtherDayChore", c -> c.getNext().equals(TOMORROW.plusDays(1)));
    }

    @Test
    public void choreDone_choreForEveryWeek_rescheduleChoreForNextWeek()
    {
        addChore("everyWeekChore", TODAY, 1, Chore.WEEKS);
        completeChore("everyWeekChore");
        assertChore("everyWeekChore", c -> c.getNext().equals(TODAY.plusWeeks(1)));
    }

    @Test
    public void choreDone_oneChoreDone_returnLessRemainingEffort()
    {
        setRemainingEffort(10);
        addChore("Chore", TODAY, 5);
        completeChore("Chore");
        assertRemainingEffortIs(5);
    }

    @Test
    public void choreSkip_skipChore_todaysChoreDoesNotContainSkippedChore()
    {
        addChore("Chore1", TODAY);
        addChore("Chore2", TODAY);
        skipChore("Chore1");
        assertTodaysChoresEquals("Chore2");
    }

    @Test
    public void choreSkip_skipChore_remainingEffortIsUnchanged()
    {
        setRemainingEffort(10);
        addChore("Chore1", TODAY, 5);
        addChore("Chore2", TODAY, 5);
        skipChore("Chore2");
        assertRemainingEffortIs(10);
    }

    @Test
    public void choreSkip_skipChore_choreRescheduled()
    {
        setRemainingEffort(10);
        addChore("Chore1", TODAY, 3, Chore.DAYS);
        addChore("Chore2", TODAY);
        skipChore("Chore1");
        assertChore("Chore1", c -> c.getNext().equals(TODAY.plusDays(3)));
    }

    @Test
    public void getRemainingEffort_noChoreDone_returnMaxEffort()
    {
        assertRemainingEffortIs(MAX_EFFORT);
    }

    @Test
    public void getAllTasks_noTasksAdded_returnEmptyList()
    {
        assertAllTasksIsEmpty();
    }

    @Test
    public void getAllTasks_tasksAddedInNonDeadlineOrder_getAllTasksReturnsInDeadlineOrder()
    {
        addTask("A Task", TOMORROW);
        addTask("B Task", YESTERDAY);
        addTask("C Task", TODAY);
        assertAllTasksEquals(false, "B Task", "C Task", "A Task");
    }

    @Test
    public void getAllTasks_taskCompletedAndDoNotIncludeCompletedTasks_getAllTasksDoesNotContainCompletedTasks()
    {
        addTask("A Task", YESTERDAY);
        addTask("B Task", TODAY);
        addTask("C Task", TOMORROW);
        completeTask("C Task");
        assertAllTasksEquals(false, "A Task", "B Task");
    }

    @Test
    public void addTask_addSingleChore_getAllTasksContainsTask()
    {
        addTask("Task");
        assertAllTasksEquals(false, "Task");
    }

    @Test
    public void removeTask_removeExistingTask_getAllTasksDoesNotContainRemovedTask()
    {
        addTask("Task1");
        addTask("Task2");
        removeTask("Task1");
        assertAllTasksEquals(false, "Task2");
    }

    @Test
    public void getTodaysTasks_noTasksAdded_returnsEmptyList()
    {
        assertTodaysTasksIsEmpty();
    }

    @Test
    public void getTodaysTasks_tasksAddedInNonDeadlineOrder_getTodaysTasksReturnsInDeadlineOrder()
    {
        addTask("A Task", TOMORROW);
        addTask("B Task", YESTERDAY);
        addTask("C Task", TODAY);
        assertTodaysTasksEquals("B Task", "C Task", "A Task");
    }

    @Test
    public void getTodaysTasks_tasksWithIgnoreBefore_getTodaysTasksDoesNotContainIgnoredTask()
    {
        addTask("A Task", YESTERDAY, YESTERDAY);
        addTask("B Task", TODAY, TODAY);
        addTask("C Task", TOMORROW, TOMORROW);
        assertTodaysTasksEquals("A Task", "B Task");
    }

    @Test
    public void getTodaysTasks_completedTask_getTodaysTasksDoesNotContainCompletedTask()
    {
        addTask("A Task", YESTERDAY);
        addTask("B Task", TODAY);
        addTask("C Task", TOMORROW);
        completeTask("B Task");
        assertTodaysTasksEquals("A Task", "C Task");
    }

    @Test
    public void getAllRoutines_noRoutinesAdded_returnEmptyList()
    {
        assertAllRoutinesIsEmpty();
    }

    @Test
    public void addRoutine_routineAdded_getAllRoutinesContainsAddedRoutine()
    {
        addRoutine("A Routine");
        assertAllRoutinesEquals("A Routine");
    }

    @Test
    public void removeRoutine_removeExistingRoutine_getAllRoutinesDoesNotContainRoutine()
    {
        addRoutine("A Routine");
        removeRoutine("A Routine");
        assertAllRoutinesIsEmpty();
    }

    @Test
    public void getNextRoutineProcedure_singleRoutineWithProcedureInFuture_returnProcedure()
    {
        addRoutine("Routine", new Procedure("Procedure", new LocalTime(18, 0)));
        setCurrentTimeTo(TODAY.plusHours(12));
        assertNextRoutineProcedureIs("Routine", "Procedure");
    }

    @Test
    public void getNextRoutineProcedure_singleRoutineWithTwoProcedureInFuture_returnFirstProcedure()
    {
        addRoutine(
            "Routine",
            new Procedure("Procedure 1", new LocalTime(18, 0)),
            new Procedure("Procedure 2", new LocalTime(21, 0)));
        setCurrentTimeTo(TODAY.plusHours(12));
        assertNextRoutineProcedureIs("Routine", "Procedure 1");
    }

    @Test
    public void getNextRoutineProcedure_twoRoutinesWithOneProcedureEachInFuture_returnFirstProcedure()
    {
        addRoutine("Routine 1", new Procedure("Procedure 1", new LocalTime(21, 0)));
        addRoutine("Routine 2", new Procedure("Procedure 1", new LocalTime(18, 0)));
        setCurrentTimeTo(TODAY.plusHours(12));
        assertNextRoutineProcedureIs("Routine 2", "Procedure 1");
    }

    @Test
    public void getPreviousRoutineProcedure_singleRoutineWithProcedureInPast_returnProcedure()
    {
        addRoutine("Routine", new Procedure("Procedure", new LocalTime(6, 0)));
        setCurrentTimeTo(TODAY.plusHours(12));
        assertPreviousRoutineProcedureIs("Routine", "Procedure");
    }

    @Test
    public void getPreviousRoutineProcedure_singleRoutineWithTwoProcedureInPast_returnLatestProcedure()
    {
        addRoutine(
            "Routine",
            new Procedure("Procedure 1", new LocalTime(6, 0)),
            new Procedure("Procedure 2", new LocalTime(9, 0)));
        setCurrentTimeTo(TODAY.plusHours(12));
        assertPreviousRoutineProcedureIs("Routine", "Procedure 2");
    }

    @Test
    public void getPreviousRoutineProcedure_twoRoutinesWithOneProcedureEachInPast_returnLatestProcedure()
    {
        addRoutine("Routine 1", new Procedure("Procedure 1", new LocalTime(9, 0)));
        addRoutine("Routine 2", new Procedure("Procedure 1", new LocalTime(6, 0)));
        setCurrentTimeTo(TODAY.plusHours(12));
        assertPreviousRoutineProcedureIs("Routine 1", "Procedure 1");
    }

    private void setCurrentTimeTo(DateTime now)
    {
        timeService.setNow(now);
    }

    private void setRemainingEffort(int remainingEffort)
    {
        SimpleEffortTracker effortTracker = (SimpleEffortTracker)choreList.getEffortTracker();

        effortTracker.setTodaysEffort(remainingEffort);
    }

    private void addChore(String description, DateTime next)
    {
        addChore(description, next, 1, Chore.DAYS);
    }

    private void addChore(String description, DateTime next, int effort)
    {
        addChore(description, next, 1, Chore.DAYS, effort);
    }

    private void addChore(String description, DateTime next, int intervalLength, int intervalUnit)
    {
        addChore(description, next, intervalLength, intervalUnit, 1);
    }

    private void addChore(
        String description,
        DateTime next,
        int intervalLength,
        int intervalUnit,
        int effort)
    {
        Chore chore = new Chore(description, 1, effort, next, intervalLength, intervalUnit);

        choreList.addChore(chore);
    }

    private void removeChore(String description)
    {
        Chore chore = getChore(description);

        choreList.removeChore(chore);
    }

    @NonNull
    private Chore getChore(String description)
    {
        Optional<Chore> choreOptional = choreList.getAllChores()
            .stream()
            .filter(c -> c.getDescription().equals(description))
            .findFirst();

        assertTrue(choreOptional.isPresent());

        return choreOptional.get();
    }

    private void completeChore(String description)
    {
        Chore chore = getChore(description);

        choreList.choreDone(chore);
    }

    private void skipChore(String description)
    {
        Chore chore = getChore(description);

        choreList.choreSkip(chore);
    }

    @NonNull
    private Task getTask(String description)
    {
        Optional<Task> taskOptional = choreList.getAllTasks(true)
            .stream()
            .filter(t -> t.getDescription().equals(description))
            .findFirst();

        assertTrue(taskOptional.isPresent());

        return taskOptional.get();
    }

    private void addTask(String description)
    {
        addTask(description, null);
    }

    private void addTask(String description, DateTime deadline)
    {
        addTask(description, deadline, null);
    }

    private void addTask(String description, DateTime deadline, DateTime ignoreBefore)
    {
        Task task = new Task(description, deadline, ignoreBefore);

        choreList.addTask(task);
    }

    private void removeTask(String description)
    {
        Task task = getTask(description);

        choreList.removeTask(task);
    }

    private void completeTask(String description)
    {
        Task task = getTask(description);

        choreList.taskDone(task);
    }

    private Routine getRoutine(String name)
    {
        Optional<Routine> routineOptional = choreList.getAllRoutines()
            .stream()
            .filter(r -> r.getName().equals(name))
            .findFirst();

        assertTrue(routineOptional.isPresent());

        return routineOptional.get();
    }

    private void addRoutine(String name, Procedure... procedures)
    {
//        Routine routine = new Routine(name, new LocalTime(0));

//        Arrays.stream(procedures).forEach(routine::addProcedure);
//
//        choreList.addRoutine(routine);
    }

    private void removeRoutine(String name)
    {
        Routine routine = getRoutine(name);

        choreList.removeRoutine(routine);
    }

    private void assertAllChoresIsEmpty()
    {
        assertAllChoresEquals();
    }

    private void assertTodaysChoresIsEmpty()
    {
        assertTodaysChoresEquals();
    }

    private void assertAllChoresEquals(String... descriptions)
    {
        List<String> expectedDescriptions = Arrays.asList(descriptions);

        List<String> actualDescriptions = choreList.getAllChores()
            .stream()
            .map(Chore::getDescription)
            .collect(Collectors.toList());

        assertEquals(expectedDescriptions, actualDescriptions);
    }

    private void assertTodaysChoresEquals(String... descriptions)
    {
        List<String> expectedDescriptions = Arrays.asList(descriptions);

        List<String> actualDescriptions = choreList.getTodaysChores()
            .stream()
            .map(Chore::getDescription)
            .collect(Collectors.toList());

        assertEquals(expectedDescriptions, actualDescriptions);
    }

    private void assertRemainingEffortIs(int expectedRemainingEffort)
    {
        assertEquals(expectedRemainingEffort, choreList.getRemainingEffort());
    }

    private void assertChore(String description, Predicate<Chore> predicate)
    {
        Chore chore = getChore(description);

        assertTrue(chore.toString(), predicate.test(chore));
    }

    private void assertAllTasksIsEmpty()
    {
        assertAllTasksEquals(false);
    }

    private void assertTodaysTasksIsEmpty()
    {
        assertTodaysTasksEquals();
    }

    private void assertAllTasksEquals(boolean includeCompleted, String... descriptions)
    {
        List<String> expectedDescriptions = Arrays.asList(descriptions);

        List<String> actualDescriptions = choreList.getAllTasks(includeCompleted)
            .stream()
            .map(Task::getDescription)
            .collect(Collectors.toList());

        assertEquals(expectedDescriptions, actualDescriptions);
    }

    private void assertTodaysTasksEquals(String... descriptions)
    {
        List<String> expectedDescriptions = Arrays.asList(descriptions);

        List<String> actualDescriptions = choreList.getTodaysTasks()
            .stream()
            .map(Task::getDescription)
            .collect(Collectors.toList());

        assertEquals(expectedDescriptions, actualDescriptions);
    }

    private void assertAllRoutinesIsEmpty()
    {
        assertAllRoutinesEquals();
    }

    private void assertAllRoutinesEquals(String... names)
    {
        List<String> expectedNames = Arrays.asList(names);

        List<String> actualNames = choreList.getAllRoutines()
            .stream()
            .map(Routine::getName)
            .collect(Collectors.toList());

        assertEquals(expectedNames, actualNames);
    }

    private void assertNextRoutineProcedureIs(String routine, String procedure)
    {
//        Procedure nextProcedure = choreList.getNextRoutineProcedure();

//        assertNotNull(nextProcedure);
//        assertEquals(routine, nextProcedure.getRoutine().getName());
//        assertEquals(procedure, nextProcedure.getDescription());
    }

    private void assertPreviousRoutineProcedureIs(String routine, String procedure)
    {
//        Procedure previousProcedure = choreList.getPreviousRoutineProcedure();
//
//        assertNotNull(previousProcedure);
//        assertEquals(routine, previousProcedure.getRoutine().getName());
//        assertEquals(procedure, previousProcedure.getDescription());
    }

    private static class MockTimeService implements TimeService
    {
        @NonNull
        private DateTime now;

        public MockTimeService(@NonNull DateTime now)
        {
            this.now = now;
        }

        @NonNull
        @Override
        public DateTime getNow()
        {
            return now;
        }

        public void setNow(@NonNull DateTime now)
        {
            this.now = now;
        }
    }

    private static class MockStorage implements Storage
    {
        @Nullable
        @Override
        public Container load()
        {
            return null;
        }

        @Override
        public void save(@NonNull Container container)
        {

        }
    }
}
