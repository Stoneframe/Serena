package stoneframe.chorelist.gui;

import static android.app.Activity.RESULT_OK;

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

import java.util.Objects;

import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.chores.Chore;

public class AllChoresFragment extends Fragment
{
    private ActivityResultLauncher<Intent> editChoreLauncher;

    private ChoreList choreList;

    private SimpleListAdapter<Chore> choreListAdapter;

    private Chore choreUnderEdit;

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
            choreList::getAllChores,
            Chore::getDescription);
        ListView choreListView = rootView.findViewById(R.id.all_tasks);
        choreListView.setAdapter(choreListAdapter);
        choreListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Chore chore = (Chore)choreListAdapter.getItem(position);
            assert chore != null;
            startChoreEditor(chore, ChoreActivity.CHORE_ACTION_EDIT);
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

        return rootView;
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
        if (activityResult.getResultCode() == RESULT_OK)
        {
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
    }
}
