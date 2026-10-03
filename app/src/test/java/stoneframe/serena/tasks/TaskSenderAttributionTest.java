package stoneframe.serena.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;

import stoneframe.serena.mocks.MockTimeService;

public class TaskSenderAttributionTest
{
    private static final LocalDate TODAY = new LocalDate(2024, 1, 2);

    private TaskManager taskManager;
    private Task task;
    private TaskEditor editor;

    @Before
    public void setUp()
    {
        TaskContainer container = new TaskContainer();
        MockTimeService timeService = new MockTimeService(TODAY.toLocalDateTime(LocalTime.MIDNIGHT));
        taskManager = new TaskManager(() -> container, timeService);
        task = taskManager.createTask();
        editor = taskManager.getTaskEditor(task);
    }

    @Test
    public void createTask_manualTask_hasNoSenderAttribution()
    {
        assertNull(task.getSenderName());
        assertNull(editor.getSenderName());
    }

    @Test
    public void setSenderName_setsVerbatimNameAndClearsAttribution()
    {
        editor.setSenderName(" Ada ");

        assertEquals(" Ada ", task.getSenderName());
        assertEquals(" Ada ", editor.getSenderName());

        editor.setSenderName(null);

        assertNull(task.getSenderName());
        assertNull(editor.getSenderName());
    }

    @Test
    public void save_newAttributedTask_addsTaskWithAttribution()
    {
        editor.setSenderName("Ada");
        assertTrue(taskManager.getAllTasks().isEmpty());

        editor.save();

        assertEquals(1, taskManager.getAllTasks().size());
        assertSame(task, taskManager.getAllTasks().get(0));
        assertEquals("Ada", taskManager.getAllTasks().get(0).getSenderName());
    }

    @Test
    public void editTask_ordinaryChanges_preserveSenderAttribution()
    {
        editor.setSenderName("Ada");
        editor.save();

        editor.setDescription("Changed description");
        editor.setDeadline(TODAY.plusDays(1));
        editor.setIgnoreBefore(TODAY.plusDays(1));
        editor.setDone(true);

        assertEquals("Ada", task.getSenderName());

        editor.setDone(false);
        editor.save();

        assertEquals("Ada", task.getSenderName());
        assertEquals("Ada", editor.getSenderName());
    }

    @Test
    public void revert_changedAttribution_restoresCheckpointAndEditorView()
    {
        editor.setSenderName("Ada");
        task.edit();

        editor.setSenderName("Bea");
        task.revert();

        assertEquals("Ada", task.getSenderName());
        assertEquals("Ada", editor.getSenderName());
    }

    @Test
    public void revert_clearedAttribution_restoresCheckpoint()
    {
        editor.setSenderName("Ada");
        task.edit();

        editor.setSenderName(null);
        task.revert();

        assertEquals("Ada", editor.getSenderName());
    }

    @Test
    public void revert_unattributedCheckpoint_restoresNoAttribution()
    {
        task.edit();

        editor.setSenderName("Ada");
        task.revert();

        assertNull(task.getSenderName());
        assertNull(editor.getSenderName());
    }

    @Test
    public void setSmsTaskSettings_replacesTrustedSender_preservesTaskNameSnapshot()
    {
        TrustedSmsSender sender = new TrustedSmsSender("+46701234567", "Ada");
        taskManager.setSmsTaskSettings(true, Collections.singletonList(sender));
        editor.setSenderName(sender.getContactName());
        editor.save();

        TrustedSmsSender replacement = new TrustedSmsSender("+46701234567", "Bea");
        taskManager.setSmsTaskSettings(true, Collections.singletonList(replacement));

        assertEquals("Ada", task.getSenderName());
        assertEquals("Bea", taskManager.getTrustedSmsSenders().get(0).getContactName());
    }
}
