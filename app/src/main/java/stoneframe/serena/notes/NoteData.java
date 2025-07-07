package stoneframe.serena.notes;

import java.util.UUID;

class NoteData
{
    String title;
    String text;

    UUID groupId;

    public NoteData(String title, String text)
    {
        this.title = title;
        this.text = text;
        this.groupId = new UUID(0L, 0L);
    }
}
