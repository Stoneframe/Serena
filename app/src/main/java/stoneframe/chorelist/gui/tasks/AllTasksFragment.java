package stoneframe.chorelist.gui.tasks;

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

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.util.CheckboxListAdapter;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.tasks.Task;

public class AllTasksFragment extends Fragment
{
    private ActivityResultLauncher<Intent> editTaskLauncher;

    private CheckboxListAdapter<Task> taskAdapter;

    private GlobalState globalState;
    private ChoreList choreList;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();

        View rootView = inflater.inflate(R.layout.fragment_all_tasks, container, false);

        taskAdapter = new CheckboxListAdapter<>(
            requireContext(),
            choreList::getAllTasks,
            Task::getDescription,
            Task::isDone,
            t -> String.format("Deadline: %s", t.getDeadline()));
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
            Task task = (Task)taskAdapter.getItem(position);
            assert task != null;
            startTaskEditor(task, TaskActivity.TASK_ACTION_EDIT);
        });

        Button addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            Task task = new Task(
                "",
                LocalDate.now(),
                LocalDate.now());

            startTaskEditor(task, TaskActivity.TASK_ACTION_ADD);
        });

        editTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editTaskCallback);

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        taskAdapter.notifyDataSetChanged();
    }

    private void startTaskEditor(Task task, int action)
    {
        globalState.setActiveTask(task);

        Intent intent = new Intent(getActivity(), TaskActivity.class)
            .putExtra("ACTION", action);

        editTaskLauncher.launch(intent);
    }

    private void editTaskCallback(ActivityResult activityResult)
    {
        taskAdapter.notifyDataSetChanged();
    }
}
