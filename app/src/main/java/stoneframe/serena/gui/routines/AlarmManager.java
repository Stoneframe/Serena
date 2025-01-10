package stoneframe.serena.gui.routines;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import java.util.List;
import java.util.stream.Collectors;

import stoneframe.serena.model.Serena;
import stoneframe.serena.model.routines.PendingProcedure;

public class AlarmManager
{
    public static void startAlarm(Context context, Serena serena)
    {
        List<PendingProcedure> procedures = serena.getRoutineManager()
            .getPendingProcedures()
            .stream()
            .filter(p -> p.hasAlarm())
            .collect(Collectors.toList());

        if (!procedures.isEmpty())
        {
            Intent activityIntent = new Intent(context, AlarmClockActivity.class);
            PendingIntent fullScreenPendingIntent = PendingIntent.getActivity(
                context,
                0,
                activityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

//            Intent alarmIntent = new Intent(context, AlarmClockActivity.class);
//            alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            context.startActivity(alarmIntent);
        }
    }
}
