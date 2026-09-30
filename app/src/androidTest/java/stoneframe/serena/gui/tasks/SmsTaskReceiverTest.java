package stoneframe.serena.gui.tasks;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.provider.Telephony;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import stoneframe.serena.Container;
import stoneframe.serena.Serena;
import stoneframe.serena.Storage;
import stoneframe.serena.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.notifications.NotifierReceiver;
import stoneframe.serena.storages.JsonConverter;
import stoneframe.serena.storages.SharedPreferencesStorage;
import stoneframe.serena.storages.json.SimpleChoreSelectorConverter;
import stoneframe.serena.storages.json.WeeklyEffortTrackerConverter;
import stoneframe.serena.tasks.Task;
import stoneframe.serena.tasks.TaskEditor;
import stoneframe.serena.tasks.TrustedSmsSender;
import stoneframe.serena.timeservices.RealTimeService;

@RunWith(AndroidJUnit4.class)
public class SmsTaskReceiverTest
{
    private static final String ORIGIN = "+46701234567";
    private static final String BODY = "#Serena\nBuy milk";

    private Context targetContext;
    private IsolatedGlobalState globalState;
    private ReceiverContext receiverContext;
    private RecordingStorage storage;
    private Serena serena;
    private String preferencesName;

    @Before
    public void setUp()
    {
        targetContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        preferencesName = "sms-receiver-test-" + UUID.randomUUID();
        globalState = new IsolatedGlobalState(targetContext, preferencesName);
        receiverContext = new ReceiverContext(targetContext, globalState);
        storage = new RecordingStorage(new SharedPreferencesStorage(
            globalState,
            new JsonConverter(new SimpleChoreSelectorConverter(), new WeeklyEffortTrackerConverter())));

        onMain(() ->
        {
            serena = new Serena(storage, new RealTimeService(),
                new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30), new SimpleChoreSelector());
            serena.load();
            globalState.setSerena(serena);
            configureSms(true, ORIGIN);
            addTask("Keep task");
            serena.save();
            storage.saveCount = 0;
        });
    }

    @After
    public void tearDown()
    {
        targetContext.deleteSharedPreferences(preferencesName);
    }

    @Test
    public void onReceive_validMessage_savesAttributedTaskAndPreservesExistingTask()
    {
        // Arrange
        Intent intent = smsIntent(BODY);

        // Act
        receive(intent);
        onMain(() -> globalState.setSerena(null));
        Serena reloaded = loadSerena();

        // Assert
        assertEquals(1, storage.saveCount);
        assertEquals(2, reloaded.getTaskManager().getAllTasks().size());
        assertEquals("Keep task", reloaded.getTaskManager().getAllTasks().get(0).getDescription());
        Task task = reloaded.getTaskManager().getAllTasks().get(1);
        assertEquals("Buy milk", task.getDescription());
        assertEquals("Ada", task.getSenderName());
        assertFalse(task.isDone());
        assertFalse(task.hasDeadline());
    }

    @Test
    public void onReceive_coldStart_loadsPublishesAndSavesTask()
    {
        // Arrange
        onMain(() -> globalState.setSerena(null));
        Intent intent = smsIntent(BODY);

        // Act
        receive(intent);
        Serena loaded = globalState.getSerena();
        onMain(() -> globalState.setSerena(null));
        Serena reloaded = loadSerena();

        // Assert
        assertNotNull(loaded);
        assertNotSame(serena, loaded);
        assertEquals(2, reloaded.getTaskManager().getAllTasks().size());
        assertEquals("Ada", reloaded.getTaskManager().getAllTasks().get(1).getSenderName());
        assertSame(reloaded, globalState.getSerena());
    }

    @Test
    public void onReceive_activeInstance_preservesUnsavedChanges()
    {
        // Arrange
        onMain(() -> addTask("Unsaved task"));
        Intent intent = smsIntent(BODY);

        // Act
        receive(intent);

        // Assert
        assertSame(serena, globalState.getSerena());
        assertEquals(3, serena.getTaskManager().getAllTasks().size());
        assertEquals(1, storage.saveCount);
    }

    @Test
    public void onReceive_disabled_doesNotImportOrSave()
    {
        // Arrange
        onMain(() -> configureSms(false, ORIGIN));
        Intent intent = smsIntent(BODY);

        // Act
        receive(intent);

        // Assert
        assertRejected();
    }

    @Test
    public void onReceive_untrusted_doesNotImportOrSave()
    {
        // Arrange
        onMain(() -> configureSms(true, "+46701234568"));
        Intent intent = smsIntent(BODY);

        // Act
        receive(intent);

        // Assert
        assertRejected();
    }

    @Test
    public void onReceive_invalidBody_doesNotImportOrSave()
    {
        // Arrange
        Intent intent = smsIntent("Ordinary SMS");

        // Act
        receive(intent);

        // Assert
        assertRejected();
    }

    @Test
    public void onReceive_unreadableDeadline_savesUndatedTask()
    {
        // Arrange
        Intent intent = smsIntent(BODY + "\nnot a date");

        // Act
        receive(intent);

        // Assert
        assertEquals(1, storage.saveCount);
        assertEquals(2, serena.getTaskManager().getAllTasks().size());
        assertFalse(serena.getTaskManager().getAllTasks().get(1).hasDeadline());
    }

    @Test
    public void onReceive_repeatedMessages_importsAndSavesEachMessage()
    {
        // Arrange
        Intent intent = smsIntent(BODY);

        // Act
        receive(intent);
        receive(intent);

        // Assert
        assertEquals(2, storage.saveCount);
        assertEquals(3, serena.getTaskManager().getAllTasks().size());
        assertNotSame(serena.getTaskManager().getAllTasks().get(1),
            serena.getTaskManager().getAllTasks().get(2));
    }

    @Test
    public void onReceive_multipartWithValidFirstPart_doesNotLoadImportOrSave()
    {
        // Arrange
        Intent intent = smsIntent(BODY);
        intent.putExtra("pdus", new Object[] {pdu(BODY), pdu("Second part")});
        String savedData = savedData();
        onMain(() -> globalState.setSerena(null));

        // Act
        receive(intent);

        // Assert
        assertNull(globalState.getSerena());
        assertEquals(savedData, savedData());
        assertRejected();
    }

    @Test
    public void onReceive_malformedOrUnrelatedBroadcast_doesNotLoadImportOrSave()
    {
        // Arrange
        Intent missing = new Intent(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        Intent wrongType = smsIntent(BODY).putExtra("pdus", "invalid");
        Intent empty = smsIntent(BODY).putExtra("pdus", new Object[0]);
        Intent nullPart = smsIntent(BODY).putExtra("pdus", new Object[] {null});
        Intent invalidPdu = smsIntent(BODY).putExtra("pdus", new Object[] {new byte[] {0}});
        Intent wrongFormat = smsIntent(BODY).putExtra("format", "unsupported");
        Intent noFormat = smsIntent(BODY);
        noFormat.removeExtra("format");
        String savedData = savedData();
        onMain(() -> globalState.setSerena(null));

        // Act
        for (Intent intent : Arrays.asList(null, new Intent("unrelated"), missing,
            wrongType, empty, nullPart, invalidPdu, wrongFormat, noFormat))
        {
            receive(intent);
        }

        // Assert
        assertNull(globalState.getSerena());
        assertEquals(savedData, savedData());
        assertRejected();
    }

    @Test
    public void onReceive_permissionDenied_doesNotLoadImportOrSave()
    {
        // Arrange
        receiverContext.smsPermissionGranted = false;
        onMain(() -> globalState.setSerena(null));
        Intent intent = smsIntent(BODY);

        // Act
        receive(intent);

        // Assert
        assertNull(globalState.getSerena());
        assertRejected();
    }

    @Test
    public void getOrLoadSerena_repeatedCalls_reusesPublishedInstance()
    {
        // Arrange
        onMain(() -> globalState.setSerena(null));

        // Act
        Serena first = loadSerena();
        Serena second = loadSerena();

        // Assert
        assertSame(first, second);
        assertSame(first, globalState.getSerena());
        assertEquals(1, first.getTaskManager().getAllTasks().size());
    }

    @Test
    public void notifierReceiver_coldStart_usesSharedPublishedInstance()
    {
        // Arrange
        onMain(() -> globalState.setSerena(null));
        Serena[] resolved = new Serena[1];

        // Act
        onMain(() -> resolved[0] = TestNotifierReceiver.resolve(receiverContext));
        Serena second = loadSerena();

        // Assert
        assertSame(resolved[0], second);
        assertSame(second, globalState.getSerena());
    }

    @Test
    public void getOrLoadSerena_invalidSavedData_doesNotPublishOrOverwriteData()
    {
        // Arrange
        globalState.getSharedPreferences("DATA", 0).edit().putString("DATA", "{").commit();
        onMain(() -> globalState.setSerena(null));
        RuntimeException[] failure = new RuntimeException[1];

        // Act
        onMain(() ->
        {
            try
            {
                globalState.getOrLoadSerena();
            }
            catch (RuntimeException e)
            {
                failure[0] = e;
            }
        });

        // Assert
        assertNotNull(failure[0]);
        assertNull(globalState.getSerena());
        assertEquals("{", savedData());
        assertEquals(0, storage.saveCount);
    }

    private void configureSms(boolean enabled, String trustedNumber)
    {
        serena.getTaskManager().setSmsTaskSettings(enabled,
            Collections.singletonList(new TrustedSmsSender(trustedNumber, "Ada")));
    }

    private void addTask(String description)
    {
        Task task = serena.getTaskManager().createTask();
        TaskEditor editor = serena.getTaskManager().getTaskEditor(task);
        editor.setDescription(description);
        editor.save();
    }

    private void assertRejected()
    {
        assertEquals(0, storage.saveCount);
        assertEquals(1, serena.getTaskManager().getAllTasks().size());
        assertEquals("Keep task", serena.getTaskManager().getAllTasks().get(0).getDescription());
    }

    private Serena loadSerena()
    {
        Serena[] result = new Serena[1];
        onMain(() -> result[0] = globalState.getOrLoadSerena());
        return result[0];
    }

    private String savedData()
    {
        return globalState.getSharedPreferences("DATA", 0).getString("DATA", null);
    }

    private void receive(Intent intent)
    {
        onMain(() -> new SmsTaskReceiver().onReceive(receiverContext, intent));
    }

    private static void onMain(Runnable action)
    {
        InstrumentationRegistry.getInstrumentation().runOnMainSync(action);
    }

    private static Intent smsIntent(String body)
    {
        return new Intent(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
            .putExtra("format", "3gpp")
            .putExtra("pdus", new Object[] {pdu(body)});
    }

    private static byte[] pdu(String body)
    {
        // GSM SMS-DELIVER from ORIGIN, UCS-2 encoded, dated 2024-01-02.
        String header = "00040B916407214365F7000842102000000000";
        byte[] text = body.getBytes(StandardCharsets.UTF_16BE);
        int headerLength = header.length() / 2;
        byte[] result = new byte[headerLength + 1 + text.length];

        for (int i = 0; i < headerLength; i++)
        {
            result[i] = (byte)Integer.parseInt(header.substring(i * 2, i * 2 + 2), 16);
        }

        result[headerLength] = (byte)text.length;
        System.arraycopy(text, 0, result, headerLength + 1, text.length);
        return result;
    }

    private static final class IsolatedGlobalState extends GlobalState
    {
        private final String preferencesName;

        private IsolatedGlobalState(Context context, String preferencesName)
        {
            attachBaseContext(context);
            this.preferencesName = preferencesName;
        }

        @Override
        public SharedPreferences getSharedPreferences(String name, int mode)
        {
            return getBaseContext().getSharedPreferences(preferencesName, mode);
        }
    }

    private static final class ReceiverContext extends ContextWrapper
    {
        private final GlobalState globalState;
        private boolean smsPermissionGranted = true;

        private ReceiverContext(Context context, GlobalState globalState)
        {
            super(context);
            this.globalState = globalState;
        }

        @Override
        public Context getApplicationContext()
        {
            return globalState;
        }

        @Override
        public int checkPermission(String permission, int pid, int uid)
        {
            if (Manifest.permission.RECEIVE_SMS.equals(permission))
            {
                return smsPermissionGranted ? PackageManager.PERMISSION_GRANTED : PackageManager.PERMISSION_DENIED;
            }

            return super.checkPermission(permission, pid, uid);
        }

        @Override
        public Object getSystemService(String name)
        {
            return Context.TELEPHONY_SERVICE.equals(name) ? null : super.getSystemService(name);
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
            saveCount++;
            delegate.save(container);
        }

        @Override
        public int getCurrentVersion()
        {
            return delegate.getCurrentVersion();
        }
    }

    private static final class TestNotifierReceiver extends NotifierReceiver
    {
        private static Serena resolve(Context context)
        {
            return getSerena(context);
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
        }
    }
}
