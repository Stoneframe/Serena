package stoneframe.serena.gui.notes;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.Serena;
import stoneframe.serena.notes.Note;
import stoneframe.serena.notes.NoteManager;

public class AllNotesFragment extends Fragment
{
    public static final int ACTION_ADD = 0;
    public static final int ACTION_EDIT = 1;

    private ActivityResultLauncher<Intent> editNoteLauncher;

    private SimpleListAdapter<Note> notesListAdapter;

    private ListView notesListView;

    private Button addButton;

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

        notesListAdapter = new SimpleListAdapterBuilder<>(
            requireContext(),
            noteManager::getAllNotes,
            Note::getTitle).create();

        notesListView = rootView.findViewById(R.id.all_notes);
        notesListView.setAdapter(notesListAdapter);
        notesListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Note note = (Note)notesListAdapter.getItem(position);
            assert note != null;
            startNoteEditor(note, ACTION_EDIT);
        });

        addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            Note note = noteManager.createNote("");

            startNoteEditor(note, ACTION_ADD);
        });

        editNoteLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editNoteCallback);

        return rootView;
    }

    private void startNoteEditor(Note note, int action)
    {
        globalState.setActiveNote(note);

        Intent intent = new Intent(getActivity(), EditNoteActivity.class);
        intent.putExtra("ACTION", action);

        editNoteLauncher.launch(intent);
    }

    private void editNoteCallback(ActivityResult activityResult)
    {
        notesListAdapter.notifyDataSetChanged();
    }
}