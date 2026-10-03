package stoneframe.serena.gui.notifications;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import org.joda.time.LocalDateTime;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.gui.MainActivity;
import stoneframe.serena.reminders.Reminder;
import stoneframe.serena.routines.PendingProcedure;

public class Notifier
{
    public static final String ROUTINES_CHANNEL_ID = "Serena_Routines_v4";
    public static final String REMINDERS_CHANNEL_ID = "Serena_Reminders_v2";
    public static final String TASKS_CHANNEL_ID = "Serena_Tasks_v1";

    private static final long[] THREE_SHORT = {0, 250, 125, 250, 125, 250};
    private static final long[] ONE_LONG_TWO_SHORT = {0, 750, 125, 250, 125, 250};

    private static final List<SerenaNotificationChannel> channels = Arrays.asList(
        new SerenaNotificationChannel(
            ROUTINES_CHANNEL_ID,
            R.string.notification_channel_routines, R.string.notification_subtext_routine,
            THREE_SHORT, R.id.nav_todays),
        new SerenaNotificationChannel(
            REMINDERS_CHANNEL_ID,
            R.string.notification_channel_reminders, R.string.notification_subtext_reminder,
            ONE_LONG_TWO_SHORT, R.id.nav_todays),
        new SerenaNotificationChannel(
            TASKS_CHANNEL_ID,
            R.string.notification_channel_tasks, R.string.notification_subtext_task,
            THREE_SHORT, R.id.nav_all_tasks));

    public static void scheduleAlarm(Context context, Serena serena)
    {
        scheduleRoutineAlarm(context, serena);
        scheduleReminderAlarm(context, serena);
    }

    public static void scheduleRoutineAlarm(Context context, Serena serena)
    {
        scheduleAlarm(
            context,
            serena.getRoutineManager().getNextProcedureTime(),
            RoutineNotifierReceiver.class);
    }

    public static void scheduleReminderAlarm(Context context, Serena serena)
    {
        scheduleAlarm(
            context,
            serena.getReminderManager().getNextReminderTime(),
            ReminderNotifierReceiver.class);
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

    public static void showTaskImportNotification(Context context, boolean deadlineUnreadable)
    {
        SerenaNotificationChannel channel = getChannel(TASKS_CHANNEL_ID);

        String text = context.getString(deadlineUnreadable
            ? R.string.task_notification_deadline_unreadable
            : R.string.task_notification_added);

        channel.show(context, text, true);
    }

    private static void scheduleAlarm(Context context, LocalDateTime triggerDateTime, Class<?> clazz)
    {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            new Intent(context, clazz),
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

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
        private final int nameResource;
        private final int subTextResource;
        private final long[] vibrationPattern;
        private final int destination;

        private SerenaNotificationChannel(
            String channelId,
            int nameResource,
            int subTextResource,
            long[] vibrationPattern,
            int destination)
        {
            this.channelId = channelId;
            this.nameResource = nameResource;
            this.subTextResource = subTextResource;
            this.vibrationPattern = vibrationPattern;
            this.destination = destination;
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
                context.getString(nameResource),
                NotificationManager.IMPORTANCE_HIGH);

            channel.enableVibration(true);
            channel.setVibrationPattern(vibrationPattern);
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
            if (!isNotificationAllowed(context))
            {
                return;
            }

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context,
                channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(contentText)
                .setSubText(context.getString(subTextResource))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setOnlyAlertOnce(!splash)
                .setAutoCancel(true)
                .setContentIntent(getPendingIntent(context))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));

            Notification notification = builder.build();

            notificationManager.notify(channelId.hashCode(), notification);
        }

        private boolean isNotificationAllowed(Context context)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }

            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            NotificationChannel channel = manager.getNotificationChannel(channelId);

            if (channel == null)
            {
                return false;
            }

            return manager.areNotificationsEnabled()
                && channel.getImportance() != NotificationManager.IMPORTANCE_NONE;
        }

        private PendingIntent getPendingIntent(Context context)
        {
            Intent openMainActivityIntent = new Intent(context, MainActivity.class);

            openMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            openMainActivityIntent.putExtra("fragment", destination);

            boolean opensTasks = destination == R.id.nav_all_tasks;
            int requestCode = opensTasks ? destination : 0;
            int mutability = opensTasks ? PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_MUTABLE;

            return PendingIntent.getActivity(
                context,
                requestCode,
                openMainActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | mutability);
        }
    }
}
