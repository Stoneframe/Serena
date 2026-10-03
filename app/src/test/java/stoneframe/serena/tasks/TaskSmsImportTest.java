package stoneframe.serena.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import stoneframe.serena.mocks.MockTimeService;
import stoneframe.serena.tasks.TaskManager.SmsTaskImportResult;

public class TaskSmsImportTest
{
    private static final LocalDate TODAY = new LocalDate(2024, 1, 2);
    private static final String ORIGIN = "+46701234567";
    private static final String BODY = "#Serena\nBuy milk";

    private final TrustedSmsSender ada = new TrustedSmsSender(ORIGIN, "Ada");

    private TaskContainer container;
    private TaskManager manager;
    private MockTimeService timeService;
    private Task existingTask;

    @Before
    public void setUp()
    {
        container = new TaskContainer();
        timeService = new MockTimeService(TODAY.toLocalDateTime(LocalTime.MIDNIGHT));
        manager = new TaskManager(() -> container, timeService);
        manager.setSmsTaskSettings(true, Collections.singletonList(ada));
    }

    @Test
    public void importSmsTask_disabled_preservesExistingTaskData()
    {
        // Arrange
        addExistingTask();

        manager.setSmsTaskSettings(false, Collections.singletonList(ada));

        // Act
        SmsTaskImportResult result = manager.importSmsTask(ORIGIN, BODY, "SE");

        // Assert
        assertRejected(result);
    }

    @Test
    public void importSmsTask_noTrustedSenders_preservesExistingTaskData()
    {
        // Arrange
        addExistingTask();

        manager.setSmsTaskSettings(true, Collections.emptyList());

        // Act
        SmsTaskImportResult result = manager.importSmsTask(ORIGIN, BODY, "SE");

        // Assert
        assertRejected(result);
    }

    @Test
    public void importSmsTask_unmatchedOrInvalidOrigin_preservesExistingTaskData()
    {
        // Arrange
        List<String> origins = Arrays.asList("+46711234567", "not a number", "+999", null);

        addExistingTask();

        // Act
        List<SmsTaskImportResult> results = new ArrayList<>();

        for (String origin : origins)
        {
            results.add(manager.importSmsTask(origin, BODY, "SE"));
        }

        results.add(manager.importSmsTask("0701234567", BODY, null));

        // Assert
        for (SmsTaskImportResult result : results)
        {
            assertRejected(result);
        }
    }

    @Test
    public void importSmsTask_rejectedBody_preservesExistingTaskData()
    {
        // Arrange
        List<String> bodies = Arrays.asList(null, "", "Buy milk", "#Serena\n \t ", "Hello\n#Serena\nBuy milk");

        addExistingTask();

        // Act
        List<SmsTaskImportResult> results = new ArrayList<>();

        for (String body : bodies)
        {
            results.add(manager.importSmsTask(ORIGIN, body, "SE"));
        }

        // Assert
        for (SmsTaskImportResult result : results)
        {
            assertRejected(result);
        }
    }

    @Test
    public void importSmsTask_validMessage_addsAttributedTaskWithNormalDefaults()
    {
        // Arrange
        String body = "\r\n #Serena \r\n Buy milk \r\n2024-1-10\r\nignored";

        // Act
        SmsTaskImportResult result = manager.importSmsTask(ORIGIN, body, "SE");

        // Assert
        Task task = assertCreatedTask(result, "Buy milk", "Ada", TODAY);
        assertEquals(new LocalDate(2024, 1, 10), task.getDeadline());
        assertTrue(task.hasDeadline());
        assertFalse(result.isDeadlineUnreadable());
        assertEquals(1, container.tasks.size());
    }

    @Test
    public void importSmsTask_absentOrBlankDeadline_addsUndatedTaskWithoutWarning()
    {
        // Arrange
        List<String> bodies = Arrays.asList(BODY, BODY + "\n \t ");
        List<SmsTaskImportResult> results = new ArrayList<>();

        // Act
        for (String body : bodies)
        {
            results.add(manager.importSmsTask(ORIGIN, body, "SE"));
        }

        // Assert
        for (SmsTaskImportResult result : results)
        {
            Task task = assertCreatedTask(result, "Buy milk", "Ada", TODAY);

            assertFalse(task.hasDeadline());
            assertEquals(Task.MAXIMUM_DEADLINE, task.getDeadline());
            assertNull(manager.getTaskEditor(task).getDeadline());
            assertFalse(result.isDeadlineUnreadable());
        }

        assertEquals(2, container.tasks.size());
    }

