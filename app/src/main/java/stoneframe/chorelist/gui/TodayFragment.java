package stoneframe.chorelist.gui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import java.util.Objects;

import stoneframe.chorelist.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.Chore;

public class TodayFragment extends Fragment
{
    private ChoreList choreList;

    private ArrayAdapter<Chore> choreAdapter;

    @Override
    public View onCreateView(
        LayoutInflater inflater, ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = (GlobalState)Objects.requireNonNull(getActivity())
            .getApplication();
        choreList = globalState.getChoreList();

        View view = inflater.inflate(R.layout.fragment_today, container, false);

        choreAdapter = new ArrayAdapter<>(
            getActivity().getBaseContext(),
            android.R.layout.simple_list_item_1);
        ListView choreListView = view.findViewById(R.id.today);
        choreListView.setAdapter(choreAdapter);
        choreListView.setOnItemClickListener((parent, view1, position, id) ->
        {
            Chore chore = choreAdapter.getItem(position);
            choreList.choreDone(chore);
            choreAdapter.remove(chore);
        });
        choreListView.setOnItemLongClickListener((parent, view12, position, id) ->
        {
            Chore chore = choreList.getTodaysChores().get(position);
            choreList.choreSkip(chore);
            choreAdapter.clear();
            choreAdapter.addAll(choreList.getTodaysChores());
            return true;
        });

        return view;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        choreAdapter.addAll(choreList.getTodaysChores());
    }

    @Override
    public void onStop()
    {
        super.onStop();

        choreAdapter.clear();
    }
}
