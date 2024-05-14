package stoneframe.chorelist.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class ChecklistManager
{
    private final List<Checklist> checklists = new LinkedList<>();

    public List<Checklist> getChecklists()
    {
        return Collections.unmodifiableList(checklists);
    }

    public void createChecklist(String name)
    {
        checklists.add(new Checklist(name));
    }

    public void removeChecklist(Checklist checklist)
    {
        checklists.remove(checklist);
    }
}
