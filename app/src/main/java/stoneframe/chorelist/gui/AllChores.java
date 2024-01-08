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

import java.util.Objects;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Chore;

public class AllChores extends Fragment
{
    private static final int ACTIVITY_ADD_CHORE = 0;
    private static final int ACTIVITY_EDIT_CHORE = 1;

    private ChoreList choreList;

    private ArrayAdapter<Chore> choreAdapter;

    private Chore choreUnderEdit;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = (GlobalState)Objects.requireNonNull(getActivity()).getApplication();
        choreList = globalState.getChoreList();

        View view = inflater.inflate(R.layout.fragment_all_chores, container, false);

        choreAdapter = new ArrayAdapter<>(
            getActivity().getBaseContext(),
            android.R.layout.simple_list_item_1);
        ListView choreListView = view.findViewById(R.id.all_chores);
        choreListView.setAdapter(choreAdapter);
        choreListView.setOnItemClickListener((parent, view1, position, id) ->
        {
            Chore chore = choreAdapter.getItem(position);
            assert chore != null;
            startChoreEditor(chore, ACTIVITY_EDIT_CHORE);
        });
        choreListView.setOnItemLongClickListener((parent, view12, position, id) ->
        {
            Chore chore = choreAdapter.getItem(position);
            choreAdapter.remove(chore);
            choreList.removeChore(chore);
            return true;
        });

        Button addButton = view.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            Chore duty = new Chore(
                "",
                1,
                1,
                DateTime.now().withTimeAtStartOfDay(),
                1, Chore.DAYS
            );
            startChoreEditor(duty, ACTIVITY_ADD_CHORE);
        });

        return view;
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
        intent.putExtra("Next", chore.getNext());
        intent.putExtra("Description", chore.getDescription());
        intent.putExtra("Priority", chore.getPriority());
        intent.putExtra("Effort", chore.getEffort());
        intent.putExtra("IntervalUnit", chore.getIntervalUnit());
        intent.putExtra("IntervalLength", chore.getIntervalLength());

        startActivityForResult(intent, mode);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK)
        {
            Chore duty = choreUnderEdit;

            duty.setNext((DateTime)data.getSerializableExtra("Next"));
            duty.setDescription(data.getStringExtra("Description"));
            duty.setPriority(data.getIntExtra("Priority", 1));
            duty.setEffort(data.getIntExtra("Effort", 1));
            duty.setIntervalUnit(data.getIntExtra("IntervalUnit", 1));
            duty.setIntervalLength(data.getIntExtra("IntervalLength", 1));

            if (requestCode == ACTIVITY_ADD_CHORE)
            {
                choreList.addChore(duty);
            }
        }
    }
}
