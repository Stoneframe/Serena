package stoneframe.serena.gui.today;

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

import java.util.Objects;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.routines.RoutineNotifier;
import stoneframe.serena.gui.tasks.TaskActivity;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.gui.util.SimpleCheckboxListAdapter;
import stoneframe.serena.model.Serena;
import stoneframe.serena.model.chores.Chore;
import stoneframe.serena.model.routines.PendingProcedure;
import stoneframe.serena.model.tasks.Task;

public class TodayFragment extends Fragment
{
    private Serena serena;

    private ActivityResultLauncher<Intent> editTaskLauncher;
    private Task taskUnderEdit;

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

        serena = globalState.getSerena();

        rootView = inflater.inflate(R.layout.fragment_today, container, false);

        procedureAdapter = new SimpleCheckboxListAdapter<>(
            requireContext(),
            serena::getFirstPendingProcedures,
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
                    serena.procedureDone(procedure);
                    procedureAdapter.notifyDataSetChanged();
                    RoutineNotifier.updateNotification(getContext(), serena);
                    serena.save();
                });
            }).start();
        });

        choreAdapter = new SimpleCheckboxListAdapter<>(
            requireContext(),
            serena::getTodaysChores,
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
                    serena.choreDone(chore);
                    choreAdapter.notifyDataSetChanged();
                    serena.save();
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
                    Chore chore = serena.getTodaysChores().get(position);
                    serena.choreSkip(chore);
                    choreAdapter.notifyDataSetChanged();
                    serena.save();
                })
                .setNegativeButton("Postpone", (dialog, postponeButtonId) ->
                {
                    Chore chore = serena.getTodaysChores().get(position);
                    serena.chorePostpone(chore);
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
            serena::getTodaysTasks,
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
                    serena.taskDone(task);
                    taskAdapter.notifyDataSetChanged();
                    serena.save();
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
                    task.setIgnoreBefore(today.plusDays(1));
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

                            task.setIgnoreBefore(ignoreBefore);
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

                    serena.getPendingProcedures().forEach(p -> serena.procedureDone(p));
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

                    serena.getEffortTracker().reset(LocalDate.now());
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
            taskUnderEdit = new Task("", LocalDate.now(), LocalDate.now());

            Intent intent = new Intent(getActivity(), TaskActivity.class);

            intent.putExtra("ACTION", TaskActivity.TASK_ACTION_ADD);
            intent.putExtra("Description", taskUnderEdit.getDescription());
            intent.putExtra("Deadline", taskUnderEdit.getDeadline());
            intent.putExtra("IgnoreBefore", taskUnderEdit.getIgnoreBefore());
            intent.putExtra("IsDone", taskUnderEdit.isDone());

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

        Task task = taskUnderEdit;

        Intent intent = Objects.requireNonNull(activityResult.getData());

        task.setDescription(intent.getStringExtra("Description"));
        task.setDeadline((LocalDate)intent.getSerializableExtra("Deadline"));
        task.setIgnoreBefore((LocalDate)intent.getSerializableExtra("IgnoreBefore"));

        if (intent.getIntExtra("ACTION", -1) == TaskActivity.TASK_ACTION_ADD)
        {
            serena.addTask(task);
        }

        boolean isDone = intent.getBooleanExtra("IsDone", false);

        if (isDone != task.isDone())
        {
            if (isDone)
            {
                serena.taskDone(task);
            }
            else
            {
                serena.taskUndone(task);
            }
        }

        serena.save();
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
