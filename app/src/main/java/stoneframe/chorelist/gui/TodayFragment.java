package stoneframe.chorelist.gui;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import java.util.Objects;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.PendingProcedure;
import stoneframe.chorelist.model.Procedure;
import stoneframe.chorelist.model.Task;

public class TodayFragment extends Fragment
{
    private ChoreList choreList;

    private SimpleCheckboxListAdapter<PendingProcedure> procedureAdapter;
    private SimpleCheckboxListAdapter<Chore> choreAdapter;
    private SimpleCheckboxListAdapter<Task> taskAdapter;

    private View rootView;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = (GlobalState)Objects.requireNonNull(getActivity())
            .getApplication();
        choreList = globalState.getChoreList();

        rootView = inflater.inflate(R.layout.fragment_today, container, false);

        procedureAdapter = new SimpleCheckboxListAdapter<>(
            getActivity().getBaseContext(),
            choreList::getFirstPendingProcedures,
            PendingProcedure::toString);
        procedureAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView procedureListView = rootView.findViewById(R.id.todays_routines);
        procedureListView.setAdapter(procedureAdapter);
        procedureListView.setOnItemClickListener((parent, view, position, id) ->
        {
            PendingProcedure procedure = (PendingProcedure)procedureAdapter.getItem(position);

            procedureAdapter.setChecked(procedure);

            new Thread(() ->
            {
                waitTwoSeconds();
                getActivity().runOnUiThread(() ->
                {
                    choreList.procedureDone(procedure);
                    procedureAdapter.notifyDataSetChanged();
                    RoutineNotifier.updateNotification(getContext(), choreList);
                });
            }).start();
        });

        choreAdapter = new SimpleCheckboxListAdapter<>(
            getActivity().getBaseContext(),
            choreList::getTodaysChores,
            Chore::getDescription);
        choreAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView choreListView = rootView.findViewById(R.id.todays_chores);
        choreListView.setAdapter(choreAdapter);
        choreListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Chore chore = (Chore)choreAdapter.getItem(position);

            choreAdapter.setChecked(chore);

            new Thread(() ->
            {
                waitTwoSeconds();
                getActivity().runOnUiThread(() ->
                {
                    choreList.choreDone(chore);
                    choreAdapter.notifyDataSetChanged();
                });
            }).start();
        });
        choreListView.setOnItemLongClickListener((parent, view, position, id) ->
        {
            Chore chore = choreList.getTodaysChores().get(position);
            choreList.choreSkip(chore);
            choreAdapter.notifyDataSetChanged();
            return true;
        });

        taskAdapter = new SimpleCheckboxListAdapter<>(
            getActivity().getBaseContext(),
            choreList::getTodaysTasks,
            Task::getDescription);
        taskAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView taskListView = rootView.findViewById(R.id.todays_tasks);
        taskListView.setAdapter(taskAdapter);
        taskListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Task task = (Task)taskAdapter.getItem(position);

            taskAdapter.setChecked(task);

            new Thread(() ->
            {
                waitTwoSeconds();
                getActivity().runOnUiThread(() ->
                {
                    choreList.taskDone(task);
                    taskAdapter.notifyDataSetChanged();
                });
            }).start();
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        procedureAdapter.notifyDataSetChanged();
        choreAdapter.notifyDataSetChanged();
        taskAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    private void updateColors()
    {
        updateColorsOf(procedureAdapter, rootView.findViewById(R.id.routines_text));
        updateColorsOf(choreAdapter, rootView.findViewById(R.id.chores_text));
        updateColorsOf(taskAdapter, rootView.findViewById(R.id.tasks_text));
    }

    private void updateColorsOf(ListAdapter adapter, TextView textView)
    {
        final int darkGreen = Color.parseColor("#228C22");

        if (adapter.isEmpty())
        {
            textView.setBackgroundColor(darkGreen);
        }
        else
        {
            textView.setBackgroundColor(Color.BLACK);
        }
    }

    private static void waitTwoSeconds()
    {
        try
        {
            Thread.sleep(2_000);
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
        }
    }

    private class TodayDataSetObserver extends DataSetObserver
    {
        @Override
        public void onChanged()
        {
            updateColors();
        }
    }
}
