package stoneframe.chorelist.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.Chore;

public class TodaysChores extends Fragment
{
    private Schedule schedule;

    private ArrayAdapter<Chore> choreAdapter;
    private ListView choreList;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = (GlobalState)getActivity().getApplication();
        schedule = globalState.getSchedule();

        View view = inflater.inflate(R.layout.fragment_todays_chores, container, false);

        choreAdapter = new ArrayAdapter<>(
            getActivity().getBaseContext(),
            android.R.layout.simple_list_item_1);
        choreList = (ListView)view.findViewById(R.id.todays_chores);
        choreList.setAdapter(choreAdapter);
        choreList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                Chore chore = choreAdapter.getItem(position);
                schedule.complete(chore);
                choreAdapter.remove(chore);
            }
        });
        choreList.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener()
        {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
            {
                Chore chore = schedule.getChores().get(position);
                schedule.skip(chore);
                choreAdapter.clear();
                choreAdapter.addAll(schedule.getChores());
                return true;
            }
        });

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        choreAdapter.addAll(schedule.getChores());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        choreAdapter.clear();
    }
}