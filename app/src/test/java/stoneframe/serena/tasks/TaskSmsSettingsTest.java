package stoneframe.serena.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stoneframe.serena.mocks.MockTimeService;

public class TaskSmsSettingsTest
{
    @Test
    public void smsTaskSettings_defaultToDisabledWithNoTrustedSenders()
    {
        TaskManager taskManager = createTaskManager();

        assertFalse(taskManager.isSmsTaskCreationEnabled());
        assertTrue(taskManager.getTrustedSmsSenders().isEmpty());
    }

    @Test
    public void setSmsTaskSettings_copiesInputAndReturnsReadOnlySnapshot()
    {
        TaskManager taskManager = createTaskManager();
        List<TrustedSmsSender> senders = new ArrayList<>();
        senders.add(new TrustedSmsSender("+46701234567", "Ada"));

        taskManager.setSmsTaskSettings(true, senders);
        senders.clear();

        List<TrustedSmsSender> storedSenders = taskManager.getTrustedSmsSenders();

        assertTrue(taskManager.isSmsTaskCreationEnabled());
        assertEquals(1, storedSenders.size());
        assertEquals("+46701234567", storedSenders.get(0).getPhoneNumber());
        assertEquals("Ada", storedSenders.get(0).getContactName());
        assertThrows(UnsupportedOperationException.class, storedSenders::clear);
    }

    @Test
    public void setSmsTaskSettings_allowsEnabledWithNoTrustedSenders()
    {
        TaskManager taskManager = createTaskManager();

        taskManager.setSmsTaskSettings(true, Collections.emptyList());

        assertTrue(taskManager.isSmsTaskCreationEnabled());
        assertTrue(taskManager.getTrustedSmsSenders().isEmpty());
    }

    private static TaskManager createTaskManager()
    {
        TaskContainer taskContainer = new TaskContainer();
        MockTimeService timeService = new MockTimeService(
            new LocalDate(2024, 1, 1).toLocalDateTime(LocalTime.MIDNIGHT));

        return new TaskManager(() -> taskContainer, timeService);
    }
}
