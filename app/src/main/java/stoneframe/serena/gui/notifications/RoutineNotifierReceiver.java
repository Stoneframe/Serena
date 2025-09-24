package stoneframe.serena.gui.notifications;

import android.content.Context;
import android.content.Intent;

import stoneframe.serena.Serena;

public class RoutineNotifierReceiver extends NotifierReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Serena serena = getSerena(context);

        serena.notifyChange();

        Notifier.showRoutineNotification(context, serena, true);
        Notifier.scheduleRoutineAlarm(context, serena);
    }
}
