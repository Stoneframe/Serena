package stoneframe.serena.storages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import java.util.List;

import stoneframe.serena.Container;
import stoneframe.serena.mocks.MockTimeService;
import stoneframe.serena.storages.json.SimpleChoreSelectorConverter;
import stoneframe.serena.storages.json.SimpleEffortTrackerConverter;
import stoneframe.serena.tasks.Task;
import stoneframe.serena.tasks.TaskContainer;
import stoneframe.serena.tasks.TaskEditor;
import stoneframe.serena.tasks.TaskManager;

public class TaskSenderAttributionStorageTest
{
    private static final LocalDate TODAY = new LocalDate(2024, 1, 2);
    private static final LocalDate DEADLINE = new LocalDate(2024, 2, 3);
    private static final LocalDate IGNORE_BEFORE = new LocalDate(2024, 1, 1);

    private final JsonConverter jsonConverter = new JsonConverter(
        new SimpleChoreSelectorConverter(), new SimpleEffortTrackerConverter());

    @Test
    public void roundTrip_attributedAndManualTasks_preservesAttributionAndTaskFields() throws Exception
    {
        Container container = new Container();
        container.Version = 30;
        container.TaskContainer = new TaskContainer();
        TaskManager manager = createTaskManager(container);
        TaskEditor attributedEditor = manager.getTaskEditor(manager.createTask());
        attributedEditor.setDescription("Attributed task");
        attributedEditor.setDeadline(DEADLINE);
        attributedEditor.setIgnoreBefore(IGNORE_BEFORE);
        attributedEditor.setSenderName(" Ada ");
        attributedEditor.setDone(true);
        attributedEditor.save();
        TaskEditor manualEditor = manager.getTaskEditor(manager.createTask());
        manualEditor.setDescription("Manual task");
        manualEditor.save();

        Container restored = jsonConverter.fromJson(jsonConverter.toJson(container));
        List<Task> tasks = createTaskManager(restored).getAllTasks();
        Task attributedTask = findTask(tasks, "Attributed task");
        Task manualTask = findTask(tasks, "Manual task");

        assertEquals(30, restored.Version);
        assertEquals(2, tasks.size());
        assertEquals(" Ada ", attributedTask.getSenderName());
        assertTaskFields(attributedTask, "Attributed task");
        assertNull(manualTask.getSenderName());
        assertFalse(manualTask.hasDeadline());
        assertEquals(TODAY, manualTask.getIgnoreBefore());
        assertFalse(manualTask.isDone());
        assertNull(manualTask.getCompleted());
    }

    @Test
    public void fromJson_version29TaskWithoutAttribution_preservesTaskThroughMigration() throws Exception
    {
        assertUnattributedTaskLoadsAndRoundTrips(29, "");
    }

    @Test
    public void fromJson_version30TaskWithoutAttribution_preservesTask() throws Exception
    {
        assertUnattributedTaskLoadsAndRoundTrips(30, "");
    }

    @Test
    public void fromJson_explicitNullAttribution_preservesTaskThroughRoundTrip() throws Exception
    {
        assertUnattributedTaskLoadsAndRoundTrips(30, ",\"senderName\":null");
    }

    private void assertUnattributedTaskLoadsAndRoundTrips(int version, String attributionJson) throws Exception
    {
        String json = "{\"Version\":" + version + ",\"TaskContainer\":{\"tasks\":[{\"data\":{"
            + "\"description\":\"Existing task\",\"deadline\":\"2024-02-03\",\"ignoreBefore\":\"2024-01-01\","
            + "\"completed\":\"2024-01-02\",\"isDone\":true" + attributionJson + "}}]}}";

        Container loaded = jsonConverter.fromJson(json);
        assertUnattributedTask(loaded);
        assertUnattributedTask(jsonConverter.fromJson(jsonConverter.toJson(loaded)));
    }

    private void assertUnattributedTask(Container container)
    {
        List<Task> tasks = createTaskManager(container).getAllTasks();

        assertEquals(30, container.Version);
        assertEquals(1, tasks.size());
        assertNull(tasks.get(0).getSenderName());
        assertTaskFields(tasks.get(0), "Existing task");
    }

    private static void assertTaskFields(Task task, String description)
    {
        assertEquals(description, task.getDescription());
        assertEquals(DEADLINE, task.getDeadline());
        assertEquals(IGNORE_BEFORE, task.getIgnoreBefore());
        assertEquals(TODAY, task.getCompleted());
        assertTrue(task.isDone());
    }

    private static TaskManager createTaskManager(Container container)
    {
        MockTimeService timeService = new MockTimeService(TODAY.toLocalDateTime(LocalTime.MIDNIGHT));
        return new TaskManager(() -> container.TaskContainer, timeService);
    }

    private static Task findTask(List<Task> tasks, String description)
    {
        return tasks.stream()
            .filter(task -> description.equals(task.getDescription()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Task not found: " + description));
    }
}
