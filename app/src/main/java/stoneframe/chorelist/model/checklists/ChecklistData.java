package stoneframe.chorelist.model.checklists;

import java.util.LinkedList;
import java.util.List;

class ChecklistData
{
    final List<ChecklistItem> items = new LinkedList<>();

    String name;

    ChecklistData(String name)
    {
        this.name = name;
    }
}
