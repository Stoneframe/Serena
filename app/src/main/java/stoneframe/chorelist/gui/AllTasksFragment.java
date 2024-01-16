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
    private static final int ACTIVITY_ADD_TASK = 0;
    private static final int ACTIVITY_EDIT_TASK = 1;

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

        View view = inflater.inflate(R.layout.fragment_all_tasks, container, false);

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

        ListView choreListView = view.findViewById(R.id.all_tasks);
        choreListView.setAdapter(taskAdapter);
        choreListView.setOnItemClickListener((parent, view1, position, id) ->
        {
            Task chore = taskAdapter.getItem(position);
            assert chore != null;
            startTaskEditor(chore, ACTIVITY_EDIT_TASK);
        });
        choreListView.setOnItemLongClickListener((parent, view12, position, id) ->
        {
            Task chore = taskAdapter.getItem(position);
            taskAdapter.remove(chore);
            choreList.removeTask(chore);
            return true;
        });

        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            Task task = new Task(
                "",
                DateTime.now().withTimeAtStartOfDay(),
                DateTime.now().withTimeAtStartOfDay());

            startTaskEditor(task, ACTIVITY_ADD_TASK);
        });

        return view;
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
        intent.putExtra("Description", task.getDescription());
        intent.putExtra("Deadline", task.getDeadline());
        intent.putExtra("IgnoreBefore", task.getIgnoreBefore());
        intent.putExtra("IsDone", task.isDone());

        startActivityForResult(intent, mode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            Task task = taskUnderEdit;

            task.setDescription(data.getStringExtra("Description"));
            task.setDeadline((DateTime)data.getSerializableExtra("Deadline"));
            task.setIgnoreBefore((DateTime)data.getSerializableExtra("IgnoreBefore"));

            if (requestCode == ACTIVITY_ADD_TASK)
            {
                choreList.addTask(task);
            }

            boolean isDone = data.getBooleanExtra("IsDone", false);

            if (isDone == task.isDone()) return;

            if (isDone)
            {
                choreList.taskDone(task);
            }
            else
            {
                choreList.taskUndone(task);
            }
        }
    }
}
