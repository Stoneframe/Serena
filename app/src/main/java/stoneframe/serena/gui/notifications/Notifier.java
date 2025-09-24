package stoneframe.serena.gui.notifications;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.joda.time.LocalDateTime;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.gui.MainActivity;
import stoneframe.serena.gui.util.DateUtils;
import stoneframe.serena.reminders.Reminder;
import stoneframe.serena.routines.PendingProcedure;

public class Notifier
{
    public static final String ROUTINES_CHANNEL_ID = "Serena_Routines_v4";
    public static final String REMINDERS_CHANNEL_ID = "Serena_Reminders_v1";

    private static final List<SerenaNotificationChannel> channels = Arrays.asList(
        new SerenaNotificationChannel(ROUTINES_CHANNEL_ID, "Routines", "Routine"),
        new SerenaNotificationChannel(REMINDERS_CHANNEL_ID, "Reminders", "Reminder"));

    public static void scheduleAlarm(Context context, Serena serena)
    {
        scheduleRoutineAlarm(context, serena);
        scheduleReminderAlarm(context,serena);
    }

    public static void scheduleRoutineAlarm(Context context, Serena serena)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            new Intent(context, RoutineNotifierReceiver.class),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        LocalDateTime triggerDateTime = serena.getRoutineManager().getNextProcedureTime();

        if (triggerDateTime != null)
        {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerDateTime.toDateTime().getMillis(),
                pendingIntent);
        }
        else
        {
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void scheduleReminderAlarm(Context context, Serena serena)
    {
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            new Intent(context, ReminderNotifierReceiver.class),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        LocalDateTime triggerDateTime = serena.getReminderManager().getNextReminderTime();

        if (triggerDateTime != null)
        {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerDateTime.toDateTime().getMillis(),
                pendingIntent);
        }
        else
        {
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void setupNotificationChannels(Context context)
    {
        setupUsedChannels(context);
        clearUnusedChannels(context);
    }

    public static void showRoutineNotification(Context context, Serena serena, boolean splash)
    {
        SerenaNotificationChannel channel = getChannel(ROUTINES_CHANNEL_ID);

        List<PendingProcedure> procedures = serena.getRoutineManager().getPendingProcedures();

        if (procedures.isEmpty())
        {
            channel.cancel(context);
        }
        else
        {
            String notificationText = procedures.stream()
                .map(PendingProcedure::toString)
                .collect(Collectors.joining(System.lineSeparator()));

            channel.show(context, notificationText, splash);
        }
    }

    public static void showReminderNotification(Context context, Serena serena, boolean splash)
    {
        SerenaNotificationChannel channel = getChannel(REMINDERS_CHANNEL_ID);

        List<Reminder> reminders = serena.getReminderManager().getPendingReminders();

        if (reminders.isEmpty())
        {
            channel.cancel(context);
        }
        else
        {
            String notificationText = reminders.stream()
                .map(Reminder::getText)
                .collect(Collectors.joining(System.lineSeparator()));

            channel.show(context, notificationText, splash);
        }
    }

    private static void setupUsedChannels(Context context)
    {
        channels.forEach(c -> c.setup(context));
    }

    private static void clearUnusedChannels(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        for (NotificationChannel channel : notificationManager.getNotificationChannels())
        {
            if (channels.stream().noneMatch(c -> c.getChannelId().equals(channel.getId())))
            {
                notificationManager.deleteNotificationChannel(channel.getId());
            }
        }
    }

    private static SerenaNotificationChannel getChannel(String channelId)
    {
        return channels.stream().filter(c -> c.getChannelId().equals(channelId)).findFirst().get();
    }

    private static class SerenaNotificationChannel
    {
        private final String channelId;
        private final String name;
        private final String subText;

        private SerenaNotificationChannel(String channelId, String name, String subText)
        {
            this.channelId = channelId;
            this.name = name;
            this.subText = subText;
        }

        public String getChannelId()
        {
            return channelId;
        }

        public void setup(Context context)
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            if (notificationManager.getNotificationChannel(channelId) != null)
            {
                return;
            }

            NotificationChannel channel = new NotificationChannel(
                channelId,
                name,
                NotificationManager.IMPORTANCE_HIGH);

            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 250, 125, 250, 125, 250});
            notificationManager.cancel(channelId.hashCode());
            notificationManager.createNotificationChannel(channel);
        }

        public void show(Context context, String contentText, boolean splash)
        {
            if (!splash && !isShowing(context))
            {
                return;
            }

            showNotification(context, contentText, splash);
        }

        public void cancel(Context context)
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            notificationManager.cancel(channelId.hashCode());
        }

        private boolean isShowing(Context context)
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            return notificationManager.getActiveNotifications()
                .stream()
                .anyMatch(n -> n.getId() == channelId.hashCode());
        }

        @SuppressLint("MissingPermission")
        private void showNotification(Context context, String contentText, boolean splash)
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                ROUTINES_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(contentText)
                .setSubText(subText)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOnlyAlertOnce(!splash)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntent(context))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));

            Notification notification = builder.build();

            notificationManager.notify(channelId.hashCode(), notification);
        }

        private static PendingIntent getPendingIntent(Context context)
        {
            Intent openMainActivityIntent = new Intent(context, MainActivity.class);

            openMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            openMainActivityIntent.putExtra("fragment", R.id.nav_todays);

            return PendingIntent.getActivity(
                context,
                0,
                openMainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        }
    }
}
