package stoneframe.serena.model.notes;

import stoneframe.serena.model.util.Revertible;

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
}
