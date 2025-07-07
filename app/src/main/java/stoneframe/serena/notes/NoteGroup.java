package stoneframe.serena.notes;

import java.util.UUID;

import stoneframe.serena.util.Revertible;

public class NoteGroup extends Revertible<NoteGroupData>
{
    public static final NoteGroup NONE = new NoteGroup();

    public NoteGroup(String name)
    {
        super(new NoteGroupData(UUID.randomUUID(), name));
    }

    private NoteGroup()
    {
        super(new NoteGroupData(new UUID(0L, 0L), "None"));
    }

    public UUID getId()
    {
        return data().id;
    }

    public String getName()
    {
        return data().name;
    }

    void setName(String name)
    {
        this.data().name = name;
    }
}
