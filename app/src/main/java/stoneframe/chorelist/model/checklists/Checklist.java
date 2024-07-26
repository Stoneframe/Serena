package stoneframe.chorelist.model.checklists;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class Checklist
{
    private final List<ChecklistItem> items = new LinkedList<>();

    private String name;

    public Checklist(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public List<ChecklistItem> getItems()
    {
        return Collections.unmodifiableList(items);
    }

    public void addItem(ChecklistItem item)
    {
        items.add(item);
    }

    public void removeItem(ChecklistItem item)
    {
        items.remove(item);
    }

    public void moveItem(ChecklistItem item, Integer newPosition)
    {
        items.remove(item);
        items.add(newPosition, item);
    }
}
