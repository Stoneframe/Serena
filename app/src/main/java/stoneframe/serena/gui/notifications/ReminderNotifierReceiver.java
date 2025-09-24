package stoneframe.serena.gui.notifications;

import android.content.Context;
import android.content.Intent;

import stoneframe.serena.Serena;

public class ReminderNotifierReceiver extends NotifierReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Serena serena = getSerena(context);

        serena.notifyChange();

        Notifier.showReminderNotification(context, serena, true);
        Notifier.scheduleReminderAlarm(context, serena);
    }
}
