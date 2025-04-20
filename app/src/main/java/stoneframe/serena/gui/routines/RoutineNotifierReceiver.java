package stoneframe.serena.gui.routines;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.storages.json.SimpleChoreSelectorConverter;
import stoneframe.serena.storages.json.WeeklyEffortTrackerConverter;
import stoneframe.serena.Serena;
import stoneframe.serena.Storage;
import stoneframe.serena.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.serena.storages.JsonConverter;
import stoneframe.serena.storages.SharedPreferencesStorage;
import stoneframe.serena.timeservices.RealTimeService;

public class RoutineNotifierReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Serena serena = getSerena(context);

        serena.notifyChange();

        RoutineNotifier.showRoutineNotification(context, serena);
        RoutineNotifier.scheduleRoutineAlarm(
            context,
            serena.getRoutineManager().getNextProcedureTime());
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
