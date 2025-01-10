package stoneframe.serena.gui.routines;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import org.joda.time.LocalDateTime;

public class RoutineNotifier
{
    @SuppressLint("ScheduleExactAlarm")
    public static void scheduleRoutineAlarm(Context context, LocalDateTime triggerDateTime)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            new Intent(context, RoutineNotifierReceiver.class),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerDateTime.toDateTime().getMillis(),
            pendingIntent);
    }

    public static void cancelRoutineAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            new Intent(context, RoutineNotifierReceiver.class),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        alarmManager.cancel(pendingIntent);
    }
}
