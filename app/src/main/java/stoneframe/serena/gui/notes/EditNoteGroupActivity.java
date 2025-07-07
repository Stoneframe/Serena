package stoneframe.serena.gui.notes;

import android.widget.EditText;

import stoneframe.serena.R;
import stoneframe.serena.gui.EditActivity;
import stoneframe.serena.gui.util.enable.EditTextCriteria;
import stoneframe.serena.gui.util.enable.EnableCriteria;
import stoneframe.serena.notes.NoteGroupEditor;
import stoneframe.serena.notes.NoteGroupView;

public class EditNoteGroupActivity extends EditActivity
{
    private EditText nameEditText;

    private NoteGroupEditor noteGroupEditor;

    @Override
    protected int getActivityLayoutId()
    {
        return R.layout.activity_note_group;
    }

    @Override
    protected String getActivityTitle()
    {
        return "Note Group";
    }

    @Override
    protected String getEditedObjectName()
    {
        return "Note Group";
    }

    @Override
    protected void createActivity()
    {
        NoteGroupView noteGroup = globalState.getActiveNoteGroup();

        noteGroupEditor = serena.getNoteManager().getNoteGroupEditor(noteGroup);

        nameEditText = findViewById(R.id.nameEditText);

        nameEditText.setText(noteGroupEditor.getName());
    }

    @Override
    protected EnableCriteria[] getSaveEnabledCriteria()
    {
        return new EnableCriteria[]
            {
                new EditTextCriteria(nameEditText, EditTextCriteria.IS_NOT_EMPTY)
            };
    }

    @Override
    protected boolean onSave(int action)
    {
        noteGroupEditor.setName(nameEditText.getText().toString().trim());
        noteGroupEditor.save();

        serena.save();

        return true;
    }

    @Override
    protected boolean onCancel()
    {
        return true;
    }

    @Override
    protected void onRemove()
    {
        noteGroupEditor.remove();
        serena.save();
    }
}