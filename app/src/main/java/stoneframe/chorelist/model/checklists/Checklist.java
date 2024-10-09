package stoneframe.chorelist.model.checklists;

import java.util.List;

import stoneframe.chorelist.model.util.Revertible;

public class Checklist extends Revertible<ChecklistData>
{
    public Checklist(String name)
    {
        super(new ChecklistData(name));
    }

    public String getName()
    {
        return data().getName();
    }

    public void setName(String name)
    {
        data().setName(name);
    }

    public List<ChecklistItem> getItems()
    {
        return data().getItems();
    }

    public void addItem(ChecklistItem item)
    {
        data().addItem(item);
    }

    public void removeItem(ChecklistItem item)
    {
        data().removeItem(item);
    }

    public void moveItem(ChecklistItem item, Integer newPosition)
    {
        data().moveItem(item, newPosition);
    }
}
