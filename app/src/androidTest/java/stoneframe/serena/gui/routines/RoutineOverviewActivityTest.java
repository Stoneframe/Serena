package stoneframe.serena.gui.routines;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.app.Instrumentation;
import android.app.Notification;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.SystemClock;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import stoneframe.serena.Container;
import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.Storage;
import stoneframe.serena.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.notifications.Notifier;
import stoneframe.serena.routines.DayRoutine;
import stoneframe.serena.routines.DayRoutineEditor;
import stoneframe.serena.routines.FortnightRoutine;
import stoneframe.serena.routines.FortnightRoutineEditor;
import stoneframe.serena.routines.Procedure;
import stoneframe.serena.routines.ProcedureOccurrence;
import stoneframe.serena.routines.Routine;
import stoneframe.serena.routines.RoutineManager;
import stoneframe.serena.routines.WeekRoutine;
import stoneframe.serena.routines.WeekRoutineEditor;
import stoneframe.serena.storages.JsonConverter;
import stoneframe.serena.storages.SharedPreferencesStorage;
import stoneframe.serena.storages.json.SimpleChoreSelectorConverter;
import stoneframe.serena.storages.json.WeeklyEffortTrackerConverter;
import stoneframe.serena.timeservices.TimeService;

@RunWith(AndroidJUnit4.class)
public class RoutineOverviewActivityTest
{
    private Instrumentation instrumentation;
    private Context context;
    private GlobalState globalState;
    private Serena originalSerena;
    private Serena serena;
    private RecordingStorage storage;
    private FixedTimeService clock;
    private String preferencesName;
    private RoutineOverviewActivity activity;
    private ListView list;
    private LocalDate today;
    private Routine<?> routine;
    private Routine<?> otherRoutine;
    private final Procedure earlier = new Procedure("Earlier", new LocalTime(9, 0));
    private final Procedure selected = new Procedure("Selected", new LocalTime(10, 0));
    private final Procedure simultaneous = new Procedure("Simultaneous", new LocalTime(10, 0));
    private final Procedure later = new Procedure("Later", new LocalTime(11, 0));

    @Before
    public void setUp()
    {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        context = instrumentation.getTargetContext();
        globalState = (GlobalState)context.getApplicationContext();
        originalSerena = globalState.getSerena();
        // The Activity uses the device date; domain time remains controlled independently.
        today = LocalDate.now();
        clock = new FixedTimeService(today.minusDays(28).toLocalDateTime(LocalTime.MIDNIGHT));
        preferencesName = "routine-overview-test-" + UUID.randomUUID();
        Context storageContext = new ContextWrapper(context)
        {
            @Override
            public SharedPreferences getSharedPreferences(String name, int mode)
            {
                return context.getSharedPreferences(preferencesName, mode);
            }
        };
        storage = new RecordingStorage(new SharedPreferencesStorage(storageContext,
            new JsonConverter(new SimpleChoreSelectorConverter(), new WeeklyEffortTrackerConverter())));
        onMain(() ->
        {
            serena = createSerena();
            serena.load();
            globalState.setSerena(serena);
        });
    }

    @After
    public void tearDown()
    {
        onMain(() ->
        {
            if (activity != null)
            {
                activity.finish();
            }
        });
        instrumentation.waitForIdleSync();
        onMain(() -> globalState.setSerena(originalSerena));
        NotificationManagerCompat.from(context).cancel(Notifier.ROUTINES_CHANNEL_ID.hashCode());
        context.deleteSharedPreferences(preferencesName);
    }

    @Test
    public void rowClick_uncompletedOccurrence_identifiesOccurrenceWithoutSaving()
    {
        arrangeRoutines(Routine.DAY_ROUTINE);
        openOverview(14);

        clickSelected();

        LocalDate date = today.plusDays(14);
        String message = context.getString(R.string.routine_overview_complete_message,
            selected.getDescription(), routine.getName(), date.toLocalDateTime(selected.getTime())
                .toString("yyyy-MM-dd HH:mm"));
        assertTrue(hasDialogText(message));
        assertUnchanged(date);
    }

    @Test
    public void confirmation_declined_keepsEveryStatusAndDoesNotSave()
    {
        arrangeRoutines(Routine.DAY_ROUTINE);
        openOverview(14);
        clickSelected();

        clickDialogButton("No");

        assertFalse(hasConfirmation());
        assertUnchanged(today.plusDays(14));
    }

