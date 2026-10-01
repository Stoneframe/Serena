package stoneframe.serena.gui.notifications;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.SystemClock;

import androidx.core.app.NotificationManagerCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import stoneframe.serena.Container;
import stoneframe.serena.Serena;
import stoneframe.serena.Storage;
import stoneframe.serena.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.MainActivity;
import stoneframe.serena.reminders.Reminder;
import stoneframe.serena.reminders.ReminderEditor;
import stoneframe.serena.timeservices.RealTimeService;

@RunWith(AndroidJUnit4.class)
public class TaskImportNotificationTest
{
    private Instrumentation instrumentation;
    private Context context;
    private GlobalState globalState;
    private Serena originalSerena;
    private Serena serena;
    private NotificationManagerCompat manager;
    private Instrumentation.ActivityMonitor activityMonitor;
    private Activity activity;

    @Before
    public void setUp()
    {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        context = instrumentation.getTargetContext();
        Notifier.setupNotificationChannels(context);
        manager = NotificationManagerCompat.from(context);
        cancelNotifications();
        globalState = (GlobalState)context.getApplicationContext();
        originalSerena = globalState.getSerena();
        activityMonitor = instrumentation.addMonitor(MainActivity.class.getName(), null, false);
        instrumentation.runOnMainSync(() ->
        {
            serena = new Serena(
                new EmptyStorage(), new RealTimeService(),
                new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30), new SimpleChoreSelector());
            serena.load();
            globalState.setSerena(serena);
        });
    }

    @After
    public void tearDown()
    {
        instrumentation.runOnMainSync(() ->
        {
            if (activity != null)
            {
                activity.finish();
            }
        });
        instrumentation.waitForIdleSync();
        instrumentation.runOnMainSync(() -> globalState.setSerena(originalSerena));
        instrumentation.removeMonitor(activityMonitor);
        cancelNotifications();
    }

    @Test
    public void showTaskImportNotification_existingChannel_postsGenericNotification()
    {
        // Arrange
        String expectedText = "New task added";

        // Act
        Notifier.showTaskImportNotification(context, false);
        Notification notification = awaitNotification(Notifier.TASKS_CHANNEL_ID, expectedText);

        // Assert
        assertEquals(
            NotificationManager.IMPORTANCE_HIGH,
            manager.getNotificationChannel(Notifier.TASKS_CHANNEL_ID).getImportance());
        assertEquals("Tasks", manager.getNotificationChannel(Notifier.TASKS_CHANNEL_ID).getName().toString());
        assertEquals(expectedText, notification.extras.getString(Notification.EXTRA_TEXT));
        assertEquals(expectedText, notification.extras.getString(Notification.EXTRA_BIG_TEXT));
        assertEquals("Task", notification.extras.getString(Notification.EXTRA_SUB_TEXT));
        assertTrue((notification.flags & Notification.FLAG_AUTO_CANCEL) != 0);
        assertFalse((notification.flags & Notification.FLAG_ONLY_ALERT_ONCE) != 0);
        assertNotNull(notification.contentIntent);
    }

    @Test
    public void showTaskImportNotification_laterNormalImport_replacesDeadlineWarning()
    {
        // Arrange
        String warningText = "New task added, but its deadline could not be read.";
        Notifier.showTaskImportNotification(context, true);
        Notification warning = awaitNotification(Notifier.TASKS_CHANNEL_ID, warningText);

        // Act
        Notifier.showTaskImportNotification(context, false);
        Notification normal = awaitNotification(Notifier.TASKS_CHANNEL_ID, "New task added");

        // Assert
        assertEquals(warningText, warning.extras.getString(Notification.EXTRA_TEXT));
        assertEquals("New task added", normal.extras.getString(Notification.EXTRA_TEXT));
        assertEquals(
            1, manager.getActiveNotifications().stream()
                .filter(n -> n.getId() == Notifier.TASKS_CHANNEL_ID.hashCode()).count());
    }

    @Test
    public void setupNotificationChannels_afterTaskImport_preservesTasksChannelAndNotification()
    {
        // Arrange
        Notifier.showTaskImportNotification(context, false);
        awaitNotification(Notifier.TASKS_CHANNEL_ID, "New task added");

        // Act
        Notifier.setupNotificationChannels(context);

        // Assert
        assertNotNull(manager.getNotificationChannel(Notifier.TASKS_CHANNEL_ID));
        assertEquals(
            "New task added", awaitNotification(Notifier.TASKS_CHANNEL_ID, "New task added")
                .extras.getString(Notification.EXTRA_TEXT));
    }

    @Test
    public void notificationTaps_tasksAndReminders_keepSeparateDestinations() throws Exception
    {
        // Arrange
        instrumentation.runOnMainSync(() ->
        {
            Reminder reminder = serena.getReminderManager().createReminder();
            ReminderEditor editor = serena.getReminderManager().getEditor(reminder);
            editor.setText("Synthetic reminder");
            editor.setDateTime(LocalDateTime.now().minusMinutes(1));
            editor.save();
        });
        Notifier.setupNotificationChannels(context);
        Notifier.showTaskImportNotification(context, false);
        Notification task = awaitNotification(Notifier.TASKS_CHANNEL_ID, "New task added");
        Notifier.showReminderNotification(context, serena, true);
        Notification reminder = awaitNotification(Notifier.REMINDERS_CHANNEL_ID, "Synthetic reminder");

        // Act
        sendNotification(task, "Tasks");
        String coldTaskTitle = activity.getTitle().toString();
        Notifier.showTaskImportNotification(context, true);
        sendNotification(reminder, "Today");
        String reminderTitle = activity.getTitle().toString();
        sendNotification(task, "Tasks");
        String runningTaskTitle = activity.getTitle().toString();

        // Assert
        assertEquals("Tasks", coldTaskTitle);
        assertEquals("Today", reminderTitle);
        assertEquals("Tasks", runningTaskTitle);
    }

    private void sendNotification(Notification notification, String expectedTitle) throws Exception
    {
        notification.contentIntent.send();
        if (activity == null)
        {
            activity = instrumentation.waitForMonitorWithTimeout(activityMonitor, 3000);
            assertNotNull(activity);
        }
        instrumentation.waitForIdleSync();
        long deadline = SystemClock.elapsedRealtime() + 3000;
        while (!expectedTitle.contentEquals(activity.getTitle()) && SystemClock.elapsedRealtime() < deadline)
        {
            SystemClock.sleep(20);
            instrumentation.waitForIdleSync();
        }
    }

    private Notification awaitNotification(String channelId, String expectedText)
    {
        long deadline = SystemClock.elapsedRealtime() + 3000;
        while (SystemClock.elapsedRealtime() < deadline)
        {
            Notification notification = manager.getActiveNotifications().stream()
                .filter(n -> n.getId() == channelId.hashCode())
                .map(n -> n.getNotification()).findFirst().orElse(null);
            if (notification != null && expectedText.equals(notification.extras.getString(Notification.EXTRA_TEXT)))
            {
                return notification;
            }
            SystemClock.sleep(20);
        }
        throw new AssertionError("Notification text was not posted: " + expectedText);
    }

    private void cancelNotifications()
    {
        Arrays.asList(Notifier.TASKS_CHANNEL_ID, Notifier.ROUTINES_CHANNEL_ID, Notifier.REMINDERS_CHANNEL_ID)
            .forEach(id -> manager.cancel(id.hashCode()));
        long deadline = SystemClock.elapsedRealtime() + 3000;
        while (manager.getActiveNotifications().size() != 0 && SystemClock.elapsedRealtime() < deadline)
        {
            SystemClock.sleep(20);
        }
        assertEquals(0, manager.getActiveNotifications().size());
    }

    private static final class EmptyStorage implements Storage
    {
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
            return 30;
        }
    }
}
