package stoneframe.chorelist.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import org.joda.time.DateTime;

import java.util.Objects;

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

        choreAdapter = new SimpleCheckboxArrayAdapter<>(
            getActivity().getBaseContext(),
            Chore::getDescription,
            chore -> chore.getNext().isAfter(DateTime.now()));
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
                waitOneSecond();
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
                waitOneSecond();
                getActivity().runOnUiThread(() -> taskAdapter.remove(task));
            }).start();
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
            Thread.sleep(2_000);
        }
        catch (InterruptedException ie)
        {
            Thread.currentThread().interrupt();
        }
    }
}
