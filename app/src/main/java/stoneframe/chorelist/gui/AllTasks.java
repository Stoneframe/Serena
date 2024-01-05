package stoneframe.chorelist.gui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import org.joda.time.DateTime;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.Task;

public class AllTasks extends Fragment
{
    private static final int ACTIVITY_ADD_TASK = 0;
    private static final int ACTIVITY_EDIT_TASK = 1;

    private Schedule schedule;

    private ArrayAdapter<Task> taskAdapter;
    private ListView taskList;
    private Button addButton;

    private Task taskUnderEdit;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = (GlobalState)getActivity().getApplication();
        schedule = globalState.getSchedule();

        View view = inflater.inflate(R.layout.fragment_all_tasks, container, false);

        taskAdapter = new ArrayAdapter<Task>(
            getActivity().getBaseContext(),
            android.R.layout.simple_list_item_1);
        taskList = (ListView)view.findViewById(R.id.all_tasks);
        taskList.setAdapter(taskAdapter);
        taskList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Task task = taskAdapter.getItem(position);
                startTaskEditor(task, ACTIVITY_EDIT_TASK);
            }
        });
        taskList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                Task task = taskAdapter.getItem(position);
                taskAdapter.remove(task);
                schedule.removeTask(task);
                return true;
            }
        });

        addButton = (Button)view.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Task duty = new Task(
                    "",
                    1,
                    1,
                    DateTime.now().withTimeAtStartOfDay(),
                    Task.DAILY,
                    1);
                startTaskEditor(duty, ACTIVITY_ADD_TASK);
            }
        });

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        taskAdapter.addAll(schedule.getAllTasks());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        taskAdapter.clear();
    }

    private void startTaskEditor(Task task, int mode)
    {
        taskUnderEdit = task;

        Intent intent = new Intent(getActivity(), TaskActivity.class);
        intent.putExtra("Next", task.getNext());
        intent.putExtra("Description", task.getDescription());
        intent.putExtra("Priority", task.getPriority());
        intent.putExtra("Effort", task.getEffort());
        intent.putExtra("Periodicity", task.getPeriodicity());
        intent.putExtra("Frequency", task.getFrequency());

        startActivityForResult(intent, mode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            Task duty = taskUnderEdit;

            duty.setNext((DateTime)data.getSerializableExtra("Next"));
            duty.setDescription(data.getStringExtra("Description"));
            duty.setPriority(data.getIntExtra("Priority", 1));
            duty.setEffort(data.getIntExtra("Effort", 1));
            duty.setPeriodicity(data.getIntExtra("Periodicity", 1));
            duty.setFrequency(data.getIntExtra("Frequency", 1));

            if (requestCode == ACTIVITY_ADD_TASK)
            {
                schedule.addTask(duty);
            }
        }
    }
}
