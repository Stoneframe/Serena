package stoneframe.chorelist.gui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import org.joda.time.DateTime;

import java.util.Objects;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Task;

public class AllTasksFragment extends Fragment
{
    private ChoreList choreList;

    private TaskArrayAdapter taskAdapter;

    private Task taskUnderEdit;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = (GlobalState)Objects.requireNonNull(getActivity())
            .getApplication();
        choreList = globalState.getChoreList();

        View rootView = inflater.inflate(R.layout.fragment_all_tasks, container, false);

        taskAdapter = new TaskArrayAdapter(getActivity().getBaseContext());
        taskAdapter.setCheckboxChangedListener((task, isChecked) ->
        {
            if (isChecked)
            {
                choreList.taskDone(task);
            }
            else
            {
                choreList.taskUndone(task);
            }
        });

        ListView taskListView = rootView.findViewById(R.id.all_tasks);
        taskListView.setAdapter(taskAdapter);
        taskListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Task chore = taskAdapter.getItem(position);
            assert chore != null;
            startTaskEditor(chore, TaskActivity.TASK_ACTION_EDIT);
        });

        Button addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            Task task = new Task(
                "",
                DateTime.now().withTimeAtStartOfDay(),
                DateTime.now().withTimeAtStartOfDay());

            startTaskEditor(task, TaskActivity.TASK_ACTION_ADD);
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        taskAdapter.addAll(choreList.getAllTasks(true));
    }

    @Override
    public void onStop()
    {
        super.onStop();

        taskAdapter.clear();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        taskAdapter.notifyDataSetChanged();
    }

    private void startTaskEditor(Task task, int mode)
    {
        taskUnderEdit = task;

        Intent intent = new Intent(getActivity(), TaskActivity.class);

        intent.putExtra("ACTION", mode);
        intent.putExtra("Description", task.getDescription());
        intent.putExtra("Deadline", task.getDeadline());
        intent.putExtra("IgnoreBefore", task.getIgnoreBefore());
        intent.putExtra("IsDone", task.isDone());

        startActivityForResult(intent, mode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK)
        {
            Task task = taskUnderEdit;

            switch (intent.getIntExtra("RESULT", -1))
            {
                case TaskActivity.TASK_RESULT_SAVE:
                    task.setDescription(intent.getStringExtra("Description"));
                    task.setDeadline((DateTime)intent.getSerializableExtra("Deadline"));
                    task.setIgnoreBefore((DateTime)intent.getSerializableExtra("IgnoreBefore"));

                    if (requestCode == TaskActivity.TASK_ACTION_ADD)
                    {
                        choreList.addTask(task);
                    }

                    boolean isDone = intent.getBooleanExtra("IsDone", false);

                    if (isDone == task.isDone()) return;

                    if (isDone)
                    {
                        choreList.taskDone(task);
                    }
                    else
                    {
                        choreList.taskUndone(task);
                    }

                    break;
                case TaskActivity.TASK_RESULT_REMOVE:
                    taskAdapter.remove(task);
                    choreList.removeTask(task);
                    break;
            }
        }

        choreList.save();
    }
}
