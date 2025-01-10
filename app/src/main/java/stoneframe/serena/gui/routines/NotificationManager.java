package stoneframe.serena.gui.routines;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.List;
import java.util.stream.Collectors;

import stoneframe.serena.R;
import stoneframe.serena.gui.MainActivity;
import stoneframe.serena.model.Serena;
import stoneframe.serena.model.routines.PendingProcedure;

public class NotificationManager
{
    public static final String CHANNEL_ID = "1234";
    public static final int NOTIFICATION_ID = 1;

    public static void setupNotificationChannel(Context context)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (notificationManager.getNotificationChannel(CHANNEL_ID) != null)
        {
            return;
        }

        NotificationChannel channel = new NotificationChannel(
            CHANNEL_ID,
            "Routines",
            android.app.NotificationManager.IMPORTANCE_HIGH);

        notificationManager.createNotificationChannel(channel);
    }

    public static void showRoutineNotification(Context context, Serena serena)
    {
        List<PendingProcedure> procedures = serena.getRoutineManager().getPendingProcedures();

        if (procedures.isEmpty())
        {
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

            notificationManager.cancel(NOTIFICATION_ID);
        }
        else
        {
            String notificationText = procedures.stream()
                .map(PendingProcedure::toString)
                .collect(Collectors.joining(System.lineSeparator()));

            showRoutineNotification(context, notificationText, true);
        }
    }

    @SuppressLint("MissingPermission")
    public static void updateNotification(Context context, Serena serena)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        if (notificationManager.getActiveNotifications()
            .stream()
            .noneMatch(n -> n.getId() == NOTIFICATION_ID))
        {
            return;
        }

        List<PendingProcedure> procedures = serena.getRoutineManager().getPendingProcedures();

        if (procedures.isEmpty())
        {
            notificationManager.cancel(NOTIFICATION_ID);
        }
        else
        {
            String notificationText = procedures.stream()
                .map(PendingProcedure::toString)
                .collect(Collectors.joining(System.lineSeparator()));

            showRoutineNotification(context, notificationText, false);
        }
    }

    @SuppressLint("MissingPermission")
    private static void showRoutineNotification(Context context, String contentText, boolean splash)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Intent openMainActivityIntent = new Intent(context, MainActivity.class);

        openMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openMainActivityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText(contentText)
            .setSubText("Routine")
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOnlyAlertOnce(!splash)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText));

        Notification notification = builder.build();

        notificationManager.notify(NOTIFICATION_ID, notification);
    }
}
