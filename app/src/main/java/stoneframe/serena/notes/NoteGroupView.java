package stoneframe.serena.notes;

import androidx.annotation.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public final class NoteGroupView
{
    private final NoteContainer container;

    private final NoteGroup noteGroup;

    public NoteGroupView(NoteContainer container, NoteGroup noteGroup)
    {
        this.container = container;
        this.noteGroup = noteGroup;
    }

    public String getName()
    {
        return noteGroup.getName();
    }

    public List<NoteView> getNotes()
    {
        return container.notes.stream()
            .filter(n -> n.getGroupId().equals(noteGroup.getId()))
            .map(n -> new NoteView(n, noteGroup))
            .sorted(Comparator.comparing(NoteView::getTitle))
            .collect(Collectors.toList());
    }

    NoteGroup getNoteGroup()
    {
        return noteGroup;
    }

    @Override
    public int hashCode()
    {
        return noteGroup.getId().hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj)
    {
        if (obj instanceof NoteGroupView)
        {
            NoteGroupView other = (NoteGroupView)obj;

            return this.noteGroup.getId().equals(other.noteGroup.getId());
        }

        return false;
    }
}
