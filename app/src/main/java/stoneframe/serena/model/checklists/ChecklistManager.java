package stoneframe.serena.model.checklists;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

import stoneframe.serena.model.timeservices.TimeService;

public class ChecklistManager
{
    private final Supplier<ChecklistContainer> container;

    private final TimeService timeService;

    public ChecklistManager(Supplier<ChecklistContainer> container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public List<Checklist> getChecklists()
    {
        return Collections.unmodifiableList(getContainer().checklists);
    }

    public ChecklistEditor getChecklistEditor(Checklist checklist)
    {
        return new ChecklistEditor(this, checklist, timeService);
    }

    public void createChecklist(String name)
    {
        getContainer().checklists.add(new Checklist(name));
    }

    void removeChecklist(Checklist checklist)
    {
        getContainer().checklists.remove(checklist);
    }

    private ChecklistContainer getContainer()
    {
        return container.get();
    }
}
