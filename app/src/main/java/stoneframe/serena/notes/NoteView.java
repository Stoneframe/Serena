package stoneframe.serena.notes;

public class NoteView
{
    private final Note note;
    private final NoteGroup group;

    private final NoteContainer container;

    public NoteView(Note note, NoteGroup group, NoteContainer container)
    {
        this.note = note;
        this.group = group;
        this.container = container;
    }

    public String getTitle()
    {
        return note.getTitle();
    }

    public String getText()
    {
        return note.getText();
    }

    public NoteGroupView getGroup()
    {
        return new NoteGroupView(container, group);
    }

    Note getNote()
    {
        return note;
    }
}
