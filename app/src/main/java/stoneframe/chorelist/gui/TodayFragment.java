package stoneframe.chorelist.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.Task;

public class TodayFragment extends Fragment
{
    private ChoreList choreList;

    private ArrayAdapter<Chore> choreAdapter;
    private ArrayAdapter<Task> taskAdapter;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = (GlobalState)Objects.requireNonNull(getActivity())
            .getApplication();
        choreList = globalState.getChoreList();

        View rootView = inflater.inflate(R.layout.fragment_today, container, false);

        choreAdapter = new ArrayAdapter<>(
            getActivity().getBaseContext(),
            android.R.layout.simple_list_item_checked);
        ListView choreListView = rootView.findViewById(R.id.todays_chores);
        choreListView.setAdapter(choreAdapter);
        choreListView.setOnItemClickListener((parent, view, position, id) ->
        {
            CheckedTextView checkedTextView = (CheckedTextView)view;

            checkedTextView.setChecked(true);

            new Thread(() ->
                getActivity().runOnUiThread(() ->
                {
                    waitOneSecond();
                    Chore chore = choreAdapter.getItem(position);
                    choreList.choreDone(chore);
                    choreAdapter.remove(chore);
                    checkedTextView.setChecked(false);
                })).start();

        });
        choreListView.setOnItemLongClickListener((parent, view, position, id) ->
        {
            Chore chore = choreList.getTodaysChores().get(position);
            choreList.choreSkip(chore);
            choreAdapter.clear();
            choreAdapter.addAll(choreList.getTodaysChores());
            return true;
        });

        taskAdapter = new ArrayAdapter<>(
            getActivity().getBaseContext(),
            android.R.layout.simple_list_item_checked);
        ListView taskListView = rootView.findViewById(R.id.todays_tasks);
        taskListView.setAdapter(taskAdapter);
        taskListView.setOnItemClickListener((parent, view, position, id) ->
        {
            CheckedTextView checkedTextView = (CheckedTextView)view;

            checkedTextView.setChecked(true);

            new Thread(() ->
                getActivity().runOnUiThread(() ->
                {
                    waitOneSecond();
                    Task task = taskAdapter.getItem(position);
                    choreList.taskDone(task);
                    taskAdapter.remove(task);
                    checkedTextView.setChecked(false);
                })).start();
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        choreAdapter.addAll(choreList.getTodaysChores());
        taskAdapter.addAll(choreList.getTodaysTasks());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        choreAdapter.clear();
        taskAdapter.clear();
    }

    private static void waitOneSecond()
    {
        try
        {
            TimeUnit.SECONDS.sleep(1);
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
        }
    }
}
