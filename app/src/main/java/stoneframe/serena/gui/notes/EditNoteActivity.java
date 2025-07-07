package stoneframe.serena.gui.notes;

import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import stoneframe.serena.R;
import stoneframe.serena.gui.EditActivity;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.gui.util.enable.EnableCriteria;
import stoneframe.serena.gui.util.enable.OrEnableCriteria;
import stoneframe.serena.gui.util.enable.SpinnerCriteria;
import stoneframe.serena.notes.NoteEditor;
import stoneframe.serena.notes.NoteGroupView;
import stoneframe.serena.notes.NoteView;

public class EditNoteActivity extends EditActivity implements NoteEditor.NoteEditorListener
{
    private EditText titleEditText;
    private EditText textEditText;

    private Spinner groupSpinner;

    private ImageButton dateButton;
    private ImageButton timeButton;
    private ImageButton crossButton;
    private ImageButton checkmarkButton;
    private ImageButton bulletButton;
    private ImageButton tabButton;

    private SimpleListAdapter<NoteGroupView> groupsListAdapter;

    private NoteEditor noteEditor;

    @Override
    protected int getActivityLayoutId()
    {
        return R.layout.activity_note;
    }

    @Override
    protected String getActivityTitle()
    {
        return "Note";
    }

    @Override
    protected String getEditedObjectName()
    {
        return "Note";
    }

    @Override
    protected void createActivity()
    {
        NoteView note = globalState.getActiveNote();

        noteEditor = serena.getNoteManager().getNoteEditor(note);

        titleEditText = findViewById(R.id.titleEditText);
        textEditText = findViewById(R.id.textEditText);
        groupSpinner = findViewById(R.id.groupSpinner);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        crossButton = findViewById(R.id.crossButton);
        checkmarkButton = findViewById(R.id.checkmarkButton);
        tabButton = findViewById(R.id.tabButton);
        bulletButton = findViewById(R.id.bulletButton);

        titleEditText.setText(noteEditor.getTitle());
        textEditText.setText(noteEditor.getText());

        groupsListAdapter = new SimpleListAdapterBuilder<>(
            this,
            () -> serena.getNoteManager().getAllGroups(),
            NoteGroupView::getName)
            .create();

        int index = serena.getNoteManager().getAllGroups().indexOf(noteEditor.getGroup());

        groupSpinner.setAdapter(groupsListAdapter);
        groupSpinner.setSelection(index);

        dateButton.setOnClickListener(l -> addCharacter(LocalDate.now().toString("yyyy-MM-dd")));
        timeButton.setOnClickListener(l -> addCharacter(LocalTime.now().toString("HH:mm")));
        crossButton.setOnClickListener(l -> addCharacter("✖"));
        checkmarkButton.setOnClickListener(l -> addCharacter("✔"));
        bulletButton.setOnClickListener(l -> addCharacter("• "));
        tabButton.setOnClickListener(l -> addCharacter("\t"));
    }

    @Override
    protected EnableCriteria[] getSaveEnabledCriteria()
    {
        return new EnableCriteria[]
            {
                new EditTextCriteria(titleEditText, EditTextCriteria.IS_NOT_EMPTY),
                new OrEnableCriteria(
                    new EditTextCriteria(titleEditText, e -> hasTextChanges(
                        titleEditText,
                        noteEditor.getTitle())),
                    new EditTextCriteria(textEditText, e -> hasTextChanges(
                        textEditText,
                        noteEditor.getText())),
                    new SpinnerCriteria(
                        groupSpinner,
                        s -> !s.getSelectedItem().equals(noteEditor.getGroup()))),
            };
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        noteEditor.addListener(this);
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        noteEditor.removeListener(this);
    }

    @Override
    protected boolean onSave(int action)
    {
        noteEditor.setTitle(titleEditText.getText().toString().trim());
        noteEditor.setText(textEditText.getText().toString());
        noteEditor.setGroup((NoteGroupView)groupSpinner.getSelectedItem());
        noteEditor.save();

        serena.save();

        saveButton.setEnabled(false);

        return false;
    }

    @Override
    protected boolean onCancel()
    {
        if (!hasTextChanges(textEditText, noteEditor.getText()))
        {
            return true;
        }

        DialogUtils.showConfirmationDialog(
            this,
            "Unsaved changes",
            "You have unsaved changes to you note. If you close without saving, " +
                "the changes will be lost. Are you sure you want to close?",
            isConfirmed ->
            {
                if (!isConfirmed) return;

                noteEditor.revert();
                finish();
            });

        return false;
    }

    @Override
    protected void onRemove()
    {
        noteEditor.remove();

        serena.save();
    }

    @Override
    public void titleChanged()
    {

    }

    @Override
    public void textChanged()
    {

    }

    private boolean hasTextChanges(EditText editText, String originalText)
    {
        return !originalText.equals(editText.getText().toString());
    }

    private void addCharacter(String character)
    {
        int start = textEditText.getSelectionStart();
        int end = textEditText.getSelectionEnd();
        textEditText.getText().replace(Math.min(start, end), Math.max(start, end), character);
    }
}
