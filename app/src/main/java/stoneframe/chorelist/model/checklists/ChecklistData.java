package stoneframe.chorelist.model.checklists;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class ChecklistData
{
    private final List<ChecklistItem> items = new LinkedList<>();

    private String name;

    ChecklistData(String name)
    {
        this.name = name;
    }

    String getName()
    {
        return name;
    }

    List<ChecklistItem> getItems()
    {
        return Collections.unmodifiableList(items);
    }

    void setName(String name)
    {
        this.name = name;
    }

    void addItem(ChecklistItem item)
    {
        items.add(item);
    }

    void removeItem(ChecklistItem item)
    {
        items.remove(item);
    }

    void moveItem(ChecklistItem item, Integer newPosition)
    {
        items.remove(item);
        items.add(newPosition, item);
    }
}
