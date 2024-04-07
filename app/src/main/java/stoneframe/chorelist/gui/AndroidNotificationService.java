package stoneframe.chorelist.gui;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.NotifierService;

public class AndroidNotificationService implements NotifierService
{
    private static final String CHANNEL_ID = "1234";

    private final Context context;

    public AndroidNotificationService(Context context)
    {
        this.context = context;

        setupNotificationChannel();
    }

    @Override
    public void sendNotification(String contentText, String subText)
    {
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);

        Intent openMainActivityIntent = new Intent(context, MainActivity.class);

        openMainActivityIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
            context,
            0,
            openMainActivityIntent,
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

    private void setupNotificationChannel()
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
}
