package stoneframe.serena.gui.notes;

import android.widget.EditText;

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

        titleEditText.setText(noteEditor.getTitle());
        textEditText.setText(noteEditor.getText());
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
}
