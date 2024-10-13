package stoneframe.chorelist.model.checklists;

import java.util.List;

import stoneframe.chorelist.model.util.Revertible;

public class Checklist extends Revertible<ChecklistData>
{
    Checklist(String name)
    {
        super(new ChecklistData(name));
    }

    public String getName()
    {
        return data().getName();
    }

    public List<ChecklistItem> getItems()
    {
        return data().getItems();
    }

    void setName(String name)
    {
        data().setName(name);
    }

    void addItem(ChecklistItem item)
    {
        data().addItem(item);
    }

    void removeItem(ChecklistItem item)
    {
        data().removeItem(item);
    }

    void moveItem(ChecklistItem item, Integer newPosition)
    {
        data().moveItem(item, newPosition);
    }
}
