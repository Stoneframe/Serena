package stoneframe.serena.gui.tasks;

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

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.CheckboxListAdapter;
import stoneframe.serena.model.Serena;
import stoneframe.serena.model.tasks.Task;
import stoneframe.serena.model.tasks.TaskManager;

public class AllTasksFragment extends Fragment
{
    private ActivityResultLauncher<Intent> editTaskLauncher;

    private CheckboxListAdapter<Task> taskAdapter;

    private GlobalState globalState;
    private Serena serena;
    private TaskManager taskManager;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();

        serena = globalState.getSerena();
        taskManager = serena.getTaskManager();

        View rootView = inflater.inflate(R.layout.fragment_all_tasks, container, false);

        taskAdapter = new CheckboxListAdapter<>(
            requireContext(),
            taskManager::getAllTasks,
            Task::getDescription,
            Task::isDone,
            t -> String.format("Deadline: %s", t.getDeadline()));
        taskAdapter.setCheckboxChangedListener((task, isChecked) ->
        {
            if (isChecked)
            {
                taskManager.complete(task);
            }
            else
            {
                taskManager.undo(task);
            }
        });

        ListView taskListView = rootView.findViewById(R.id.all_tasks);
        taskListView.setAdapter(taskAdapter);
        taskListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Task task = (Task)taskAdapter.getItem(position);
            assert task != null;
            startTaskEditor(task, EditTaskActivity.ACTION_EDIT);
        });

        Button addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            Task task = taskManager.createTask();

            startTaskEditor(task, EditTaskActivity.ACTION_ADD);
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

        Intent intent = new Intent(getActivity(), EditTaskActivity.class)
            .putExtra("ACTION", action);

        editTaskLauncher.launch(intent);
    }

    private void editTaskCallback(ActivityResult activityResult)
    {
        taskAdapter.notifyDataSetChanged();
    }
}
