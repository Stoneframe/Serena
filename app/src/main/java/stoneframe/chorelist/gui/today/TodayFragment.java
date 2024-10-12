package stoneframe.chorelist.gui.today;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
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

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.routines.RoutineNotifier;
import stoneframe.chorelist.gui.tasks.EditTaskActivity;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.SimpleCheckboxListAdapter;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.chores.Chore;
import stoneframe.chorelist.model.routines.PendingProcedure;
import stoneframe.chorelist.model.tasks.Task;
import stoneframe.chorelist.model.tasks.TaskEditor;

public class TodayFragment extends Fragment
{
    private ChoreList choreList;

    private ActivityResultLauncher<Intent> editTaskLauncher;

    private SimpleCheckboxListAdapter<PendingProcedure> procedureAdapter;
    private SimpleCheckboxListAdapter<Chore> choreAdapter;
    private SimpleCheckboxListAdapter<Task> taskAdapter;

    private View rootView;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();

        rootView = inflater.inflate(R.layout.fragment_today, container, false);

        procedureAdapter = new SimpleCheckboxListAdapter<>(
            requireContext(),
            choreList::getFirstPendingProcedures,
            PendingProcedure::toString);
        procedureAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView procedureListView = rootView.findViewById(R.id.todays_routines);
        procedureListView.setAdapter(procedureAdapter);
        procedureListView.setOnItemClickListener((parent, view, position, id) ->
        {
            PendingProcedure procedure = (PendingProcedure)procedureAdapter.getItem(position);

            procedureAdapter.setChecked(procedure);

            new Thread(() ->
            {
                waitTwoSeconds();
                requireActivity().runOnUiThread(() ->
                {
                    choreList.procedureDone(procedure);
                    procedureAdapter.notifyDataSetChanged();
                    RoutineNotifier.updateNotification(getContext(), choreList);
                    choreList.save();
                });
            }).start();
        });

        choreAdapter = new SimpleCheckboxListAdapter<>(
            requireContext(),
            choreList::getTodaysChores,
            Chore::getDescription);
        choreAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView choreListView = rootView.findViewById(R.id.todays_chores);
        choreListView.setAdapter(choreAdapter);
        choreListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Chore chore = (Chore)choreAdapter.getItem(position);

            choreAdapter.setChecked(chore);

            new Thread(() ->
            {
                waitTwoSeconds();
                requireActivity().runOnUiThread(() ->
                {
                    choreList.choreDone(chore);
                    choreAdapter.notifyDataSetChanged();
                    choreList.save();
                });
            }).start();
        });
        choreListView.setOnItemLongClickListener((parent, view, position, id) ->
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Do you want to skip or postpone this chore?")
                .setCancelable(false)
                .setPositiveButton("Skip", (dialog, skipButtonId) ->
                {
                    Chore chore = choreList.getTodaysChores().get(position);
                    choreList.choreSkip(chore);
                    choreAdapter.notifyDataSetChanged();
                    choreList.save();
                })
                .setNegativeButton("Postpone", (dialog, postponeButtonId) ->
                {
                    Chore chore = choreList.getTodaysChores().get(position);
                    choreList.chorePostpone(chore);
                    choreAdapter.notifyDataSetChanged();
                    choreList.save();
                })
                .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();

            return true;
        });

        taskAdapter = new SimpleCheckboxListAdapter<>(
            requireContext(),
            choreList::getTodaysTasks,
            Task::getDescription);
        taskAdapter.registerDataSetObserver(new TodayDataSetObserver());
        ListView taskListView = rootView.findViewById(R.id.todays_tasks);
        taskListView.setAdapter(taskAdapter);
        taskListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Task task = (Task)taskAdapter.getItem(position);

            taskAdapter.setChecked(task);

            new Thread(() ->
            {
                waitTwoSeconds();
                requireActivity().runOnUiThread(() ->
                {
                    choreList.taskDone(task);
                    taskAdapter.notifyDataSetChanged();
                    choreList.save();
                });
            }).start();
        });
        taskListView.setOnItemLongClickListener((parent, view, position, id) ->
        {
            LocalDate today = LocalDate.now();

            Task task = (Task)taskAdapter.getItem(position);

            if (task.getDeadline().isEqual(task.getIgnoreBefore()))
            {
                DialogUtils.showWarningDialog(
                    requireContext(),
                    "Deadline is today!",
                    "Cannot ignore after deadline.");

                return true;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Until when do you want to ignore this task?")
                .setCancelable(false)
                .setPositiveButton("Tomorrow", (dialog, skipButtonId) ->
                {
                    TaskEditor taskEditor = choreList.getTaskEditor(task);
                    taskEditor.setIgnoreBefore(today.plusDays(1));
                    taskEditor.save();

                    taskAdapter.notifyDataSetChanged();
                    choreList.save();
                })
                .setNegativeButton("Choose...", (dialog, postponeButtonId) ->
                {
                    DatePickerDialog datePickerDialog = new DatePickerDialog(
                        requireContext(),
                        (v, year, month, dayOfMonth) ->
                        {
                            LocalDate ignoreBefore = new LocalDate(year, month + 1, dayOfMonth);

                            TaskEditor taskEditor = choreList.getTaskEditor(task);
                            taskEditor.setIgnoreBefore(ignoreBefore);
                            taskEditor.save();

                            taskAdapter.notifyDataSetChanged();
                            choreList.save();
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

                    choreList.getPendingProcedures().forEach(p -> choreList.procedureDone(p));
                    choreList.save();

                    procedureAdapter.notifyDataSetChanged();

                    RoutineNotifier.updateNotification(getContext(), choreList);
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

                    choreList.getEffortTracker().reset(LocalDate.now());
                    choreList.save();

                    choreAdapter.notifyDataSetChanged();
                }
            ));

        editTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editTaskCallback);

        ImageButton addTaskButton = rootView.findViewById(R.id.addTaskButton);
        addTaskButton.setOnClickListener(v ->
        {
            Task task = choreList.createTask();

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

        procedureAdapter.notifyDataSetChanged();
        choreAdapter.notifyDataSetChanged();
        taskAdapter.notifyDataSetChanged();

        updateColors();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        updateColors();
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
