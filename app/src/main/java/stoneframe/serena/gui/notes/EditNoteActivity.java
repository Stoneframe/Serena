package stoneframe.serena.gui.notes;

import android.widget.EditText;

import stoneframe.serena.R;
import stoneframe.serena.gui.EditActivity;
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
                new EditTextCriteria(titleEditText, EditTextCriteria.IS_NOT_EMPTY)
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
    protected void onSave(int action)
    {
        noteEditor.setTitle(titleEditText.getText().toString().trim());
        noteEditor.setText(textEditText.getText().toString());
        noteEditor.save();

        serena.save();
    }

    @Override
    protected void onCancel()
    {
        noteEditor.revert();
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
}