    @Test
    public void importSmsTask_unreadableOrImpossibleDeadline_addsUndatedTaskWithWarning()
    {
        // Arrange
        List<String> deadlines = Arrays.asList("not a date", "2024-02-30");
        List<SmsTaskImportResult> results = new ArrayList<>();

        // Act
        for (String deadline : deadlines)
        {
            results.add(manager.importSmsTask(ORIGIN, BODY + "\n" + deadline, "SE"));
        }

        // Assert
        for (SmsTaskImportResult result : results)
        {
            Task task = assertCreatedTask(result, "Buy milk", "Ada", TODAY);

            assertFalse(task.hasDeadline());
            assertEquals(Task.MAXIMUM_DEADLINE, task.getDeadline());
            assertNull(manager.getTaskEditor(task).getDeadline());
            assertTrue(result.isDeadlineUnreadable());
        }

        assertEquals(2, container.tasks.size());
    }

    @Test
    public void importSmsTask_pastDeadline_preservesDeadline()
    {
        // Arrange
        String body = BODY + "\n2001-01-01";

        // Act
        SmsTaskImportResult result = manager.importSmsTask(ORIGIN, body, "SE");

        // Assert
        Task task = assertCreatedTask(result, "Buy milk", "Ada", TODAY);

        assertEquals(new LocalDate(2001, 1, 1), task.getDeadline());
        assertFalse(result.isDeadlineUnreadable());
    }

    @Test
    public void importSmsTask_regionalOrInternationalOrigin_usesExistingMatcher()
    {
        // Arrange
        String nationalOrigin = "070 123 45 67";
        String internationalOrigin = "(+46) 70 123 45 67";

        // Act
        SmsTaskImportResult national = manager.importSmsTask(nationalOrigin, BODY, " se ");
        SmsTaskImportResult international = manager.importSmsTask(internationalOrigin, BODY, null);

        // Assert
        assertCreatedTask(national, "Buy milk", "Ada", TODAY);
        assertCreatedTask(international, "Buy milk", "Ada", TODAY);
        assertEquals(2, container.tasks.size());
    }

    @Test
    public void importSmsTask_equivalentTrustedEntries_usesFirstMatchingName()
    {
        // Arrange
        TrustedSmsSender bea = new TrustedSmsSender("070 123 45 67", "Bea");

        manager.setSmsTaskSettings(true, Arrays.asList(bea, ada));

        // Act
        SmsTaskImportResult result = manager.importSmsTask(ORIGIN, BODY, "SE");

        // Assert
        assertCreatedTask(result, "Buy milk", "Bea", TODAY);
    }

    @Test
    public void importSmsTask_repeatedMessagesAtDailyLimit_addsDistinctTasksWithoutConsumingSlots()
    {
        // Arrange
        manager.setMaximumNumberOfTasksPerDay(1);

        container.numberOfTasksCompletedToday = 1;
        container.taskCompletionCountDate = TODAY.minusDays(1);

        // Act
        SmsTaskImportResult first = manager.importSmsTask(ORIGIN, BODY, "SE");
        SmsTaskImportResult second = manager.importSmsTask(ORIGIN, BODY, "SE");

        // Assert
        assertCreatedTask(first, "Buy milk", "Ada", TODAY);
        assertCreatedTask(second, "Buy milk", "Ada", TODAY);
        assertNotSame(first.getTask(), second.getTask());
        assertEquals(2, container.tasks.size());
        assertEquals(Integer.valueOf(1), container.maximumNumberOfTasksPerDay);
        assertEquals(1, container.numberOfTasksCompletedToday);
        assertEquals(TODAY.minusDays(1), container.taskCompletionCountDate);
    }

