package stoneframe.chorelist.gui;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.checklists.Checklist;

public class AllChecklistsFragment extends Fragment
{
    private SimpleListAdapter<Checklist> checklistAdapter;

    private GlobalState globalState;
    private ChoreList choreList;

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();

        View rootView = inflater.inflate(R.layout.fragment_all_checklists, container, false);

        ListView checklistListView = rootView.findViewById(R.id.listView);
        checklistAdapter = new SimpleListAdapter<>(
            requireContext(),
            choreList::getChecklists,
            Checklist::getName);
        checklistListView.setAdapter(checklistAdapter);
        checklistListView.setOnItemClickListener((parent, view, position, id) ->
        {
            globalState.setActiveChecklist((Checklist)checklistAdapter.getItem(position));
            openChecklistActivity();
        });

        Button btnAdd = rootView.findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> onAddButtonClicked());

        return rootView;
    }

    private void onAddButtonClicked()
    {
        final EditText checklistNameText = new EditText(getContext());

        checklistNameText.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create checklist");
        builder.setView(checklistNameText);

        builder.setPositiveButton("OK", (dialog, which) ->
        {
            String checklistName = checklistNameText.getText().toString();

            choreList.createChecklist(checklistName);
            checklistAdapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void openChecklistActivity()
    {
        Intent intent = new Intent(requireContext(), ChecklistActivity.class);
        startActivity(intent);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        checklistAdapter.notifyDataSetChanged();
    }
}
