package stoneframe.serena.gui.today;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import org.joda.time.LocalDate;

import java.util.Collection;
import java.util.HashMap;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.routines.RoutineNotifier;
import stoneframe.serena.gui.tasks.EditTaskActivity;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.gui.util.SimpleCheckboxListAdapter;
import stoneframe.serena.Serena;
import stoneframe.serena.SerenaChangedListener;
import stoneframe.serena.chores.Chore;
import stoneframe.serena.chores.ChoreManager;
import stoneframe.serena.routines.PendingProcedure;
import stoneframe.serena.routines.RoutineManager;
import stoneframe.serena.sleep.Sleep;
import stoneframe.serena.sleep.SleepManager;
import stoneframe.serena.tasks.Task;
import stoneframe.serena.tasks.TaskEditor;
import stoneframe.serena.tasks.TaskManager;

public class TodayFragment extends Fragment implements SerenaChangedListener
{
    private final HashMap<PendingProcedure, Runnable> procedureRemovals = new HashMap<>();
    private final HashMap<Chore, Runnable> choreRemovals = new HashMap<>();
    private final HashMap<Task, Runnable> taskRemovals = new HashMap<>();

    private final Handler handler = new Handler();

    private ActivityResultLauncher<Intent> editTaskLauncher;

    private SimpleCheckboxListAdapter<PendingProcedure> procedureAdapter;
    private SimpleCheckboxListAdapter<Chore> choreAdapter;
    private SimpleCheckboxListAdapter<Task> taskAdapter;

    private View rootView;

    private RoutineManager routineManager;
    private ChoreManager choreManager;
    private TaskManager taskManager;

    private Serena serena;

    private boolean isShowingSleepDialog;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = GlobalState.getInstance();

        serena = globalState.getSerena();

        routineManager = serena.getRoutineManager();
        choreManager = serena.getChoreManager();
        taskManager = serena.getTaskManager();

        rootView = inflater.inflate(R.layout.fragment_today, container, false);

        procedureAdapter = new SimpleCheckboxListAdapter<>(
            requireContext(),
            routineManager::getFirstPendingProcedures,
            PendingProcedure::toString);
        procedureAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView procedureListView = rootView.findViewById(R.id.todays_routines);
        procedureListView.setAdapter(procedureAdapter);
        procedureListView.setOnItemClickListener((p, v, position, i) -> onProcedureClicked(position));

