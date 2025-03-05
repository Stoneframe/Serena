package stoneframe.serena.gui.notes;

import android.widget.EditText;
import android.widget.ImageButton;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import stoneframe.serena.R;
import stoneframe.serena.gui.EditActivity;
import stoneframe.serena.gui.util.DialogUtils;
import stoneframe.serena.gui.util.EditTextCriteria;
import stoneframe.serena.gui.util.EnableCriteria;
import stoneframe.serena.model.notes.Note;
import stoneframe.serena.model.notes.NoteEditor;

public class EditNoteActivity extends EditActivity implements NoteEditor.NoteEditorListener
{
    private EditText titleEditText;
    private EditText textEditText;

    private ImageButton dateButton;
    private ImageButton timeButton;
    private ImageButton crossButton;
    private ImageButton checkmarkButton;
    private ImageButton bulletButton;
    private ImageButton tabButton;

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
        Note note = globalState.getActiveNote();

        noteEditor = serena.getNoteManager().getNoteEditor(note);

        titleEditText = findViewById(R.id.titleEditText);
        textEditText = findViewById(R.id.textEditText);
        dateButton = findViewById(R.id.dateButton);
        timeButton = findViewById(R.id.timeButton);
        crossButton = findViewById(R.id.crossButton);
        checkmarkButton = findViewById(R.id.checkmarkButton);
        tabButton = findViewById(R.id.tabButton);
        bulletButton = findViewById(R.id.bulletButton);

        titleEditText.setText(noteEditor.getTitle());
        textEditText.setText(noteEditor.getText());

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
                new EditTextCriteria(textEditText, e -> hasTextChanges()),
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
        noteEditor.save();

        serena.save();

        saveButton.setEnabled(false);

        return false;
    }

    @Override
    protected boolean onCancel()
    {
        if (!hasTextChanges())
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

    private boolean hasTextChanges()
    {
        return !noteEditor.getText().equals(textEditText.getText().toString());
    }

    private void addCharacter(String character)
    {
        int start = textEditText.getSelectionStart();
        int end = textEditText.getSelectionEnd();
        textEditText.getText().replace(Math.min(start, end), Math.max(start, end), character);
    }
}
