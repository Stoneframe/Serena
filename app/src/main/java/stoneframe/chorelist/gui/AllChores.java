package stoneframe.chorelist.gui;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import org.joda.time.DateTime;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.Chore;

public class AllChores extends Fragment
{
    private static final int ACTIVITY_ADD_CHORE = 0;
    private static final int ACTIVITY_EDIT_CHORE = 1;

    private Schedule schedule;

    private ArrayAdapter<Chore> choreAdapter;
    private ListView choreList;
    private Button addButton;

    private Chore choreUnderEdit;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = (GlobalState)getActivity().getApplication();
        schedule = globalState.getSchedule();

        View view = inflater.inflate(R.layout.fragment_all_chores, container, false);

        choreAdapter = new ArrayAdapter<Chore>(
            getActivity().getBaseContext(),
            android.R.layout.simple_list_item_1);
        choreList = (ListView)view.findViewById(R.id.all_chores);
        choreList.setAdapter(choreAdapter);
        choreList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Chore chore = choreAdapter.getItem(position);
                startChoreEditor(chore, ACTIVITY_EDIT_CHORE);
            }
        });
        choreList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                Chore chore = choreAdapter.getItem(position);
                choreAdapter.remove(chore);
                schedule.removeChore(chore);
                return true;
            }
        });

        addButton = (Button)view.findViewById(R.id.add_button);
        addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Chore duty = new Chore(
                    "",
                    1,
                    1,
                    DateTime.now().withTimeAtStartOfDay(),
                    Chore.DAILY,
                    1);
                startChoreEditor(duty, ACTIVITY_ADD_CHORE);
            }
        });

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        choreAdapter.addAll(schedule.getAllChores());
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
                schedule.addChore(duty);
            }
        }
    }
}
