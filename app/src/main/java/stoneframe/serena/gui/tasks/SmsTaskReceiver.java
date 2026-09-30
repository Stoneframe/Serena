package stoneframe.serena.gui.tasks;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.IntentCompat;

import stoneframe.serena.Serena;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.tasks.TaskManager;

public class SmsTaskReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (intent == null || !isSmsReceptionAllowed(context, intent.getAction()))
        {
            return;
        }

        SmsMessage message = decodeSinglePartMessage(intent);

        if (message == null)
        {
            return;
        }

        addTaskToSerena(context, message);
    }

    private static boolean isSmsReceptionAllowed(Context context, String action)
    {
        return Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(action) && hasReceiveSmsPermission(context);
    }

    private static boolean hasReceiveSmsPermission(Context context)
    {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.RECEIVE_SMS) == PackageManager.PERMISSION_GRANTED;
    }

    @Nullable
    private static SmsMessage decodeSinglePartMessage(Intent intent)
    {
        try
        {
            Object[] pdus = IntentCompat.getSerializableExtra(intent, "pdus", Object[].class);
            String format = intent.getStringExtra("format");

            if (!isSinglePartPdu(pdus, format))
            {
                return null;
            }

            SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);

            return messages == null || messages.length != 1 ? null : messages[0];
        }
        catch (RuntimeException e)
        {
            // Malformed broadcast extras and PDUs must not reach the import or save path.
            return null;
        }
    }

    private static boolean isSinglePartPdu(@Nullable Object[] pdus, @Nullable String format)
    {
        return pdus != null
            && pdus.length == 1
            && pdus[0] instanceof byte[]
            && ("3gpp".equals(format) || "3gpp2".equals(format));
    }

    private static void addTaskToSerena(Context context, SmsMessage message)
    {
        GlobalState globalState = (GlobalState)context.getApplicationContext();
        Serena serena = globalState.getOrLoadSerena();

        TaskManager.SmsTaskImportResult importResult = serena.getTaskManager().importSmsTask(
            message.getOriginatingAddress(),
            message.getMessageBody(),
            getCountryIso(context));

        if (importResult != null)
        {
            serena.save();
        }
    }

    @Nullable
    private static String getCountryIso(Context context)
    {
        TelephonyManager telephonyManager = context.getSystemService(TelephonyManager.class);

        if (telephonyManager == null)
        {
            return null;
        }

        try
        {
            return telephonyManager.getNetworkCountryIso();
        }
        catch (UnsupportedOperationException e)
        {
            return null;
        }
    }
}
