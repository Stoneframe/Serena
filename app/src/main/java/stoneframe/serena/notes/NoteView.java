package stoneframe.serena.notes;

public class NoteView
{
    private final Note note;
    private final NoteGroup group;

    public NoteView(Note note, NoteGroup group)
    {
        this.note = note;
        this.group = group;
    }

    public String getTitle()
    {
        return note.getTitle();
    }

    public String getText()
    {
        return note.getText();
    }

    public NoteGroup getGroup()
    {
        return group;
    }

    Note getNote()
    {
        return note;
    }
}