    @Test
    public void confirmation_backDismissed_keepsEveryStatusAndDoesNotSave()
    {
        arrangeRoutines(Routine.DAY_ROUTINE);
        openOverview(14);
        clickSelected();

        instrumentation.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        waitForUi();

        assertFalse(hasConfirmation());
        assertUnchanged(today.plusDays(14));
    }

    @Test
    public void confirmation_outsideDismissed_keepsEveryStatusAndDoesNotSave()
    {
        arrangeRoutines(Routine.DAY_ROUTINE);
        openOverview(14);
        clickSelected();
        Rect bounds = new Rect();
        activeRoot().getBoundsInScreen(bounds);
        assertTrue("Dialog must leave an outside tap target above it", bounds.top > 200);

        long downTime = SystemClock.uptimeMillis();
        sendTouch(downTime, MotionEvent.ACTION_DOWN, bounds.centerX(), bounds.top / 2);
        sendTouch(downTime, MotionEvent.ACTION_UP, bounds.centerX(), bounds.top / 2);
        waitForUi();

        assertFalse(hasConfirmation());
        assertUnchanged(today.plusDays(14));
    }

    @Test
    public void confirmation_futureDailyOccurrence_refreshesAndPersistsDatedBoundary()
    {
        verifyCompletion(Routine.DAY_ROUTINE, 14);
    }

    @Test
    public void confirmation_futureWeeklyOccurrence_refreshesAndPersistsDatedBoundary()
    {
        verifyCompletion(Routine.WEEK_ROUTINE, 14);
    }

    @Test
    public void confirmation_futureFortnightOccurrence_refreshesAndPersistsDatedBoundary()
    {
        verifyCompletion(Routine.FORTNIGHT_ROUTINE, 14);
    }

    @Test
    public void confirmation_pastOccurrence_usesDisplayedDate()
    {
        verifyCompletion(Routine.DAY_ROUTINE, -7);
    }

    @Test
    public void confirmation_laterTodayOccurrence_allowsAdvanceCompletion()
    {
        verifyCompletion(Routine.DAY_ROUTINE, 0);
    }

    @Test
    public void rowClick_completedOccurrence_doesNotOpenConfirmationOrSave()
    {
        arrangeRoutines(Routine.DAY_ROUTINE);
        onMain(() -> serena.getRoutineManager().procedureDone(findSelected(serena, today)));
        openOverview(0);

        clickSelected();

        assertFalse(hasConfirmation());
        assertEquals(0, storage.saveCount);
        assertTrue(routine.isDone(today, selected));
        assertFalse(routine.isDone(today, later));
        assertFalse(otherRoutine.isDone(today, selected));
    }

    @Test
    public void confirmation_pendingNotification_removesCompletedEntries()
    {
        arrangeRoutines(Routine.DAY_ROUTINE);
        onMain(() ->
        {
            clock.now = today.toLocalDateTime(new LocalTime(12, 0));
            Notifier.setupNotificationChannels(context);
            Notifier.showRoutineNotification(context, serena, true);
        });
        openOverview(14);
        assertTrue(routineNotificationText().contains("Earlier"));
        clickSelected();

        clickDialogButton("Yes");

        String expected = serena.getRoutineManager().getPendingProcedures().stream()
            .map(ProcedureOccurrence::toString).collect(Collectors.joining(System.lineSeparator()));
        assertFalse(expected.contains("Earlier"));
        assertFalse(expected.isEmpty());
        assertEquals(expected, routineNotificationText());
    }

    private String routineNotificationText()
    {
        return NotificationManagerCompat.from(context).getActiveNotifications().stream()
            .filter(n -> n.getId() == Notifier.ROUTINES_CHANNEL_ID.hashCode()).findFirst().get()
            .getNotification().extras.getCharSequence(Notification.EXTRA_BIG_TEXT).toString();
    }

    private void verifyCompletion(int routineType, int dayOffset)
    {
        arrangeRoutines(routineType);
        onMain(() -> clock.now = today.toLocalDateTime(new LocalTime(9, 0)));
        openOverview(dayOffset);
        LocalDate date = today.plusDays(dayOffset);
        clickSelected();

        clickDialogButton("Yes");

        assertFalse(hasConfirmation());
        assertEquals(1, storage.saveCount);
        assertBoundary(routine, otherRoutine, date);
        onMain(() ->
        {
            TextView dateView = activity.findViewById(R.id.dateTextView);
            assertEquals(date.toString("yyyy-MM-dd"), dateView.getText().toString());
            assertEquals(5, list.getAdapter().getCount());
            View row = list.getChildAt(selectedPosition() - list.getFirstVisiblePosition());
            assertNotNull("Completed occurrence must remain visible", row);
            assertCompletedAppearance((GradientDrawable)row.getBackground());
        });
        assertPersistedBoundary(date);
    }

