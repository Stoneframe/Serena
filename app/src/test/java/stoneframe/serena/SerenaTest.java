package stoneframe.serena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import stoneframe.serena.mocks.MockTimeService;
import stoneframe.serena.chores.Chore;
import stoneframe.serena.chores.ChoreEditor;
import stoneframe.serena.chores.IntervalRepetition;
import stoneframe.serena.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.chores.efforttrackers.SimpleEffortTracker;
import stoneframe.serena.routines.DayRoutine;
import stoneframe.serena.routines.DayRoutineEditor;
import stoneframe.serena.routines.Procedure;
import stoneframe.serena.routines.Routine;
import stoneframe.serena.tasks.Task;
import stoneframe.serena.tasks.TaskEditor;

@SuppressWarnings("SameParameterValue")
public class SerenaTest
{
    private static final LocalDate TODAY = new LocalDate(2024, 1, 1);
    private static final LocalDate YESTERDAY = TODAY.minusDays(1);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);

    private final MockTimeService timeService = new MockTimeService(TODAY.toLocalDateTime(LocalTime.MIDNIGHT));

    private final int MAX_EFFORT = 10;

    private Serena serena;

    @Before
    public void before()
    {
        serena = new Serena(
            new MockStorage(),
            timeService,
            new SimpleEffortTracker(MAX_EFFORT),
            new SimpleChoreSelector());

        serena.load();
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
        addChore("everyDayChore", TODAY, 1, IntervalRepetition.DAYS);
        completeChore("everyDayChore");
        assertChore("everyDayChore", c -> c.getNext().equals(TOMORROW));
    }

    @Test
    public void choreDone_choreForEveryOtherDay_rescheduleChoreForTheDayAfterTomorrow()
    {
        addChore("everyOtherDayChore", TODAY, 2, IntervalRepetition.DAYS);
        completeChore("everyOtherDayChore");
        assertChore("everyOtherDayChore", c -> c.getNext().equals(TOMORROW.plusDays(1)));
    }

    @Test
    public void choreDone_choreForEveryWeek_rescheduleChoreForNextWeek()
    {
        addChore("everyWeekChore", TODAY, 1, IntervalRepetition.WEEKS);
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
        addChore("Chore1", TODAY, 3, IntervalRepetition.DAYS);
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

    private void setRemainingEffort(int remainingEffort)
    {
        SimpleEffortTracker effortTracker = (SimpleEffortTracker)serena
            .getChoreManager()
            .getEffortTracker();

        effortTracker.setTodaysEffort(remainingEffort);
    }

    private void addChore(String description, LocalDate next)
    {
        addChore(description, next, 1, IntervalRepetition.DAYS);
    }

    private void addChore(String description, LocalDate next, int effort)
    {
        addChore(description, next, 1, IntervalRepetition.DAYS, effort);
    }

    private void addChore(String description, LocalDate next, int intervalLength, int intervalUnit)
    {
        addChore(description, next, intervalLength, intervalUnit, 1);
    }

    private void addChore(
        String description,
        LocalDate next,
        int intervalLength,
        int intervalUnit,
        int effort)
    {
        Chore chore = serena.getChoreManager().createChore();

        ChoreEditor choreEditor = serena.getChoreManager().getChoreEditor(chore);

        choreEditor.setDescription(description);
        choreEditor.setEffort(effort);

        IntervalRepetition repetition = (IntervalRepetition)choreEditor.getRepetition();

        repetition.setNext(next);
        repetition.setIntervalLength(intervalLength);
        repetition.setIntervalUnit(intervalUnit);

        choreEditor.save();
    }

    private void removeChore(String description)
    {
        Chore chore = getChore(description);

        serena.getChoreManager().getChoreEditor(chore).remove();
    }

    @NonNull
    private Chore getChore(String description)
    {
        Optional<Chore> choreOptional = serena.getChoreManager().getAllChores()
            .stream()
            .filter(c -> c.getDescription().equals(description))
            .findFirst();

        assertTrue(choreOptional.isPresent());

        return choreOptional.get();
    }

    private void completeChore(String description)
    {
        Chore chore = getChore(description);

        serena.getChoreManager().complete(chore);
    }

    private void skipChore(String description)
    {
        Chore chore = getChore(description);

        serena.getChoreManager().skip(chore);
    }

    @NonNull
    private Task getTask(String description)
    {
        Optional<Task> taskOptional = serena.getTaskManager().getAllTasks()
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

    private void addTask(String description, LocalDate deadline)
    {
        addTask(description, deadline, null);
    }

    private void addTask(String description, LocalDate deadline, LocalDate ignoreBefore)
    {
        Task task = serena.getTaskManager().createTask();

        TaskEditor taskEditor = serena.getTaskManager().getTaskEditor(task);

        taskEditor.setDescription(description);
        taskEditor.setDeadline(deadline);
        taskEditor.setIgnoreBefore(ignoreBefore);

        taskEditor.save();
    }

    private void removeTask(String description)
    {
        Task task = getTask(description);

        serena.getTaskManager().getTaskEditor(task).remove();
    }

    private void completeTask(String description)
    {
        Task task = getTask(description);

        serena.getTaskManager().complete(task);
    }

    private Routine<?> getRoutine(String name)
    {
        Optional<Routine<?>> routineOptional = serena.getRoutineManager().getAllRoutines()
            .stream()
            .filter(r -> r.getName().equals(name))
            .findFirst();

        assertTrue(routineOptional.isPresent());

        return routineOptional.get();
    }

    private void addRoutine(String name, Procedure... procedures)
    {
        DayRoutine routine = serena.getRoutineManager().createDayRoutine();

        DayRoutineEditor routineEditor = serena.getRoutineManager().getDayRoutineEditor(routine);

        routineEditor.setName(name);
        Arrays.stream(procedures).forEach(routineEditor::addProcedure);

        routineEditor.save();
    }

    private void removeRoutine(String name)
    {
        Routine<?> routine = getRoutine(name);

        serena.getRoutineManager().getDayRoutineEditor((DayRoutine)routine).remove();
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

        List<String> actualDescriptions = serena.getChoreManager().getAllChores()
            .stream()
            .map(Chore::getDescription)
            .collect(Collectors.toList());

        assertEquals(expectedDescriptions, actualDescriptions);
    }

    private void assertTodaysChoresEquals(String... descriptions)
    {
        List<String> expectedDescriptions = Arrays.asList(descriptions);

        List<String> actualDescriptions = serena.getChoreManager().getTodaysChores()
            .stream()
            .map(Chore::getDescription)
            .collect(Collectors.toList());

        assertEquals(expectedDescriptions, actualDescriptions);
    }

    private void assertRemainingEffortIs(int expectedRemainingEffort)
    {
        assertEquals(expectedRemainingEffort, serena.getChoreManager().getRemainingEffort());
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

        List<String> actualDescriptions = serena.getTaskManager().getAllTasks()
            .stream()
            .filter(t -> includeCompleted || !t.isDone())
            .map(Task::getDescription)
            .collect(Collectors.toList());

        assertEquals(expectedDescriptions, actualDescriptions);
    }

    private void assertTodaysTasksEquals(String... descriptions)
    {
        List<String> expectedDescriptions = Arrays.asList(descriptions);

        List<String> actualDescriptions = serena.getTaskManager().getTodaysTasks()
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

        List<String> actualNames = serena.getRoutineManager().getAllRoutines()
            .stream()
            .map(Routine::getName)
            .collect(Collectors.toList());

        assertEquals(expectedNames, actualNames);
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
        public void save(Container container)
        {

        }

        @Override
        public int getCurrentVersion()
        {
            return 0;
        }
    }
}
