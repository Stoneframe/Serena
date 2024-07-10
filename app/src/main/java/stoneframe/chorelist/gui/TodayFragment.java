package stoneframe.chorelist.gui;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import org.joda.time.LocalDate;

import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.chores.Chore;
import stoneframe.chorelist.model.routines.PendingProcedure;
import stoneframe.chorelist.model.tasks.Task;

public class TodayFragment extends Fragment
{
    private ChoreList choreList;

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

            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage("Until when do you want to ignore this task?")
                .setCancelable(false)
                .setPositiveButton("Tomorrow", (dialog, skipButtonId) ->
                {
                    Task task = (Task)taskAdapter.getItem(position);
                    task.setIgnoreBefore(today.plusDays(1));
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

                            Task task = (Task)taskAdapter.getItem(position);
                            task.setIgnoreBefore(ignoreBefore);
                            taskAdapter.notifyDataSetChanged();
                            choreList.save();
                        },
                        today.getYear(),
                        today.getMonthOfYear() - 1,
                        today.getDayOfMonth());

                    datePickerDialog.show();
                })
                .setNeutralButton("Cancel", (dialog, cancelButtonId) -> dialog.cancel());

            AlertDialog alert = builder.create();
            alert.show();

            return true;
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

        if (adapter.isEmpty())
        {
            textView.setBackgroundColor(darkGreen);
        }
        else
        {
            textView.setBackgroundColor(Color.BLACK);
        }
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
