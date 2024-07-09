package stoneframe.chorelist.gui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Objects;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.DayRoutine;
import stoneframe.chorelist.model.FortnightRoutine;
import stoneframe.chorelist.model.Routine;
import stoneframe.chorelist.model.WeekRoutine;

public class AllRoutinesFragment extends Fragment
{
    private ActivityResultLauncher<Intent> editRoutineLauncher;

    private ChoreList choreList;

    private SimpleListAdapter<Routine> routineListAdapter;

    private Routine routineUnderEdit;
    private GlobalState globalState;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();

        View rootView = inflater.inflate(R.layout.fragment_all_routines, container, false);

        routineListAdapter = new SimpleListAdapter<>(
            requireContext(),
            choreList::getAllRoutines,
            Routine::getName);
        ListView routineListView = rootView.findViewById(R.id.all_routines);
        routineListView.setAdapter(routineListAdapter);
        routineListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Routine routine = (Routine)routineListAdapter.getItem(position);
            assert routine != null;
            startRoutineEditor(routine, DayRoutineActivity.ROUTINE_ACTION_EDIT);
        });

        Button addDayButton = rootView.findViewById(R.id.add_day_button);
        addDayButton.setOnClickListener(v ->
        {
            Routine routine = new DayRoutine("", LocalDateTime.now());
            startRoutineEditor(routine, DayRoutineActivity.ROUTINE_ACTION_ADD);
        });

        Button addWeekButton = rootView.findViewById(R.id.add_week_button);
        addWeekButton.setOnClickListener(v ->
        {
            Routine routine = new WeekRoutine("", LocalDateTime.now());
            startRoutineEditor(routine, WeekRoutineActivity.ROUTINE_ACTION_ADD);
        });

        Button addFortnightButton = rootView.findViewById(R.id.add_fortnight_button);
        addFortnightButton.setOnClickListener(v ->
        {
            Routine routine = new FortnightRoutine(
                "",
                LocalDate.now(),
                LocalDateTime.now());
            startRoutineEditor(routine, FortnightRoutineActivity.ROUTINE_ACTION_ADD);
        });

        editRoutineLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editRoutineCallback);

        return rootView;
    }

    private void startRoutineEditor(Routine routine, int mode)
    {
        routineUnderEdit = routine;

        globalState.ActiveRoutine = routine;

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

    private void editRoutineCallback(ActivityResult activityResult)
    {
        if (activityResult.getResultCode() == RESULT_OK)
        {
            Routine routine = routineUnderEdit;

            Intent intent = Objects.requireNonNull(activityResult.getData());

            switch (intent.getIntExtra("RESULT", -1))
            {
                case DayRoutineActivity.ROUTINE_RESULT_SAVE:

                    if (intent.getIntExtra("ACTION", -1) == DayRoutineActivity.ROUTINE_ACTION_ADD)
                    {
                        choreList.addRoutine(routine);
                    }

                    break;

                case DayRoutineActivity.ROUTINE_RESULT_REMOVE:
//                    routineAdapter.remove(routine);
                    choreList.removeRoutine(routine);
                    break;
            }
        }

        choreList.save();

        routineListAdapter.notifyDataSetChanged();

        LocalDateTime nextAlarmTime = choreList.getNextRoutineProcedureTime();

        if (nextAlarmTime == null)
        {
            RoutineNotifier.cancelRoutineAlarm(requireContext());
        }
        else
        {
            RoutineNotifier.scheduleRoutineAlarm(requireContext(), nextAlarmTime);
        }
    }
}
