package stoneframe.chorelist.model.checklists;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import stoneframe.chorelist.model.timeservices.TimeService;

public class ChecklistManager
{
    private final List<Checklist> checklists = new LinkedList<>();

    public List<Checklist> getChecklists()
    {
        return Collections.unmodifiableList(checklists);
    }

    public ChecklistEditor getChecklistEditor(Checklist checklist, TimeService timeService)
    {
        return new ChecklistEditor(this, checklist, timeService);
    }

    public void createChecklist(String name)
    {
        checklists.add(new Checklist(name));
    }

    void removeChecklist(Checklist checklist)
    {
        checklists.remove(checklist);
    }
}