    private void assertCompletedAppearance(GradientDrawable background)
    {
        assertEquals(ContextCompat.getColor(context, R.color.status_success_container),
            background.getColor().getDefaultColor());
        Rect bounds = background.getBounds();
        Bitmap bitmap = Bitmap.createBitmap(bounds.width(), bounds.height(), Bitmap.Config.ARGB_8888);
        background.draw(new Canvas(bitmap));
        assertEquals(ContextCompat.getColor(context, R.color.status_success),
            bitmap.getPixel(bounds.width() / 2, 0));
        bitmap.recycle();
    }

    private void assertPersistedBoundary(LocalDate date)
    {
        onMain(() ->
        {
            // Finish directly from Overview; no MainActivity save can mask a missing save here.
            activity.finish();
        });
        instrumentation.waitForIdleSync();
        onMain(() ->
        {
            Serena reloaded = createSerena();
            reloaded.load();
            Routine<?> loadedRoutine = findRoutine(reloaded, routine.getName());
            assertBoundary(loadedRoutine, findRoutine(reloaded, otherRoutine.getName()), date);
            clock.now = date.toLocalDateTime(selected.getTime());
            assertFalse(reloaded.getRoutineManager().getPendingProcedures().stream()
                .anyMatch(p -> p.getRoutine() == loadedRoutine));
            clock.now = date.toLocalDateTime(later.getTime());
            assertTrue(reloaded.getRoutineManager().getPendingProcedures().stream()
                .anyMatch(p -> p.getRoutine() == loadedRoutine
                    && p.getDateTime().equals(clock.now)));
        });
    }

    private void assertBoundary(Routine<?> completed, Routine<?> unchanged, LocalDate date)
    {
        assertTrue(completed.isDone(date.minusDays(1), selected));
        assertTrue(completed.isDone(date.minusWeeks(2), selected));
        assertTrue(completed.isDone(date, earlier));
        assertTrue(completed.isDone(date, selected));
        assertTrue(completed.isDone(date, simultaneous));
        assertFalse(completed.isDone(date, later));
        assertFalse(completed.isDone(date.plusDays(1), selected));
        assertFalse(unchanged.isDone(date.minusDays(1), selected));
        assertFalse(unchanged.isDone(date, selected));
    }

    private void assertUnchanged(LocalDate date)
    {
        assertEquals(0, storage.saveCount);
        for (Procedure procedure : new Procedure[]{earlier, selected, simultaneous, later})
        {
            assertFalse(routine.isDone(date.minusDays(1), procedure));
            assertFalse(routine.isDone(date, procedure));
            assertFalse(routine.isDone(date.plusDays(1), procedure));
        }
        assertFalse(otherRoutine.isDone(date, selected));
    }

    private void arrangeRoutines(int type)
    {
        onMain(() ->
        {
            routine = addRoutine(type, "Selected routine", earlier, selected, simultaneous, later);
            otherRoutine = addRoutine(type, "Other routine", selected.copy());
        });
    }

    private Routine<?> addRoutine(int type, String name, Procedure... procedures)
    {
        RoutineManager manager = serena.getRoutineManager();
        Routine<?> created;
        if (type == Routine.DAY_ROUTINE)
        {
            DayRoutine day = manager.createDayRoutine();
            DayRoutineEditor editor = manager.getDayRoutineEditor(day);
            for (Procedure procedure : procedures)
            {
                editor.addProcedure(procedure);
            }
            editor.save();
            created = day;
        }
        else if (type == Routine.WEEK_ROUTINE)
        {
            WeekRoutine week = manager.createWeekRoutine();
            WeekRoutineEditor editor = manager.getWeekRoutineEditor(week);
            for (int day = 1; day <= 7; day++)
            {
                for (Procedure procedure : procedures)
                {
                    editor.addProcedure(day, procedure);
                }
            }
            editor.save();
            created = week;
        }
        else
        {
            FortnightRoutine fortnight = manager.createFortnightRoutine();
            FortnightRoutineEditor editor = manager.getFortnightRoutineEditor(fortnight);
            for (int week = 1; week <= 2; week++)
            {
                for (int day = 1; day <= 7; day++)
                {
                    for (Procedure procedure : procedures)
                    {
                        editor.addProcedure(week, day, procedure);
                    }
                }
            }
            editor.save();
            created = fortnight;
        }
        created.setName(name);
        created.setEnabled(true);
        return created;
    }

