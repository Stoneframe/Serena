package stoneframe.serena.gui.checklists;

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

import java.util.Comparator;
import java.util.stream.Collectors;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.model.Serena;
import stoneframe.serena.model.checklists.Checklist;
import stoneframe.serena.model.checklists.ChecklistManager;

public class AllChecklistsFragment extends Fragment
{
    private SimpleListAdapter<Checklist> checklistAdapter;

    private GlobalState globalState;
    private Serena serena;
    private ChecklistManager checklistManager;

    @Nullable
    @Override
    public View onCreateView(
        @NonNull LayoutInflater inflater,
        @Nullable ViewGroup container,
        @Nullable Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();

        serena = globalState.getSerena();

        checklistManager = serena.getChecklistManager();

        View rootView = inflater.inflate(R.layout.fragment_all_checklists, container, false);

        ListView checklistListView = rootView.findViewById(R.id.listView);
        checklistAdapter = new SimpleListAdapter<>(
            requireContext(),
            () -> checklistManager.getChecklists()
                .stream()
                .sorted(Comparator.comparing(Checklist::getName))
                .collect(Collectors.toList()),
            Checklist::getName,
            null,
            null,
            null);
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

    @Override
    public void onStart()
    {
        super.onStart();

        checklistAdapter.notifyDataSetChanged();
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

            checklistManager.createChecklist(checklistName);
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
