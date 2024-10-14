package stoneframe.chorelist.model;

import stoneframe.chorelist.model.checklists.ChecklistContainer;
import stoneframe.chorelist.model.checklists.ChecklistManager;
import stoneframe.chorelist.model.chores.ChoreContainer;
import stoneframe.chorelist.model.chores.ChoreManager;
import stoneframe.chorelist.model.chores.ChoreSelector;
import stoneframe.chorelist.model.chores.EffortTracker;
import stoneframe.chorelist.model.limiters.LimiterContainer;
import stoneframe.chorelist.model.limiters.LimiterManager;
import stoneframe.chorelist.model.routines.RoutineContainer;
import stoneframe.chorelist.model.routines.RoutineManager;
import stoneframe.chorelist.model.tasks.TaskContainer;
import stoneframe.chorelist.model.tasks.TaskManager;
import stoneframe.chorelist.model.timeservices.TimeService;

public class ChoreList
{
    private final Storage storage;
    private final TimeService timeService;

    private final EffortTracker effortTracker;
    private final ChoreSelector choreSelector;

    private Container container;

    public ChoreList(
        Storage storage,
        TimeService timeService,
        EffortTracker effortTracker,
        ChoreSelector choreSelector)
    {
        this.storage = storage;
        this.timeService = timeService;
        this.effortTracker = effortTracker;
        this.choreSelector = choreSelector;
    }

    public void load()
    {
        container = storage.load();

        if (container == null)
        {
            container = new Container();

            container.Version = storage.getCurrentVersion();
            container.RoutineContainer = new RoutineContainer();
            container.ChoreContainer = new ChoreContainer(effortTracker, choreSelector);
            container.TaskContainer = new TaskContainer();
            container.ChecklistContainer = new ChecklistContainer();
            container.LimiterContainer = new LimiterContainer();
        }
    }

    public void save()
    {
        storage.save(container);
    }

    // ====================================================================
    // CHORES
    // ====================================================================

    public ChoreManager getChoreManager()
    {
        return new ChoreManager(() -> container.ChoreContainer, timeService);
    }

    // ====================================================================
    // TASKS
    // ====================================================================

    public TaskManager getTaskManager()
    {
        return new TaskManager(() -> container.TaskContainer, timeService);
    }

    // ====================================================================
    // ROUTINES
    // ====================================================================

    public RoutineManager getRoutineManager()
    {
        return new RoutineManager(() -> container.RoutineContainer, timeService);
    }

    // ====================================================================
    // CHECKLISTS
    // ====================================================================

    public ChecklistManager getChecklistManager()
    {
        return new ChecklistManager(() -> container.ChecklistContainer, timeService);
    }

    // ====================================================================
    // LIMITERS
    // ====================================================================

    public LimiterManager getLimiterManager()
    {
        return new LimiterManager(() -> container.LimiterContainer, timeService);
    }
}
