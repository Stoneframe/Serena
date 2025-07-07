package stoneframe.serena.gui.notes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.notes.NoteGroupView;
import stoneframe.serena.notes.NoteManager;

public class AllNoteGroupsActivity extends AppCompatActivity
{
    private ActivityResultLauncher<Intent> editNoteGroupLauncher;

    private SimpleListAdapter<NoteGroupView> noteGroupListAdapter;
    private ListView noteGroupList;

    private Button addButton;
    private Button doneButton;

    private NoteManager noteManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        noteManager = GlobalState.getInstance().getSerena().getNoteManager();

        setContentView(R.layout.activity_note_groups);
        setTitle("Note Groups");

        editNoteGroupLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editNoteGroupCallback);


        noteGroupListAdapter = new SimpleListAdapterBuilder<>(
            this,
            () -> noteManager.getAllGroups(false),
            NoteGroupView::getName)
            .create();

        noteGroupList = findViewById(R.id.all_note_groups);
        noteGroupList.setAdapter(noteGroupListAdapter);
        noteGroupList.setOnItemClickListener((parent, view, position, id) ->
        {
            NoteGroupView noteGroup = (NoteGroupView)noteGroupListAdapter.getItem(position);
            assert noteGroup != null;
            startNoteGroupEditor(noteGroup, EditNoteGroupActivity.ACTION_EDIT);
        });

        addButton = findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            NoteGroupView noteGroup = noteManager.createNoteGroup("");
            startNoteGroupEditor(noteGroup, EditNoteGroupActivity.ACTION_ADD);
        });

        doneButton = findViewById(R.id.done_button);
        doneButton.setOnClickListener(v -> finish());
    }

    private void startNoteGroupEditor(NoteGroupView noteGroup, int action)
    {
        GlobalState.getInstance().setActiveNoteGroup(noteGroup);

        Intent intent = new Intent(this, EditNoteGroupActivity.class);
        intent.putExtra("ACTION", action);

        editNoteGroupLauncher.launch(intent);
    }

    private void editNoteGroupCallback(ActivityResult activityResult)
    {
        noteGroupListAdapter.notifyDataSetChanged();
    }
}
