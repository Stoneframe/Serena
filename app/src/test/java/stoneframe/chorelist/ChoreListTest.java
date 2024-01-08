package stoneframe.chorelist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.Container;
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

    private ChoreList choreList;

    @Before
    public void before()
    {
        MockTimeService timeService = new MockTimeService(TODAY);

        choreList = new ChoreList(
            new MockStorage(),
            timeService,
            new SimpleEffortTracker(MAX_EFFORT),
            new SimpleChoreSelector());

        choreList.load();
    }

    @Test
    public void GetAllChores_NoChoresAdded_ReturnEmptyList()
    {
        assertAllChoresIsEmpty();
    }

    @Test
    public void GetAllChores_ChoresAddedInNonAlphabeticOrder_GetAllChoresReturnsInAlphabeticOrder()
    {
        addChore("C Chore", TODAY);
        addChore("A Chore", TODAY);
        addChore("B Chore", TODAY);
        assertAllChoresEquals("A Chore", "B Chore", "C Chore");
    }

    @Test
    public void AddChore_AddSingleChore_GetAllChoresContainsChore()
    {
        addChore("Chore", TODAY);
        assertAllChoresEquals("Chore");
    }

    @Test
    public void AddChore_ChoreAddedWithTodaysDate_TodaysChoresContainsAddedChore()
    {
        addChore("ChoreForToday", TODAY);
        assertTodaysChoresEquals("ChoreForToday");
    }

    @Test
    public void AddChore_ChoreAddedWithYesterdaysDate_TodaysChoresContainsAddedChore()
    {
        addChore("ChoreForYesterday", YESTERDAY);
        assertTodaysChoresEquals("ChoreForYesterday");
    }

    @Test
    public void AddChore_ChoreAddedWithTomorrowsDate_TodaysChoresContainsAddedChore()
    {
        addChore("ChoreForTomorrow", TOMORROW);
        assertTodaysChoresIsEmpty();
    }

    @Test
    public void RemoveChore_RemoveExistingChore_GetAllChoresDoesNotContainChore()
    {
        addChore("Chore1", TODAY);
        addChore("Chore2", TODAY);
        removeChore("Chore1");
        assertAllChoresEquals("Chore2");
    }

    @Test
    public void GetTodaysChores_NoChoresAdded_ReturnEmptyList()
    {
        assertTodaysChoresIsEmpty();
    }

    @Test
    public void GetTodaysChore_MoreChoresThanEffort_ReturnChoresThatMatchEffort1()
    {
        setRemainingEffort(7);
        addChore("Chore1", TODAY, 5);
        addChore("Chore2", TODAY, 5);
        addChore("Chore3", TODAY, 5);
        assertTodaysChoresEquals("Chore1", "Chore2");
    }

    @Test
    public void GetTodaysChore_MoreChoresThanEffort_ReturnChoresThatMatchEffort2()
    {
        setRemainingEffort(10);
        addChore("Chore1", TODAY, 5);
        addChore("Chore2", TODAY, 5);
        addChore("Chore3", TODAY, 5);
        assertTodaysChoresEquals("Chore1", "Chore2");
    }

    @Test
    public void GetTodaysChore_MoreChoresThanEffort_ReturnChoresThatMatchEffort3()
    {
        setRemainingEffort(13);
        addChore("Chore1", TODAY, 5);
        addChore("Chore2", TODAY, 5);
        addChore("Chore3", TODAY, 5);
        assertTodaysChoresEquals("Chore1", "Chore2", "Chore3");
    }

    @Test
    public void GetTodaysChores_OneChoreTomorrowAndOneChoreCompletedAndOneChoreRemoved_ReturnSingleRemainingChore()
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
    public void ChoreDone_ChoreForEveryDay_RescheduleChoreForTomorrow()
    {
        addChore("everyDayChore", TODAY, 1, Chore.DAYS);
        completeChore("everyDayChore");
        assertChore("everyDayChore", c -> c.getNext().equals(TOMORROW));
    }

    @Test
    public void ChoreDone_ChoreForEveryOtherDay_RescheduleChoreForTheDayAfterTomorrow()
    {
        addChore("everyOtherDayChore", TODAY, 2, Chore.DAYS);
        completeChore("everyOtherDayChore");
        assertChore("everyOtherDayChore", c -> c.getNext().equals(TOMORROW.plusDays(1)));
    }

    @Test
    public void ChoreDone_ChoreForEveryWeek_RescheduleChoreForNextWeek()
    {
        addChore("everyWeekChore", TODAY, 1, Chore.WEEKS);
        completeChore("everyWeekChore");
        assertChore("everyWeekChore", c -> c.getNext().equals(TODAY.plusWeeks(1)));
    }

    @Test
    public void ChoreDone_OneChoreDone_ReturnLessRemainingEffort()
    {
        setRemainingEffort(10);
        addChore("Chore", TODAY, 5);
        completeChore("Chore");
        assertRemainingEffortIs(5);
    }

    @Test
    public void ChoreSkip_SkipChore_TodaysChoreDoesNotContainSkippedChore()
    {
        addChore("Chore1", TODAY);
        addChore("Chore2", TODAY);
        skipChore("Chore1");
        assertTodaysChoresEquals("Chore2");
    }

    @Test
    public void ChoreSkip_SkipChore_RemainingEffortIsUnchanged()
    {
        setRemainingEffort(10);
        addChore("Chore1", TODAY, 5);
        addChore("Chore2", TODAY, 5);
        skipChore("Chore2");
        assertRemainingEffortIs(10);
    }

    @Test
    public void ChoreSkip_SkipChore_ChoreRescheduled()
    {
        setRemainingEffort(10);
        addChore("Chore1", TODAY, 3, Chore.DAYS);
        addChore("Chore2", TODAY);
        skipChore("Chore1");
        assertChore("Chore1", c -> c.getNext().equals(TODAY.plusDays(3)));
    }

    @Test
    public void GetRemainingEffort_NoChoreDone_ReturnMaxEffort()
    {
        assertRemainingEffortIs(MAX_EFFORT);
    }

    @Test
    public void GetAllTasks_NoTasksAdded_ReturnEmptyList()
    {
        assertAllTasksIsEmpty();
    }

    @Test
    public void GetAllTasks_TasksAddedInNonDeadlineOrder_GetAllTasksReturnsInDeadlineOrder()
    {
        addTask("A Task", TOMORROW);
        addTask("B Task", YESTERDAY);
        addTask("C Task", TODAY);
        assertAllTasksEquals(false, "B Task", "C Task", "A Task");
    }

    @Test
    public void GetAllTasks_TaskCompletedAndDoNotIncludeCompletedTasks_GetAllTasksDoesNotContainCompletedTasks()
    {
        addTask("A Task", YESTERDAY);
        addTask("B Task", TODAY);
        addTask("C Task", TOMORROW);
        completeTask("C Task");
        assertAllTasksEquals(false, "A Task", "B Task");
    }

    @Test
    public void AddTask_AddSingleChore_GetAllTasksContainsTask()
    {
        addTask("Task");
        assertAllTasksEquals(false, "Task");
    }

    @Test
    public void RemoveTask_RemoveExistingTask_GetAllTasksDoesNotContainRemovedTask()
    {
        addTask("Task1");
        addTask("Task2");
        removeTask("Task1");
        assertAllTasksEquals(false, "Task2");
    }

    @Test
    public void GetTodaysTasks_NoTasksAdded_ReturnsEmptyList()
    {
        assertTodaysTasksIsEmpty();
    }

    @Test
    public void GetTodaysTasks_TasksAddedInNonDeadlineOrder_GetTodaysTasksReturnsInDeadlineOrder()
    {
        addTask("A Task", TOMORROW);
        addTask("B Task", YESTERDAY);
        addTask("C Task", TODAY);
        assertTodaysTasksEquals("B Task", "C Task", "A Task");
    }

    @Test
    public void GetTodaysTasks_TasksWithIgnoreBefore_GetTodaysTasksDoesNotContainIgnoredTask()
    {
        addTask("A Task", YESTERDAY, YESTERDAY);
        addTask("B Task", TODAY, TODAY);
        addTask("C Task", TOMORROW, TOMORROW);
        assertTodaysTasksEquals("A Task", "B Task");
    }

    @Test
    public void GetTodaysTasks_CompletedTask_GetTodaysTasksDoesNotContainCompletedTask()
    {
        addTask("A Task", YESTERDAY);
        addTask("B Task", TODAY);
        addTask("C Task", TOMORROW);
        completeTask("B Task");
        assertTodaysTasksEquals("A Task", "C Task");
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
        String description, DateTime next, int intervalLength, int intervalUnit, int effort)
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

    private static class MockTimeService implements TimeService
    {
        @NonNull
        private final DateTime now;

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
