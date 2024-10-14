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
        return data().name;
    }

    public List<ChecklistItem> getItems()
    {
        return data().items;
    }

    void setName(String name)
    {
        data().name = name;
    }

    void addItem(ChecklistItem item)
    {
        data().items.add(item);
    }

    void removeItem(ChecklistItem item)
    {
        data().items.remove(item);
    }

    void moveItem(ChecklistItem item, Integer newPosition)
    {
        data().items.remove(item);
        data().items.add(newPosition, item);
    }
}
