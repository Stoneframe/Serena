package stoneframe.serena.gui.routines;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.model.Serena;
import stoneframe.serena.json.SimpleChoreSelectorConverter;
import stoneframe.serena.json.WeeklyEffortTrackerConverter;
import stoneframe.serena.model.Storage;
import stoneframe.serena.model.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.model.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.serena.model.storages.JsonConverter;
import stoneframe.serena.model.storages.SharedPreferencesStorage;
import stoneframe.serena.model.timeservices.RealTimeService;

public class RoutineNotifierReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Serena serena = getSerena(context);

        RoutineNotifier.showRoutineNotification(context, serena);
        RoutineNotifier.scheduleRoutineAlarm(context, serena.getNextRoutineProcedureTime());
    }

    @NonNull
    private static Serena getSerena(Context context)
    {
        GlobalState globalState = (GlobalState)context.getApplicationContext();

        Serena serena = globalState.getSerena();

        if (serena == null)
        {
            serena = loadSerenaFromStorage(context);
        }

        return serena;
    }

    @NonNull
    private static Serena loadSerenaFromStorage(Context context)
    {
        Storage storage = new SharedPreferencesStorage(
            context,
            new JsonConverter(
                new SimpleChoreSelectorConverter(),
                new WeeklyEffortTrackerConverter()));

        Serena serena = new Serena(
            storage,
            new RealTimeService(),
            new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30),
            new SimpleChoreSelector());

        serena.load();

        return serena;
    }
}
