package stoneframe.chorelist.gui;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.joda.time.DateTime;

import stoneframe.chorelist.R;

public class RoutineNotifier
{
    public static final String CHANNEL_ID = "1234";

    public static void scheduleRoutineAlarm(Context context, DateTime triggerDateTime)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            new Intent(context, RoutineNotifierReceiver.class),
            PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerDateTime.getMillis(),
            pendingIntent);
    }

    public static void cancelRoutineAlarm(Context context)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            new Intent(context, RoutineNotifierReceiver.class),
            PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager.cancel(pendingIntent);
    }

    public static void setupNotificationChannel(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null)
        {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            "Test",
            NotificationManager.IMPORTANCE_HIGH);

        notificationManager.createNotificationChannel(channel);
    }

    public static void showRoutineNotification(Context context, String contentText, String subText)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            new Intent(context, MainActivity.class),
            PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(contentText)
            .setSubText(subText)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));

        Notification notification = builder.build();

        notificationManager.notify(1, notification);
    }
}
