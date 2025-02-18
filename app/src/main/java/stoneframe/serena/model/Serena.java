package stoneframe.serena.model;

import java.util.LinkedList;
import java.util.List;

import stoneframe.serena.model.balancers.BalancerContainer;
import stoneframe.serena.model.balancers.BalancerManager;
import stoneframe.serena.model.checklists.ChecklistContainer;
import stoneframe.serena.model.checklists.ChecklistManager;
import stoneframe.serena.model.chores.ChoreContainer;
import stoneframe.serena.model.chores.ChoreManager;
import stoneframe.serena.model.chores.ChoreSelector;
import stoneframe.serena.model.chores.EffortTracker;
import stoneframe.serena.model.notes.NoteContainer;
import stoneframe.serena.model.notes.NoteManager;
import stoneframe.serena.model.routines.RoutineContainer;
import stoneframe.serena.model.routines.RoutineManager;
import stoneframe.serena.model.tasks.TaskContainer;
import stoneframe.serena.model.tasks.TaskManager;
import stoneframe.serena.model.timeservices.TimeService;

public class Serena
{
    private final List<SerenaChangedListener> listeners = new LinkedList<>();

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
            container.BalancerContainer = new BalancerContainer();
            container.NoteContainer = new NoteContainer();
        }
    }

    public void save()
    {
        storage.save(container);
    }

    public void addChangedListener(SerenaChangedListener listener)
    {
        listeners.add(listener);
    }

    public void removeChangedListener(SerenaChangedListener listener)
    {
        listeners.remove(listener);
    }

    public void notifyChange()
    {
        listeners.forEach(SerenaChangedListener::onSerenaChanged);
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
    // BALANCERS
    // ====================================================================

    public BalancerManager getBalancerManager()
    {
        return new BalancerManager(() -> container.BalancerContainer, timeService);
    }

    // ====================================================================
    // NOTES
    // ====================================================================

    public NoteManager getNoteManager()
    {
        return new NoteManager(() -> container.NoteContainer, timeService);
    }
}
