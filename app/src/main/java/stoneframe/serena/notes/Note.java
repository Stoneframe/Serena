package stoneframe.serena.notes;

import java.util.UUID;

import stoneframe.serena.util.Revertible;

public class Note extends Revertible<NoteData>
{
    Note(String title)
    {
        super(new NoteData(title, ""));
    }

    public String getTitle()
    {
        return data().title;
    }

    void setTitle(String title)
    {
        data().title = title;
    }

    public String getText()
    {
        return data().text;
    }

    void setText(String text)
    {
        data().text = text;
    }

    UUID getGroupId()
    {
        UUID groupId = data().groupId;

        if (groupId == null)
        {
            return NoteGroup.NONE.getId();
        }

        return groupId;
    }

    void setGroupId(UUID groupId)
    {
        data().groupId = groupId;
    }
}
