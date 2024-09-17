package stoneframe.chorelist.gui.chores;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import org.joda.time.LocalDate;

import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.util.SimpleListAdapter;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.chores.Chore;
import stoneframe.chorelist.model.chores.efforttrackers.WeeklyEffortTracker;

public class AllChoresFragment extends Fragment
{
    private static final int SORT_BY_DESCRIPTION = 0;
    private static final int SORT_BY_PRIORITY = 1;

    private int sortBy = SORT_BY_DESCRIPTION;

    private ActivityResultLauncher<Intent> editChoreLauncher;
    private ActivityResultLauncher<Intent> editEffortLauncher;

    private ChoreList choreList;

    private SimpleListAdapter<Chore> choreListAdapter;

    private Chore choreUnderEdit;

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();

        View rootView = inflater.inflate(R.layout.fragment_all_chores, container, false);

        choreListAdapter = new SimpleListAdapter<>(
            requireContext(),
            () -> choreList.getAllChores()
                .stream()
                .sorted(getComparator())
                .collect(Collectors.toList()),
            Chore::getDescription,
            c -> c.getNext().toString(),
            c -> String.format("Priority: %d, Effort: %d", c.getPriority(), c.getEffort()));
        ListView choreListView = rootView.findViewById(R.id.all_tasks);
        choreListView.setAdapter(choreListAdapter);
        choreListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Chore chore = (Chore)choreListAdapter.getItem(position);
            assert chore != null;
            startChoreEditor(chore, ChoreActivity.CHORE_ACTION_EDIT);
        });

        Button sortByButton = rootView.findViewById(R.id.sort_by_button);
        sortByButton.setOnClickListener(v -> showSortByDialog());

        Button effortButton = rootView.findViewById(R.id.effort_button);
        effortButton.setOnClickListener(v ->
        {
            WeeklyEffortTracker effortTracker = (WeeklyEffortTracker)choreList.getEffortTracker();

            Intent intent = new Intent(getActivity(), EffortActivity.class);

            intent.putExtra("Monday", effortTracker.getMonday());
            intent.putExtra("Tuesday", effortTracker.getTuesday());
            intent.putExtra("Wednesday", effortTracker.getWednesday());
            intent.putExtra("Thursday", effortTracker.getThursday());
            intent.putExtra("Friday", effortTracker.getFriday());
            intent.putExtra("Saturday", effortTracker.getSaturday());
            intent.putExtra("Sunday", effortTracker.getSunday());

            editEffortLauncher.launch(intent);
        });

        Button addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            Chore chore = new Chore(
                "",
                1,
                1,
                LocalDate.now(),
                1,
                Chore.DAYS);
            startChoreEditor(chore, ChoreActivity.CHORE_ACTION_ADD);
        });

        editChoreLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editChoreCallback);

        editEffortLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editEffortCallback);

        return rootView;
    }

    private Comparator<Chore> getComparator()
    {
        if (sortBy == SORT_BY_DESCRIPTION)
        {
            return Comparator.comparing(Chore::getDescription);
        }

        if (sortBy == SORT_BY_PRIORITY)
        {
            return Comparator.comparing(Chore::getPriority);
        }

        return Comparator.comparing(Chore::getDescription);
    }

    private void startChoreEditor(Chore chore, int mode)
    {
        choreUnderEdit = chore;

        Intent intent = new Intent(getActivity(), ChoreActivity.class);
        intent.putExtra("ACTION", mode);
        intent.putExtra("Next", chore.getNext());
        intent.putExtra("Description", chore.getDescription());
        intent.putExtra("Priority", chore.getPriority());
        intent.putExtra("Effort", chore.getEffort());
        intent.putExtra("IntervalUnit", chore.getIntervalUnit());
        intent.putExtra("IntervalLength", chore.getIntervalLength());

        editChoreLauncher.launch(intent);
    }

    private void editChoreCallback(ActivityResult activityResult)
    {
        if (activityResult.getResultCode() != RESULT_OK)
        {
            return;
        }

        Chore chore = choreUnderEdit;

        Intent intent = Objects.requireNonNull(activityResult.getData());

        switch (intent.getIntExtra("RESULT", -1))
        {
            case ChoreActivity.CHORE_RESULT_SAVE:
                chore.setNext((LocalDate)intent.getSerializableExtra("Next"));
                chore.setDescription(intent.getStringExtra("Description"));
                chore.setPriority(intent.getIntExtra("Priority", 1));
                chore.setEffort(intent.getIntExtra("Effort", 1));
                chore.setIntervalUnit(intent.getIntExtra("IntervalUnit", 1));
                chore.setIntervalLength(intent.getIntExtra("IntervalLength", 1));

                if (intent.getIntExtra("ACTION", -1) == ChoreActivity.CHORE_ACTION_ADD)
                {
                    choreList.addChore(chore);
                }

                break;

            case ChoreActivity.CHORE_RESULT_REMOVE:
                choreList.removeChore(chore);
                break;
        }

        choreList.save();

        choreListAdapter.notifyDataSetChanged();
    }

    private void editEffortCallback(ActivityResult activityResult)
    {
        if (activityResult.getResultCode() != RESULT_OK)
        {
            return;
        }

        WeeklyEffortTracker effortTracker = (WeeklyEffortTracker)choreList.getEffortTracker();

        Intent data = Objects.requireNonNull(activityResult.getData());

        effortTracker.setMonday(data.getIntExtra("Monday", 0));
        effortTracker.setTuesday(data.getIntExtra("Tuesday", 0));
        effortTracker.setWednesday(data.getIntExtra("Wednesday", 0));
        effortTracker.setThursday(data.getIntExtra("Thursday", 0));
        effortTracker.setFriday(data.getIntExtra("Friday", 0));
        effortTracker.setSaturday(data.getIntExtra("Saturday", 0));
        effortTracker.setSunday(data.getIntExtra("Sunday", 0));

        choreList.save();
    }

    private void showSortByDialog()
    {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_sort_chores_by);

        RadioButton descriptionRadioButton = dialog.findViewById(R.id.descriptionRadioButton);
        RadioButton priorityRadioButton = dialog.findViewById(R.id.priorityRadioButton);
        Button buttonOK = dialog.findViewById(R.id.buttonOK);

        descriptionRadioButton.setChecked(sortBy == SORT_BY_DESCRIPTION);
        priorityRadioButton.setChecked(sortBy == SORT_BY_PRIORITY);

        buttonOK.setOnClickListener(v ->
        {
            if (descriptionRadioButton.isChecked())
            {
                sortBy = SORT_BY_DESCRIPTION;
            }

            if (priorityRadioButton.isChecked())
            {
                sortBy = SORT_BY_PRIORITY;
            }

            choreListAdapter.notifyDataSetChanged();

            dialog.dismiss();
        });

        dialog.show();
    }
}
