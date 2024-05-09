package stoneframe.chorelist.gui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import org.joda.time.DateTime;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Chore;

public class AllChoresFragment extends Fragment
{
    private ChoreList choreList;

    private ArrayAdapter<Chore> choreAdapter;

    private Chore choreUnderEdit;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = GlobalState.getInstance(this);

        choreList = globalState.getChoreList();

        View rootView = inflater.inflate(R.layout.fragment_all_chores, container, false);

        choreAdapter = new ArrayAdapter<>(
            requireContext(),
            android.R.layout.simple_list_item_1);
        ListView choreListView = rootView.findViewById(R.id.all_tasks);
        choreListView.setAdapter(choreAdapter);
        choreListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Chore chore = choreAdapter.getItem(position);
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
                DateTime.now().withTimeAtStartOfDay(),
                1, Chore.DAYS
            );
            startChoreEditor(chore, ChoreActivity.CHORE_ACTION_ADD);
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        choreAdapter.addAll(choreList.getAllChores());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        choreAdapter.clear();
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

        startActivityForResult(intent, mode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_OK)
        {
            Chore chore = choreUnderEdit;

            switch (intent.getIntExtra("RESULT", -1))
            {
                case ChoreActivity.CHORE_RESULT_SAVE:
                    chore.setNext((DateTime)intent.getSerializableExtra("Next"));
                    chore.setDescription(intent.getStringExtra("Description"));
                    chore.setPriority(intent.getIntExtra("Priority", 1));
                    chore.setEffort(intent.getIntExtra("Effort", 1));
                    chore.setIntervalUnit(intent.getIntExtra("IntervalUnit", 1));
                    chore.setIntervalLength(intent.getIntExtra("IntervalLength", 1));

                    if (requestCode == ChoreActivity.CHORE_ACTION_ADD)
                    {
                        choreList.addChore(chore);
                    }

                    break;

                case ChoreActivity.CHORE_RESULT_REMOVE:
                    choreAdapter.remove(chore);
                    choreList.removeChore(chore);
                    break;
            }

            choreList.save();
        }
    }
}
