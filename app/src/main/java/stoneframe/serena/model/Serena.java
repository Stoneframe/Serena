package stoneframe.serena.model;

import stoneframe.serena.model.checklists.ChecklistContainer;
import stoneframe.serena.model.checklists.ChecklistManager;
import stoneframe.serena.model.chores.ChoreContainer;
import stoneframe.serena.model.chores.ChoreManager;
import stoneframe.serena.model.chores.ChoreSelector;
import stoneframe.serena.model.chores.EffortTracker;
import stoneframe.serena.model.limiters.LimiterContainer;
import stoneframe.serena.model.limiters.LimiterManager;
import stoneframe.serena.model.routines.RoutineContainer;
import stoneframe.serena.model.routines.RoutineManager;
import stoneframe.serena.model.tasks.TaskContainer;
import stoneframe.serena.model.tasks.TaskManager;
import stoneframe.serena.model.timeservices.TimeService;

public class Serena
{
    private final Storage storage;
    private final TimeService timeService;

    private final EffortTracker effortTracker;
    private final ChoreSelector choreSelector;

    private Container container;

    public Serena(
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