        choreAdapter = new SimpleCheckboxListAdapter<>(
            requireContext(),
            choreManager::getTodaysChores,
            Chore::getDescription);
        choreAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView choreListView = rootView.findViewById(R.id.todays_chores);
        choreListView.setAdapter(choreAdapter);
        choreListView.setOnItemClickListener((p, v, position, i) -> onChoreClicked(position));
        choreListView.setOnItemLongClickListener((parent, view, position, id) ->
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Do you want to skip or postpone this chore?")
                .setCancelable(false)
                .setPositiveButton("Skip", (dialog, skipButtonId) ->
                {
                    Chore chore = choreManager.getTodaysChores().get(position);
                    choreManager.skip(chore);
                    choreAdapter.notifyDataSetChanged();
                    serena.save();
                })
                .setNegativeButton("Postpone", (dialog, postponeButtonId) ->
                {
                    Chore chore = choreManager.getTodaysChores().get(position);
                    choreManager.postpone(chore);
                    choreAdapter.notifyDataSetChanged();
                    serena.save();
                })
                .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();

            return true;
        });

        taskAdapter = new SimpleCheckboxListAdapter<>(
            requireContext(),
            taskManager::getTodaysTasks,
            Task::getDescription);
        taskAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView taskListView = rootView.findViewById(R.id.todays_tasks);
        taskListView.setAdapter(taskAdapter);
        taskListView.setOnItemClickListener((p, v, position, i) -> onTaskClicked(position));
        taskListView.setOnItemLongClickListener((parent, view, position, id) ->
        {
            LocalDate today = LocalDate.now();

            Task task = (Task)taskAdapter.getItem(position);

            if (!today.isBefore(task.getDeadline()))
            {
                DialogUtils.showWarningDialog(
                    requireContext(),
                    "Has reached deadline!",
                    "Cannot ignore after deadline.");

                return true;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Until when do you want to ignore this task?")
                .setCancelable(false)
                .setPositiveButton("Tomorrow", (dialog, skipButtonId) ->
                {
                    TaskEditor taskEditor = taskManager.getTaskEditor(task);
                    taskEditor.setIgnoreBefore(today.plusDays(1));
                    taskEditor.save();

                    taskAdapter.notifyDataSetChanged();
                    serena.save();
                })
                .setNegativeButton("Choose...", (dialog, postponeButtonId) ->
                {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireContext(),
                        (v, year, month, dayOfMonth) ->
                        {
                            LocalDate ignoreBefore = new LocalDate(year, month + 1, dayOfMonth);

                            TaskEditor taskEditor = taskManager.getTaskEditor(task);
                            taskEditor.setIgnoreBefore(ignoreBefore);
                            taskEditor.save();

                            taskAdapter.notifyDataSetChanged();
                            serena.save();
                        },
                        today.getYear(),
                        today.getMonthOfYear() - 1,
                        today.getDayOfMonth());

                    DatePicker datePicker = datePickerDialog.getDatePicker();
                    datePicker.setMinDate(LocalDate.now().toDateTimeAtStartOfDay().getMillis());
                    datePicker.setMaxDate(task.getDeadline().toDateTimeAtStartOfDay().getMillis());

                    datePickerDialog.show();
                })
                .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();

            return true;
        });

        ImageButton clearRoutinesButton = rootView.findViewById(R.id.clearRoutinesButton);
        clearRoutinesButton.setOnClickListener(v ->
            DialogUtils.showConfirmationDialog(
                requireContext(),
                "Clear routines",
                "Are you sure you want to clear all routines?",
                isConfirmed ->
                {
                    if (!isConfirmed) return;

                    routineManager.getPendingProcedures()
                        .forEach(p -> routineManager.procedureDone(p));
                    serena.save();

                    procedureAdapter.notifyDataSetChanged();

                    RoutineNotifier.updateNotification(getContext(), serena);
                }));

        ImageButton refreshChoresButton = rootView.findViewById(R.id.refreshChoresButton);
        refreshChoresButton.setOnClickListener(v ->
            DialogUtils.showConfirmationDialog(
                requireContext(),
                "Refresh chores",
                "Are you sure you want to refresh chores?",
                isConfirmed ->
                {
                    if (!isConfirmed) return;

                    choreManager.getEffortTracker().reset(LocalDate.now());
                    serena.save();

                    choreAdapter.notifyDataSetChanged();
                }
            ));

        editTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editTaskCallback);

        ImageButton addTaskButton = rootView.findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v ->
        {
            Task task = taskManager.createTask();

            globalState.setActiveTask(task);

            Intent intent = new Intent(getActivity(), EditTaskActivity.class)
                .putExtra("ACTION", EditTaskActivity.ACTION_ADD);

            editTaskLauncher.launch(intent);
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        serena.addChangedListener(this);

        procedureAdapter.notifyDataSetChanged();
        choreAdapter.notifyDataSetChanged();
        taskAdapter.notifyDataSetChanged();

        updateColors();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        serena.addChangedListener(this);

        procedureAdapter.notifyDataSetChanged();
        choreAdapter.notifyDataSetChanged();
        taskAdapter.notifyDataSetChanged();

        checkIfAsleepAndQueryToWakeUp();

        updateColors();
    }

    @Override
    public void onPause()
    {
        super.onPause();

        serena.removeChangedListener(this);

        completePendingRemovals(procedureRemovals);
        completePendingRemovals(choreRemovals);
        completePendingRemovals(taskRemovals);
    }

    @Override
    public synchronized void onStop()
    {
        super.onStop();

        serena.removeChangedListener(this);

        completePendingRemovals(procedureRemovals);
        completePendingRemovals(choreRemovals);
        completePendingRemovals(taskRemovals);
    }

    @Override
    public void onSerenaChanged()
    {
        procedureAdapter.notifyDataSetChanged();
        choreAdapter.notifyDataSetChanged();
        taskAdapter.notifyDataSetChanged();
    }

    private synchronized void checkIfAsleepAndQueryToWakeUp()
    {
        SleepManager sleepManager = serena.getSleepManager();

        if (isShowingSleepDialog)
        {
            return;
        }

        isShowingSleepDialog = true;

        if (sleepManager.getState() == Sleep.ASLEEP)
        {
            DialogUtils.showConfirmationDialog(
                requireContext(),
                "You are asleep",
                "You are asleep. Do you want to wake up?",
                isConfirmed ->
                {
                    if (isConfirmed)
                    {
                        sleepManager.toggle();
                    }

                    isShowingSleepDialog = false;
                });
        }
    }

    private void completePendingRemovals(HashMap<?, Runnable> pendingRemovalsMap)
    {
        Collection<Runnable> pendingRemovals = pendingRemovalsMap.values();

        while (!pendingRemovals.isEmpty())
        {
            Runnable r = pendingRemovals.stream().findFirst().get();
            pendingRemovals.remove(r);
            handler.removeCallbacks(r);
            r.run();
        }
    }

    private synchronized void onProcedureClicked(int position)
    {
        PendingProcedure procedure = (PendingProcedure)procedureAdapter.getItem(position);
        if (procedureRemovals.containsKey(procedure))
        {
            procedureAdapter.setUnchecked(procedure);

            Runnable removeRunnable = procedureRemovals.remove(procedure);
            assert removeRunnable != null;
            handler.removeCallbacks(removeRunnable);
        }
        else
        {
            procedureAdapter.setChecked(procedure);

            Runnable removeRunnable = () -> removeProcedure(procedure);

            procedureRemovals.put(procedure, removeRunnable);
            handler.postDelayed(removeRunnable, 2000);
        }
    }

    private synchronized void onChoreClicked(int position)
    {
        Chore chore = (Chore)choreAdapter.getItem(position);

        if (choreRemovals.containsKey(chore))
        {
            choreAdapter.setUnchecked(chore);

            Runnable removeRunnable = choreRemovals.remove(chore);
            assert removeRunnable != null;
            handler.removeCallbacks(removeRunnable);
        }
        else
        {
            choreAdapter.setChecked(chore);

            Runnable removeRunnable = () -> removeChore(chore);

            choreRemovals.put(chore, removeRunnable);
            handler.postDelayed(removeRunnable, 2000);
        }
    }

    private synchronized void onTaskClicked(int position)
    {
        Task task = (Task)taskAdapter.getItem(position);

        if (taskRemovals.containsKey(task))
        {
            taskAdapter.setUnchecked(task);

            Runnable removeRunnable = taskRemovals.remove(task);
            assert removeRunnable != null;
            handler.removeCallbacks(removeRunnable);
        }
        else
        {
            taskAdapter.setChecked(task);

            Runnable removeRunnable = () -> removeTask(task);

            taskRemovals.put(task, removeRunnable);
            handler.postDelayed(removeRunnable, 2000);
        }
    }

    private synchronized void removeProcedure(PendingProcedure procedure)
    {
        procedureRemovals.remove(procedure);
        routineManager.procedureDone(procedure);
        procedureAdapter.notifyDataSetChanged();
        RoutineNotifier.updateNotification(getContext(), serena);
        serena.save();
    }

    private synchronized void removeChore(Chore chore)
    {
        choreRemovals.remove(chore);
        choreManager.complete(chore);
        choreAdapter.notifyDataSetChanged();
        serena.save();
    }

    private synchronized void removeTask(Task task)
    {
        taskRemovals.remove(task);
        taskManager.complete(task);
        taskAdapter.notifyDataSetChanged();
        serena.save();
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

        RelativeLayout parent = (RelativeLayout)textView.getParent();

        if (adapter.isEmpty())
        {
            parent.setBackgroundColor(darkGreen);
        }
        else
        {
            parent.setBackgroundColor(Color.BLACK);
        }
    }

    private void editTaskCallback(ActivityResult activityResult)
    {
        if (activityResult.getResultCode() != RESULT_OK)
        {
            return;
        }

        taskAdapter.notifyDataSetChanged();
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
