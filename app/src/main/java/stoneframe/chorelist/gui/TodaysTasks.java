package stoneframe.chorelist.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.Task;

public class TodaysTasks extends Fragment
{
    private Schedule schedule;

    private ArrayAdapter<Task> taskAdapter;
    private ListView taskList;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = (GlobalState)getActivity().getApplication();
        schedule = globalState.getSchedule();

        View view = inflater.inflate(R.layout.fragment_todays_tasks, container, false);

        taskAdapter = new ArrayAdapter<>(
            getActivity().getBaseContext(),
            android.R.layout.simple_list_item_1);
        taskList = (ListView)view.findViewById(R.id.todays_tasks);
        taskList.setAdapter(taskAdapter);
        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Task task = taskAdapter.getItem(position);
                schedule.complete(task);
                taskAdapter.remove(task);
            }
        });
        taskList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                Task task = schedule.getTasks().get(position);
                schedule.skip(task);
                taskAdapter.clear();
                taskAdapter.addAll(schedule.getTasks());
                return true;
            }
        });

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        taskAdapter.addAll(schedule.getTasks());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        taskAdapter.clear();
    }
}
