package stoneframe.serena.notes;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class NoteGroup
{
    private final List<Note> notes = new LinkedList<>();

    private String name;

    public NoteGroup(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    void setName(String name)
    {
        this.name = name;
    }

    public List<Note> getNotes()
    {
        return Collections.unmodifiableList(notes);
    }

    void addNote(Note note)
    {
        notes.add(note);
    }

    void removeNote(Note note)
    {
        notes.remove(note);
    }
}
