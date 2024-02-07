package stoneframe.chorelist.gui;

import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.joda.time.DateTime;

import java.util.Objects;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.Procedure;
import stoneframe.chorelist.model.Task;

public class TodayFragment extends Fragment
{
    private ChoreList choreList;

    private ArrayAdapter<Procedure> procedureAdapter;
    private ArrayAdapter<Chore> choreAdapter;
    private ArrayAdapter<Task> taskAdapter;

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

        procedureAdapter = new SimpleCheckboxArrayAdapter<>(
            getActivity().getBaseContext(),
            Procedure::getDescription,
            p -> !choreList.getPendingProcedures().contains(p));
        procedureAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView procedureListView = rootView.findViewById(R.id.todays_routines);
        procedureListView.setAdapter(procedureAdapter);
        procedureListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Procedure procedure = procedureAdapter.getItem(position);

            choreList.procedureDone(procedure);
            procedureAdapter.notifyDataSetChanged();

            new Thread(() ->
            {
                waitTwoSeconds();
                getActivity().runOnUiThread(() ->
                {
                    procedureAdapter.clear();
                    procedureAdapter.addAll(choreList.getPendingProcedures());
                });
            }).start();
        });

        choreAdapter = new SimpleCheckboxArrayAdapter<>(
            getActivity().getBaseContext(),
            Chore::getDescription,
            chore -> chore.getNext().isAfter(DateTime.now()));
        choreAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView choreListView = rootView.findViewById(R.id.todays_chores);
        choreListView.setAdapter(choreAdapter);
        choreListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Chore chore = choreAdapter.getItem(position);

            if (chore == null || chore.getNext().isAfter(DateTime.now()))
            {
                return;
            }

            choreList.choreDone(chore);
            choreAdapter.notifyDataSetChanged();

            new Thread(() ->
            {
                waitTwoSeconds();
                getActivity().runOnUiThread(() -> choreAdapter.remove(chore));
            }).start();
        });
        choreListView.setOnItemLongClickListener((parent, view, position, id) ->
        {
            Chore chore = choreList.getTodaysChores().get(position);
            choreList.choreSkip(chore);
            choreAdapter.clear();
            choreAdapter.addAll(choreList.getTodaysChores());
            return true;
        });

        taskAdapter = new SimpleCheckboxArrayAdapter<>(
            getActivity().getBaseContext(),
            Task::getDescription,
            Task::isDone);
        taskAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView taskListView = rootView.findViewById(R.id.todays_tasks);
        taskListView.setAdapter(taskAdapter);
        taskListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Task task = taskAdapter.getItem(position);

            if (task == null || task.isDone())
            {
                return;
            }

            choreList.taskDone(task);
            taskAdapter.notifyDataSetChanged();

            new Thread(() ->
            {
                waitTwoSeconds();
                getActivity().runOnUiThread(() -> taskAdapter.remove(task));
            }).start();
        });

        return rootView;
    }

    private void updateColors()
    {
        updateColorsOf(procedureAdapter, rootView.findViewById(R.id.routines_text));
        updateColorsOf(choreAdapter, rootView.findViewById(R.id.chores_text));
        updateColorsOf(taskAdapter, rootView.findViewById(R.id.tasks_text));
    }

    private void updateColorsOf(ArrayAdapter<?> adapter, TextView textView)
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

    @Override
    public void onStart()
    {
        super.onStart();

        procedureAdapter.addAll(choreList.getPendingProcedures());
        choreAdapter.addAll(choreList.getTodaysChores());
        taskAdapter.addAll(choreList.getTodaysTasks());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        procedureAdapter.clear();
        choreAdapter.clear();
        taskAdapter.clear();
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
