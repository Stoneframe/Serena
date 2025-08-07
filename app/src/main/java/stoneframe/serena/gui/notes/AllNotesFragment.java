package stoneframe.serena.gui.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ImageButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.util.LinkedList;
import java.util.List;

import stoneframe.serena.R;
import stoneframe.serena.Serena;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.notes.NoteGroupView;
import stoneframe.serena.notes.NoteManager;
import stoneframe.serena.notes.NoteView;

public class AllNotesFragment extends Fragment
{
    public static final int ACTION_ADD = 0;
    public static final int ACTION_EDIT = 1;

    private static List<String> closedNoteGroups = new LinkedList<>();

    private ActivityResultLauncher<Intent> allNoteGroupsLauncher;
    private ActivityResultLauncher<Intent> editNoteLauncher;

    private NoteGroupExpandableListAdapter notesGroupExpandableListAdapter;
    private ExpandableListView notesGroupExpandableListView;

    private Button groupsButton;
    private Button addButton;

    private ImageButton collapseGroupsButton;
    private ImageButton expandGroupsButton;

    private GlobalState globalState;
    private Serena serena;

    private NoteManager noteManager;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();
        serena = globalState.getSerena();
        noteManager = serena.getNoteManager();

        View rootView = inflater.inflate(R.layout.fragment_all_notes, container, false);

        notesGroupExpandableListAdapter = new NoteGroupExpandableListAdapter(
            requireContext(),
            noteManager);

        notesGroupExpandableListView = rootView.findViewById(R.id.note_groups_list);
        notesGroupExpandableListView.setAdapter(notesGroupExpandableListAdapter);

        notesGroupExpandableListView.setOnChildClickListener((parent, view, groupPosition, childPosition, id) ->
        {
            NoteView note = (NoteView)notesGroupExpandableListAdapter.getChild(
                groupPosition,
                childPosition);
            assert note != null;
            startNoteEditor(note, ACTION_EDIT);
            return true;
        });

        expandAllGroups();

        groupsButton = rootView.findViewById(R.id.groups_button);
        groupsButton.setOnClickListener(v -> startNoteGroupEditor());

        addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            NoteView note = noteManager.createNote("");

            startNoteEditor(note, ACTION_ADD);
        });

        expandGroupsButton = rootView.findViewById(R.id.expand_groups_button);
        expandGroupsButton.setOnClickListener(v -> expandAllGroups());

        collapseGroupsButton = rootView.findViewById(R.id.collapse_groups_button);
        collapseGroupsButton.setOnClickListener(v -> collapseAllGroups());

        allNoteGroupsLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editNoteGroupCallback);

        editNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editNoteCallback);

        return rootView;
    }

    private void startNoteEditor(NoteView note, int action)
    {
        globalState.setActiveNote(note);

        Intent intent = new Intent(getActivity(), EditNoteActivity.class);
        intent.putExtra("ACTION", action);

        editNoteLauncher.launch(intent);
    }

    private void startNoteGroupEditor()
    {
        Intent intent = new Intent(getActivity(), AllNoteGroupsActivity.class);

        allNoteGroupsLauncher.launch(intent);
    }

    private void editNoteGroupCallback(ActivityResult activityResult)
    {
        notesGroupExpandableListAdapter.notifyDataSetChanged();

        NoteGroupView noteGroup = GlobalState.getInstance().getActiveNoteGroup();

        expandGroup(noteGroup);
    }

    private void editNoteCallback(ActivityResult activityResult)
    {
        notesGroupExpandableListAdapter.notifyDataSetChanged();

        NoteGroupView noteGroup = GlobalState.getInstance().getActiveNote().getGroup();

        expandGroup(noteGroup);
    }

    private void expandAllGroups()
    {
        for (int i = 0; i < notesGroupExpandableListAdapter.getGroupCount(); i++)
        {
            notesGroupExpandableListView.expandGroup(i);
        }
    }

    private void expandGroup(NoteGroupView noteGroupToExpand)
    {
        for (int i = 0; i < notesGroupExpandableListAdapter.getGroupCount(); i++)
        {
            NoteGroupView group = (NoteGroupView)notesGroupExpandableListAdapter.getGroup(i);

            if (group.equals(noteGroupToExpand))
            {
                notesGroupExpandableListView.expandGroup(i);
                break;
            }
        }
    }

    private void collapseAllGroups()
    {
        for (int i = 0; i < notesGroupExpandableListAdapter.getGroupCount(); i++)
        {
            notesGroupExpandableListView.collapseGroup(i);
        }
    }
}