    @Test
    public void importSmsTask_changedTrustedSettings_preservesEarlierNameSnapshot()
    {
        // Arrange
        SmsTaskImportResult first = manager.importSmsTask(ORIGIN, BODY, "SE");

        manager.setSmsTaskSettings(true, Collections.singletonList(new TrustedSmsSender(ORIGIN, "Bea")));

        // Act
        SmsTaskImportResult second = manager.importSmsTask(ORIGIN, BODY, "SE");

        // Assert
        assertCreatedTask(first, "Buy milk", "Ada", TODAY);
        assertCreatedTask(second, "Buy milk", "Bea", TODAY);
    }

    @Test
    public void importSmsTask_replacedContainer_usesCurrentSettingsAndAddsToCurrentContainer()
    {
        // Arrange
        TaskContainer original = container;
        container = new TaskContainer();
        container.trustedSmsSenders.add(new TrustedSmsSender("+46701234568", "Bea"));

        // Act
        SmsTaskImportResult disabled = manager.importSmsTask("+46701234568", BODY, "SE");
        boolean tasksEmptyWhileDisabled = container.tasks.isEmpty();
        container.smsTaskCreationEnabled = true;
        SmsTaskImportResult unmatched = manager.importSmsTask(ORIGIN, BODY, "SE");
        SmsTaskImportResult result = manager.importSmsTask("+46701234568", BODY, "SE");

        // Assert
        assertNull(disabled);
        assertTrue(tasksEmptyWhileDisabled);
        assertNull(unmatched);
        assertCreatedTask(result, "Buy milk", "Bea", TODAY);
        assertEquals(1, container.tasks.size());
        assertTrue(original.tasks.isEmpty());
        assertTrue(original.smsTaskCreationEnabled);
        assertSame(ada, original.trustedSmsSenders.get(0));
    }

    @Test
    public void importSmsTask_clockAdvances_usesCurrentDateForEachTask()
    {
        // Arrange
        SmsTaskImportResult first = manager.importSmsTask(ORIGIN, BODY, "SE");

        timeService.setNow(TODAY.plusDays(1).toLocalDateTime(LocalTime.MIDNIGHT));

        // Act
        SmsTaskImportResult second = manager.importSmsTask(ORIGIN, BODY, "SE");

        // Assert
        assertCreatedTask(first, "Buy milk", "Ada", TODAY);
        assertCreatedTask(second, "Buy milk", "Ada", TODAY.plusDays(1));
    }

    private void addExistingTask()
    {
        existingTask = new Task("Keep task", TODAY.plusDays(3), TODAY.minusDays(10));
        existingTask.setSenderName("Original");
        existingTask.setDone(true, TODAY.minusDays(10));
        container.tasks.add(existingTask);
        container.maximumNumberOfTasksPerDay = 2;
        container.numberOfTasksCompletedToday = 3;
        container.taskCompletionCountDate = TODAY.minusDays(1);
    }

    private void assertRejected(SmsTaskImportResult result)
    {
        assertNull(result);
        assertEquals(Collections.singletonList(existingTask), container.tasks);
        assertEquals("Keep task", existingTask.getDescription());
        assertEquals("Original", existingTask.getSenderName());
        assertEquals(TODAY.plusDays(3), existingTask.getDeadline());
        assertEquals(TODAY.minusDays(10), existingTask.getIgnoreBefore());
        assertEquals(TODAY.minusDays(10), existingTask.getCompleted());
        assertTrue(existingTask.isDone());
        assertEquals(Integer.valueOf(2), container.maximumNumberOfTasksPerDay);
        assertEquals(3, container.numberOfTasksCompletedToday);
        assertEquals(TODAY.minusDays(1), container.taskCompletionCountDate);
    }

    private Task assertCreatedTask(SmsTaskImportResult result, String description, String senderName, LocalDate date)
    {
        assertNotNull(result);
        Task task = result.getTask();

        assertTrue(container.tasks.contains(task));
        assertEquals(description, task.getDescription());
        assertEquals(senderName, task.getSenderName());
        assertEquals(date, task.getIgnoreBefore());
        assertFalse(task.isDone());
        assertNull(task.getCompleted());
        return task;
    }
}
