package stoneframe.chorelist.gui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import org.joda.time.DateTime;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.DayRoutine;
import stoneframe.chorelist.model.FortnightRoutine;
import stoneframe.chorelist.model.Routine;
import stoneframe.chorelist.model.WeekRoutine;

public class AllRoutinesFragment extends Fragment
{
    private ChoreList choreList;

    private ArrayAdapter<Routine> routineAdapter;

    private Routine routineUnderEdit;
    private GlobalState globalState;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance(getActivity());

        choreList = globalState.getChoreList();

        View rootView = inflater.inflate(R.layout.fragment_all_routines, container, false);

        routineAdapter = new ArrayAdapter<>(
            getActivity().getBaseContext(),
            android.R.layout.simple_list_item_1);
        ListView routineListView = rootView.findViewById(R.id.all_routines);
        routineListView.setAdapter(routineAdapter);
        routineListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Routine routine = routineAdapter.getItem(position);
            assert routine != null;
            startRoutineEditor(routine, DayRoutineActivity.ROUTINE_ACTION_EDIT);
        });

        Button addDayButton = rootView.findViewById(R.id.add_day_button);
        addDayButton.setOnClickListener(v ->
        {
            Routine routine = new DayRoutine("", DateTime.now());
            startRoutineEditor(routine, DayRoutineActivity.ROUTINE_ACTION_ADD);
        });

        Button addWeekButton = rootView.findViewById(R.id.add_week_button);
        addWeekButton.setOnClickListener(v ->
        {
            Routine routine = new WeekRoutine("", DateTime.now());
            startRoutineEditor(routine, WeekRoutineActivity.ROUTINE_ACTION_ADD);
        });

        Button addFortnightButton = rootView.findViewById(R.id.add_fortnight_button);
        addFortnightButton.setOnClickListener(v ->
        {
            Routine routine = new FortnightRoutine(
                "",
                DateTime.now().toLocalDate(),
                DateTime.now());
            startRoutineEditor(routine, FortnightRoutineActivity.ROUTINE_ACTION_ADD);
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        routineAdapter.addAll(choreList.getAllRoutines());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        routineAdapter.clear();
    }

    private void startRoutineEditor(Routine routine, int mode)
    {
        routineUnderEdit = routine;

        globalState.RoutineToEdit = routine;

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

        startActivityForResult(intent, mode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK)
        {
            Routine routine = routineUnderEdit;

            switch (intent.getIntExtra("RESULT", -1))
            {
                case DayRoutineActivity.ROUTINE_RESULT_SAVE:

                    if (requestCode == DayRoutineActivity.ROUTINE_ACTION_ADD)
                    {
                        choreList.addRoutine(routine);
                    }

                    break;

                case DayRoutineActivity.ROUTINE_RESULT_REMOVE:
                    routineAdapter.remove(routine);
                    choreList.removeRoutine(routine);
                    break;
            }
        }

        DateTime nextAlarmTime = choreList.getNextRoutineProcedureTime();

        if (nextAlarmTime == null)
        {
            RoutineNotifier.cancelRoutineAlarm(getContext());
        }
        else
        {
            RoutineNotifier.scheduleRoutineAlarm(getContext(), nextAlarmTime);
        }
    }
}
