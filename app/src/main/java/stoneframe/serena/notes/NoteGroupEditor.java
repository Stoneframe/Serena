package stoneframe.serena.notes;

import stoneframe.serena.Editor;
import stoneframe.serena.timeservices.TimeService;

public class NoteGroupEditor extends Editor<NoteGroupEditor.NoteGroupEditorListener>
{
    private final NoteManager noteManager;

    private final NoteGroup noteGroup;

    private final PropertyUtil<String> nameProperty;

    public NoteGroupEditor(TimeService timeService, NoteManager noteManager, NoteGroup noteGroup)
    {
        super(timeService);

        this.noteManager = noteManager;
        this.noteGroup = noteGroup;
        this.noteGroup.edit();

        nameProperty = getNameProperty();
    }

    public String getName()
    {
        return nameProperty.getValue();
    }

    public void setName(String name)
    {
        nameProperty.setValue(name);
    }

    public void save()
    {
        noteGroup.save();

        if (!noteManager.containsNoteGroup(noteGroup))
        {
            noteManager.addNoteGroup(noteGroup);
        }
    }

    public void revert()
    {
        noteGroup.revert();
    }

    public void remove()
    {
        if (noteManager.containsNoteGroup(noteGroup))
        {
            noteManager.removeNoteGroup(noteGroup);
        }
    }

    private PropertyUtil<String> getNameProperty()
    {
        return new PropertyUtil<>(
            noteGroup::getName,
            noteGroup::setName,
            v -> notifyListeners(NoteGroupEditorListener::nameChanged));
    }

    public interface NoteGroupEditorListener
    {
        void nameChanged();
    }
}