    private Serena createSerena()
    {
        return new Serena(storage, clock, new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30),
            new SimpleChoreSelector());
    }

    private Routine<?> findRoutine(Serena source, String name)
    {
        return source.getRoutineManager().getAllRoutines().stream()
            .filter(r -> r.getName().equals(name)).findFirst().get();
    }

    private ProcedureOccurrence findSelected(Serena source, LocalDate date)
    {
        return source.getRoutineManager().getProceduresForDate(date).stream()
            .filter(p -> p.getRoutine().getName().equals(routine.getName())
                && p.getDescription().equals(selected.getDescription())).findFirst().get();
    }

    private void openOverview(int dayOffset)
    {
        activity = (RoutineOverviewActivity)instrumentation.startActivitySync(
            new Intent(context, RoutineOverviewActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        waitForUi();
        onMain(() ->
        {
            list = activity.findViewById(R.id.daysProceduresList);
            int buttonId = dayOffset < 0 ? R.id.backButton : R.id.forwardButton;
            for (int i = 0; i < Math.abs(dayOffset); i++)
            {
                activity.findViewById(buttonId).performClick();
            }
        });
        waitForUi();
    }

    private int selectedPosition()
    {
        for (int i = 0; i < list.getAdapter().getCount(); i++)
        {
            ProcedureOccurrence item = (ProcedureOccurrence)list.getAdapter().getItem(i);
            if (item.getRoutine() == routine && item.getDescription().equals(selected.getDescription()))
            {
                return i;
            }
        }
        throw new AssertionError("Selected occurrence is missing");
    }

    private void clickSelected()
    {
        onMain(() ->
        {
            int position = selectedPosition();
            list.performItemClick(list.getChildAt(position - list.getFirstVisiblePosition()), position,
                list.getAdapter().getItemId(position));
        });
        waitForUi();
    }

    private void clickDialogButton(String text)
    {
        List<AccessibilityNodeInfo> buttons = activeRoot().findAccessibilityNodeInfosByText(text);
        assertEquals(1, buttons.size());
        assertTrue(buttons.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK));
        waitForUi();
    }

    private boolean hasConfirmation()
    {
        return hasDialogText(context.getString(R.string.routine_overview_complete_title));
    }

    private boolean hasDialogText(String text)
    {
        return !activeRoot().findAccessibilityNodeInfosByText(text).isEmpty();
    }

    private AccessibilityNodeInfo activeRoot()
    {
        AccessibilityNodeInfo root = instrumentation.getUiAutomation().getRootInActiveWindow();
        assertNotNull("An app window must be active", root);
        return root;
    }

    private void sendTouch(long downTime, int action, float x, float y)
    {
        MotionEvent event = MotionEvent.obtain(downTime, SystemClock.uptimeMillis(), action, x, y, 0);
        instrumentation.sendPointerSync(event);
        event.recycle();
    }

    private void waitForUi()
    {
        instrumentation.waitForIdleSync();
        try
        {
            instrumentation.getUiAutomation().waitForIdle(200, 5000);
        }
        catch (java.util.concurrent.TimeoutException e)
        {
            throw new AssertionError("UI did not become idle", e);
        }
    }

    private void onMain(Runnable action)
    {
        instrumentation.runOnMainSync(action);
    }

    private static final class FixedTimeService implements TimeService
    {
        private LocalDateTime now;

        private FixedTimeService(LocalDateTime now)
        {
            this.now = now;
        }

        @Override
        public LocalDateTime getNow()
        {
            return now;
        }

        @Override
        public LocalDate getToday()
        {
            return now.toLocalDate();
        }
    }

    private static final class RecordingStorage implements Storage
    {
        private final Storage delegate;
        private int saveCount;

        private RecordingStorage(Storage delegate)
        {
            this.delegate = delegate;
        }

        @Override
        public Container load()
        {
            return delegate.load();
        }

        @Override
        public void save(Container container)
        {
            delegate.save(container);
            saveCount++;
        }

        @Override
        public int getCurrentVersion()
        {
            return delegate.getCurrentVersion();
        }
    }
}
