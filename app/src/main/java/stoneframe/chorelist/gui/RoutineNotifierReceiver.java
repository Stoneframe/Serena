package stoneframe.chorelist.gui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import java.security.acl.Group;
import java.util.List;
import java.util.stream.Collectors;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.json.SimpleChoreSelectorConverter;
import stoneframe.chorelist.json.WeeklyEffortTrackerConverter;
import stoneframe.chorelist.model.Procedure;
import stoneframe.chorelist.model.Storage;
import stoneframe.chorelist.model.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.efforttrackers.WeeklyEffortTracker;
import stoneframe.chorelist.model.storages.SharedPreferencesStorage;
import stoneframe.chorelist.model.timeservices.RealTimeService;

public class RoutineNotifierReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        ChoreList choreList = getChoreList(context);

        List<Procedure> procedures = choreList.getPendingProcedures();

        String notificationText = procedures.stream()
            .map(Procedure::toString)
            .collect(Collectors.joining(System.lineSeparator()));

        RoutineNotifier.showRoutineNotification(context, notificationText, "Routine");
        RoutineNotifier.scheduleRoutineAlarm(context, choreList.getNextRoutineProcedureTime());
    }

    @NonNull
    private static ChoreList getChoreList(Context context)
    {
        GlobalState globalState = (GlobalState)context.getApplicationContext();

        ChoreList choreList = globalState.getChoreList();

        if (choreList == null)
        {
            choreList = loadChoreListFromStorage(context);
        }

        return choreList;
    }

    @NonNull
    private static ChoreList loadChoreListFromStorage(Context context)
    {
        Storage storage = new SharedPreferencesStorage(
            context,
            new SimpleChoreSelectorConverter(),
            new WeeklyEffortTrackerConverter());

        ChoreList choreList = new ChoreList(
            storage,
            new RealTimeService(),
            new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30),
            new SimpleChoreSelector());

        choreList.load();

        return choreList;
    }
}
