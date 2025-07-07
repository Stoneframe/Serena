package stoneframe.serena.notes;

import java.util.UUID;

public class NoteGroupData
{
    UUID id;

    String name;

    NoteGroupData(UUID id, String name)
    {
        this.id = id;
        this.name = name;
    }
}
