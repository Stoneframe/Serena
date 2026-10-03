package stoneframe.serena.storages;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;

import stoneframe.serena.Container;
import stoneframe.serena.tasks.TaskContainer;
import stoneframe.serena.tasks.TaskEditor;
import stoneframe.serena.tasks.TaskManager;
import stoneframe.serena.tasks.TrustedSmsSender;
import stoneframe.serena.storages.json.SimpleChoreSelectorConverter;
import stoneframe.serena.storages.json.SimpleEffortTrackerConverter;
import stoneframe.serena.timeservices.RealTimeService;

public class SmsTaskSettingsStorageTest
{
    private final JsonConverter jsonConverter = new JsonConverter(
        new SimpleChoreSelectorConverter(),
        new SimpleEffortTrackerConverter());

    @Test
    public void fromJson_olderTaskData_disablesSmsTaskCreation() throws Exception
    {
        String version29Json = "{\"Version\":29,\"TaskContainer\":{\"tasks\":[],"
            + "\"maximumNumberOfTasksPerDay\":null,\"numberOfTasksCompletedToday\":0,"
            + "\"taskCompletionCountDate\":null}}";

        Container container = jsonConverter.fromJson(version29Json);
        TaskManager taskManager = new TaskManager(() -> container.TaskContainer, new RealTimeService());

        assertEquals(30, container.Version);
        assertFalse(taskManager.isSmsTaskCreationEnabled());
        assertTrue(taskManager.getTrustedSmsSenders().isEmpty());
    }

    @Test
    public void fromJson_version27Data_upgradesThroughVersion30() throws Exception
    {
        String version27Json = "{\"Version\":27,\"TaskContainer\":{\"tasks\":[]},"
            + "\"SleepContainer\":{\"sleep\":{}},\"BalancerContainer\":{\"balancers\":[]}}";

        Container container = jsonConverter.fromJson(version27Json);
        TaskManager taskManager = new TaskManager(() -> container.TaskContainer, new RealTimeService());

        assertEquals(30, container.Version);
        assertFalse(taskManager.isSmsTaskCreationEnabled());
        assertTrue(taskManager.getTrustedSmsSenders().isEmpty());
    }

    @Test
    public void smsTaskSettings_roundTripThroughJson() throws Exception
    {
        Container container = new Container();
        container.Version = 30;
        container.TaskContainer = new TaskContainer();

        TaskManager taskManager = new TaskManager(() -> container.TaskContainer, new RealTimeService());
        TaskEditor taskEditor = taskManager.getTaskEditor(taskManager.createTask());
        taskEditor.setDescription("Keep this task");
        taskEditor.save();
        taskManager.setSmsTaskSettings(
            true,
            Collections.singletonList(new TrustedSmsSender("+46701234567", "Ada")));

        Container restored = jsonConverter.fromJson(jsonConverter.toJson(container));
        TaskManager restoredTaskManager = new TaskManager(() -> restored.TaskContainer, new RealTimeService());

        assertTrue(restoredTaskManager.isSmsTaskCreationEnabled());
        assertEquals(1, restoredTaskManager.getTrustedSmsSenders().size());
        assertEquals("+46701234567", restoredTaskManager.getTrustedSmsSenders().get(0).getPhoneNumber());
        assertEquals("Ada", restoredTaskManager.getTrustedSmsSenders().get(0).getContactName());
        assertEquals("Keep this task", restoredTaskManager.getAllTasks().get(0).getDescription());
    }
}
