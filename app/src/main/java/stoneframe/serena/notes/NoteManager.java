package stoneframe.serena.notes;

import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import stoneframe.serena.timeservices.TimeService;

public class NoteManager
{
    private final Supplier<NoteContainer> container;

    private final TimeService timeService;

    public NoteManager(Supplier<NoteContainer> container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public NoteView createNote(String title)
    {
        return new NoteView(new Note(title), NoteGroup.NONE, container.get());
    }

    public NoteGroupView createNoteGroup(String name)
    {
        return new NoteGroupView(container.get(), new NoteGroup(name));
    }

    public NoteEditor getNoteEditor(NoteView note)
    {
        return new NoteEditor(timeService, this, note.getNote());
    }

    public NoteGroupEditor getNoteGroupEditor(NoteGroupView noteGroup)
    {
        return new NoteGroupEditor(timeService, this, noteGroup.getNoteGroup());
    }

    public List<NoteView> getAllNotes()
    {
        return container.get().notes.stream()
            .sorted(Comparator.comparing(Note::getTitle))
            .map(n -> new NoteView(n, NoteGroup.NONE, container.get()))
            .collect(Collectors.toList());
    }

    public List<NoteGroupView> getAllGroups()
    {
        return getAllGroups(true);
    }

    public List<NoteGroupView> getAllGroups(boolean includeNone)
    {
        List<NoteGroupView> groups = container.get().groups.stream()
            .map(g -> new NoteGroupView(container.get(), g))
            .sorted(Comparator.comparing(NoteGroupView::getName))
            .collect(Collectors.toList());

        if (includeNone)
        {
            groups.add(0, new NoteGroupView(container.get(), NoteGroup.NONE));
        }

        return groups;
    }

    public NoteGroupView getGroup(UUID groupId)
    {
        NoteGroup noteGroup = container.get().groups
            .stream()
            .filter(g -> g.getId().equals(groupId))
            .findFirst()
            .orElse(NoteGroup.NONE);

        return new NoteGroupView(container.get(), noteGroup);
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

    void addNoteGroup(NoteGroup group)
    {
        container.get().groups.add(group);
    }

    void removeNoteGroup(NoteGroup group)
    {
        for (Note note : container.get().notes)
        {
            if (note.getGroupId().equals(group.getId()))
            {
                note.setGroupId(NoteGroup.NONE.getId());
            }
        }

        container.get().groups.remove(group);
    }

    boolean containsNoteGroup(NoteGroup group)
    {
        return container.get().groups.contains(group);
    }
}
