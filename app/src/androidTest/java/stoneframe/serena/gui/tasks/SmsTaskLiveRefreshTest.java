package stoneframe.serena.gui.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.provider.Telephony;
import android.view.View;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import stoneframe.serena.Container;
import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.Storage;
import stoneframe.serena.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.MainActivity;
import stoneframe.serena.gui.notifications.Notifier;
import stoneframe.serena.tasks.Task;
import stoneframe.serena.tasks.TaskEditor;
import stoneframe.serena.tasks.TrustedSmsSender;
import stoneframe.serena.timeservices.TimeService;

@RunWith(AndroidJUnit4.class)
public class SmsTaskLiveRefreshTest
{
    private static final LocalDateTime NOW = new LocalDateTime(2024, 1, 2, 12, 0);
    private Instrumentation instrumentation;
    private Context context;
    private GlobalState globalState;
    private Serena originalSerena;
    private Serena serena;
    private MainActivity activity;
    private Activity settings;
    private Fragment fragment;
    private ListView list;
    private BaseAdapter adapter;
    private int changes;
    private Thread.UncaughtExceptionHandler originalExceptionHandler;
    private final DataSetObserver observer = new DataSetObserver()
    {
        @Override
        public void onChanged()
        {
            changes++;
        }
    };

    @Before
    public void setUp()
    {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        context = instrumentation.getTargetContext();
        globalState = (GlobalState)context.getApplicationContext();
        originalSerena = globalState.getSerena();
        originalExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        onMain(() ->
        {
            serena = new Serena(new EmptyStorage(), new FixedTimeService(),
                new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30), new SimpleChoreSelector());
            serena.load();
            serena.getTaskManager().setSmsTaskSettings(true,
                Collections.singletonList(new TrustedSmsSender("+46701234567", "Ada")));
            globalState.setSerena(serena);
        });
    }

    @After
    public void tearDown()
    {
        onMain(() ->
        {
            if (adapter != null)
            {
                adapter.unregisterDataSetObserver(observer);
            }
            if (settings != null)
            {
                settings.finish();
            }
            if (activity != null)
            {
                activity.finish();
            }
        });
        instrumentation.waitForIdleSync();
        onMain(() -> globalState.setSerena(originalSerena));
        Thread.setDefaultUncaughtExceptionHandler(originalExceptionHandler);
        NotificationManagerCompat.from(context).cancel(Notifier.TASKS_CHANNEL_ID.hashCode());
    }

    @Test
    public void onReceive_allTasksVisible_refreshesAttributedRow()
    {
        // Arrange
        onMain(() -> addTask("Keep task"));
        openFragment(R.id.nav_all_tasks, R.id.all_tasks);

        // Act
        receive("Buy milk");
        awaitRow("Buy milk (from: Ada)");

        // Assert
        assertEquals(1, changes);
        assertTrue(renderedTexts().contains("Keep task"));
        assertTrue(renderedTexts().contains("Buy milk (from: Ada)"));
        assertTrue(fragment.isResumed());
    }

    @Test
    public void onReceive_todayVisible_refreshesRowAndSectionColor()
    {
        // Arrange
        openFragment(R.id.nav_todays, R.id.todays_tasks);
        int emptyColor = headerColor();

        // Act
        receive("Buy milk");
        awaitRow("Buy milk");

        // Assert
        assertEquals(1, changes);
        assertTrue(renderedTexts().contains("Buy milk"));
        assertFalse(renderedTexts().contains("Buy milk (from: Ada)"));
        assertEquals(ContextCompat.getColor(context, R.color.status_success), emptyColor);
        assertEquals(ContextCompat.getColor(context, R.color.brand_primary), headerColor());
    }

    @Test
    public void onReceive_todayAllowanceExhausted_showsOnlyUrgentImport()
    {
        // Arrange
        onMain(() ->
        {
            serena.getTaskManager().setMaximumNumberOfTasksPerDay(1);
            serena.getTaskManager().incrementNumberOfTasksCompletedToday();
        });
        openFragment(R.id.nav_todays, R.id.todays_tasks);

        // Act
        receive("Nonurgent task");
        receive("Urgent task\n2024-01-02");
        awaitRow("Urgent task");

        // Assert
        assertEquals(2, changes);
        assertEquals(2, serena.getTaskManager().getAllTasks().size());
        assertEquals(Collections.singletonList("Urgent task"), renderedTexts());
    }

    @Test
    public void onReceive_allTasksPaused_catchesUpWithoutDuplicateListeners()
    {
        // Arrange
        openFragment(R.id.nav_all_tasks, R.id.all_tasks);
        settings = instrumentation.startActivitySync(new Intent(context, TaskSettingsActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        instrumentation.waitForIdleSync();
        onMain(() -> changes = 0);

        // Act
        receive("While paused");
        int pausedChanges = changes;
        onMain(() -> settings.finish());
        instrumentation.waitForIdleSync();
        awaitRow("While paused (from: Ada)");
        settings = instrumentation.startActivitySync(new Intent(context, TaskSettingsActivity.class)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        onMain(() -> settings.finish());
        instrumentation.waitForIdleSync();
        onMain(() -> changes = 0);
        receive("After resume");
        awaitRow("After resume (from: Ada)");

        // Assert
        assertEquals(0, pausedChanges);
        assertEquals(1, changes);
        assertTrue(renderedTexts().contains("While paused (from: Ada)"));
        assertTrue(renderedTexts().contains("After resume (from: Ada)"));
    }

    private void openFragment(int destination, int listId)
    {
        activity = (MainActivity)instrumentation.startActivitySync(new Intent(context, MainActivity.class)
            .putExtra("fragment", destination).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        instrumentation.waitForIdleSync();
        onMain(() ->
        {
            fragment = activity.getSupportFragmentManager().findFragmentById(R.id.flContent);
            list = activity.findViewById(listId);
            adapter = (BaseAdapter)list.getAdapter();
            adapter.registerDataSetObserver(observer);
            changes = 0;
        });
    }

    private void receive(String taskText)
    {
        Intent intent = smsIntent(taskText);
        onMain(() -> new SmsTaskReceiver().onReceive(context, intent));
        instrumentation.waitForIdleSync();
    }

    private static Intent smsIntent(String taskText)
    {
        String header = "00040B916407214365F7000842102000000000";
        byte[] text = ("#Serena\n" + taskText).getBytes(StandardCharsets.UTF_16BE);
        int headerLength = header.length() / 2;
        byte[] pdu = new byte[headerLength + 1 + text.length];
        for (int i = 0; i < headerLength; i++)
        {
            pdu[i] = (byte)Integer.parseInt(header.substring(i * 2, i * 2 + 2), 16);
        }
        pdu[headerLength] = (byte)text.length;
        System.arraycopy(text, 0, pdu, headerLength + 1, text.length);
        return new Intent(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
            .putExtra("format", "3gpp").putExtra("pdus", new Object[] {pdu});
    }

    private void addTask(String description)
    {
        Task task = serena.getTaskManager().createTask();
        TaskEditor editor = serena.getTaskManager().getTaskEditor(task);
        editor.setDescription(description);
        editor.save();
    }

    private List<String> renderedTexts()
    {
        List<String> texts = new ArrayList<>();
        onMain(() ->
        {
            for (int i = 0; i < list.getChildCount(); i++)
            {
                View row = list.getChildAt(i);
                TextView text = row instanceof TextView ? (TextView)row : row.findViewById(R.id.mainText);
                texts.add(text.getText().toString());
            }
        });
        return texts;
    }

    private void awaitRow(String text)
    {
        long deadline = SystemClock.elapsedRealtime() + 3000;
        while (!renderedTexts().contains(text) && SystemClock.elapsedRealtime() < deadline)
        {
            SystemClock.sleep(20);
            instrumentation.waitForIdleSync();
        }
        assertTrue("Missing rendered task: " + text, renderedTexts().contains(text));
    }

    private int headerColor()
    {
        int[] color = new int[1];
        onMain(() -> color[0] = ((GradientDrawable)((View)activity.findViewById(R.id.tasks_text)
            .getParent()).getBackground()).getColor().getDefaultColor());
        return color[0];
    }

    private void onMain(Runnable action)
    {
        instrumentation.runOnMainSync(action);
    }

    private static final class FixedTimeService implements TimeService
    {
        @Override
        public LocalDateTime getNow()
        {
            return NOW;
        }

        @Override
        public LocalDate getToday()
        {
            return NOW.toLocalDate();
        }
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
