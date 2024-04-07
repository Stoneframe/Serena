package stoneframe.chorelist.gui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

import stoneframe.chorelist.json.SimpleChoreSelectorConverter;
import stoneframe.chorelist.json.WeeklyEffortTrackerConverter;
import stoneframe.chorelist.model.AlarmListener;
import stoneframe.chorelist.model.AlarmService;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.Storage;
import stoneframe.chorelist.model.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.efforttrackers.WeeklyEffortTracker;
import stoneframe.chorelist.model.storages.JsonConverter;
import stoneframe.chorelist.model.storages.SharedPreferencesStorage;
import stoneframe.chorelist.model.timeservices.RealTimeService;

public class AndroidAlarmService extends BroadcastReceiver implements AlarmService
{
    private final Context context;

    public AndroidAlarmService(Context context)
    {
        this.context = context;
    }

    @Override
    public void setAlarm(DateTime dateTime, AlarmListener alarmListener)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            new Intent(context, AndroidAlarmService.class),
            PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            dateTime.getMillis(),
            pendingIntent);
    }

    @Override
    public void cancelAlarm()
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            new Intent(context, AndroidAlarmService.class),
            PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        AlarmListener alarmListener = getChoreList(context);

        alarmListener.notifyAlarm();
    }

    @NonNull
    private ChoreList getChoreList(Context context)
    {
        GlobalState globalState = (GlobalState)context.getApplicationContext();

        ChoreList choreList = globalState.getChoreList();

        if (choreList == null)
        {
            choreList = loadChoreListFromStorage(context);
        }

        return choreList;
    }

    @NonNull
    private ChoreList loadChoreListFromStorage(Context context)
    {
        Storage storage = new SharedPreferencesStorage(
            context,
            new JsonConverter(
                new SimpleChoreSelectorConverter(),
                new WeeklyEffortTrackerConverter()));

        ChoreList choreList = new ChoreList(
            storage,
            new RealTimeService(),
            this,
            new AndroidNotificationService(context),
            new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30),
            new SimpleChoreSelector());

        choreList.load();

        return choreList;
    }
}
