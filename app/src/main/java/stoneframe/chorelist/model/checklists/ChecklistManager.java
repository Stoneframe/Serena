package stoneframe.chorelist.model.checklists;

import java.util.Collections;
import java.util.List;

import stoneframe.chorelist.model.timeservices.TimeService;

public class ChecklistManager
{
    private final ChecklistContainer container;

    private final TimeService timeService;

    public ChecklistManager(ChecklistContainer container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public List<Checklist> getChecklists()
    {
        return Collections.unmodifiableList(container.checklists);
    }

    public ChecklistEditor getChecklistEditor(Checklist checklist)
    {
        return new ChecklistEditor(this, checklist, timeService);
    }

    public void createChecklist(String name)
    {
        container.checklists.add(new Checklist(name));
    }

    void removeChecklist(Checklist checklist)
    {
        container.checklists.remove(checklist);
    }
}
