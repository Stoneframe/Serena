package stoneframe.serena.gui.routines;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.notifications.Notifier;
import stoneframe.serena.gui.routines.days.DayRoutineActivity;
import stoneframe.serena.gui.routines.fortnights.FortnightRoutineActivity;
import stoneframe.serena.gui.routines.weeks.WeekRoutineActivity;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.Serena;
import stoneframe.serena.routines.DayRoutine;
import stoneframe.serena.routines.FortnightRoutine;
import stoneframe.serena.routines.Routine;
import stoneframe.serena.routines.RoutineManager;
import stoneframe.serena.routines.WeekRoutine;

public class AllRoutinesFragment extends Fragment
{
    private ActivityResultLauncher<Intent> editRoutineLauncher;
    private ActivityResultLauncher<Intent> routineOverviewLauncher;

    private SimpleListAdapter<Routine<?>> routineListAdapter;

    private GlobalState globalState;
    private Serena serena;
    private RoutineManager routineManager;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();

        serena = globalState.getSerena();
        routineManager = serena.getRoutineManager();

        View rootView = inflater.inflate(R.layout.fragment_all_routines, container, false);

        routineListAdapter = new SimpleListAdapterBuilder<>(
            requireContext(),
            routineManager::getAllRoutines,
            Routine::getName)
            .withSecondaryTextFunction(this::getRoutineTypeName)
            .create();

        ListView routineListView = rootView.findViewById(R.id.all_routines);
        routineListView.setAdapter(routineListAdapter);
        routineListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Routine<?> routine = (Routine<?>)routineListAdapter.getItem(position);
            assert routine != null;
            startRoutineEditor(routine, EditRoutineActivity.ACTION_EDIT);
        });

        Button overviewButton = rootView.findViewById(R.id.overviewButton);
        overviewButton.setOnClickListener(v ->
        {
            Intent intent = new Intent(getActivity(), RoutineOverviewActivity.class);
            routineOverviewLauncher.launch(intent);
        });

        Button addButton = rootView.findViewById(R.id.addButton);
        addButton.setOnClickListener(v -> showAddRoutineTypeDialog());

        editRoutineLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editRoutineCallback);

        routineOverviewLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::routineOverviewCallback);

        checkNotificationAndExactAlarmsPermissions();

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        routineListAdapter.notifyDataSetChanged();
    }

    private String getRoutineTypeName(Routine<?> routine)
    {
        switch (routine.getRoutineType())
        {
            case Routine.DAY_ROUTINE:
                return "Day";
            case Routine.WEEK_ROUTINE:
                return "Week";
            case Routine.FORTNIGHT_ROUTINE:
                return "Fortnight";
            default:
                return "Unknown";
        }
    }

    private void showAddRoutineTypeDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater dialogInflater = LayoutInflater.from(requireContext());
        View dialogView = dialogInflater.inflate(R.layout.dialog_add_routine, null);
        builder.setView(dialogView);

        builder.setNegativeButton("Close", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();

        Button addDayButton = dialogView.findViewById(R.id.addDayButton);
        addDayButton.setOnClickListener(v ->
        {
            Routine<?> routine = routineManager.createDayRoutine();
            alertDialog.cancel();
            startRoutineEditor(routine, DayRoutineActivity.ACTION_ADD);
        });

        Button addWeekButton = dialogView.findViewById(R.id.addWeekButton);
        addWeekButton.setOnClickListener(v ->
        {
            Routine<?> routine = routineManager.createWeekRoutine();
            alertDialog.cancel();
            startRoutineEditor(routine, WeekRoutineActivity.ACTION_ADD);
        });

        Button addFortnightButton = dialogView.findViewById(R.id.addFortnightButton);
        addFortnightButton.setOnClickListener(v ->
        {
            Routine<?> routine = routineManager.createFortnightRoutine();
            alertDialog.cancel();
            startRoutineEditor(routine, FortnightRoutineActivity.ACTION_ADD);
        });

        alertDialog.show();
    }

    private void startRoutineEditor(Routine<?> routine, int mode)
    {
        globalState.setActiveRoutine(routine);

        Intent intent;

        if (routine instanceof DayRoutine)
        {
            intent = new Intent(getActivity(), DayRoutineActivity.class);
        }
        else if (routine instanceof WeekRoutine)
        {
            intent = new Intent(getActivity(), WeekRoutineActivity.class);
        }
        else if (routine instanceof FortnightRoutine)
        {
            intent = new Intent(getActivity(), FortnightRoutineActivity.class);
        }
        else
        {
            return;
        }

        intent.putExtra("ACTION", mode);

        editRoutineLauncher.launch(intent);
    }

    private void checkNotificationAndExactAlarmsPermissions()
    {
        ActivityResultLauncher<String> requestNotificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::requestPermissionsCallback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            else
            {
                checkPermissionToScheduleExactAlarms();
            }
        }
        else
        {
            checkPermissionToScheduleExactAlarms();
        }
    }

    private void requestPermissionsCallback(Boolean isGranted)
    {
        if (isGranted)
        {
            // Permission was granted, you can now send notifications
            Log.d("NotificationPermission", "Notification permission granted.");
        }
        else
        {
            // Permission was denied, handle accordingly
            Log.d("NotificationPermission", "Notification permission denied.");
        }

        checkPermissionToScheduleExactAlarms();
    }

    private void editRoutineCallback(ActivityResult activityResult)
    {
        routineListAdapter.notifyDataSetChanged();

        Notifier.scheduleAlarm(requireContext(), serena);
    }

    private void routineOverviewCallback(ActivityResult activityResult)
    {
    }

    private void checkPermissionToScheduleExactAlarms()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            AlarmManager alarmManager = (AlarmManager)requireActivity().getSystemService(Context.ALARM_SERVICE);

            if (!alarmManager.canScheduleExactAlarms())
            {
                DialogUtils.showWarningDialog(
                    requireContext(),
                    "Alarms Permission",
                    "You need to allow Serena to set alarms. Enable \"Allow setting alarms and reminders\"",
                    () ->
                    {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));  // Directs the intent to your app's settings
                        startActivity(intent);
                    });
            }
        }
    }
}
