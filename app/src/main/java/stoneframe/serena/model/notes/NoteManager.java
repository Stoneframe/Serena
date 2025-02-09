package stoneframe.serena.model.notes;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import stoneframe.serena.model.timeservices.TimeService;

public class NoteManager
{
    private final Supplier<NoteContainer> container;

    private final TimeService timeService;

    public NoteManager(Supplier<NoteContainer> container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public Note createNote(String title)
    {
        return new Note(title);
    }

    public NoteEditor getNoteEditor(Note note)
    {
        return new NoteEditor(timeService, this, note);
    }

    public List<Note> getAllNotes()
    {
        return Collections.unmodifiableList(container.get().notes);
    }

    void addNote(Note note)
    {
        container.get().notes.add(note);
    }

    void removeNote(Note note)
    {
        container.get().notes.remove(note);
    }

    boolean containsNote(Note note)
    {
        return container.get().notes.contains(note);
    }
}
