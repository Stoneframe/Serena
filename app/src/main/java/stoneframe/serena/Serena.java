package stoneframe.serena;

import java.util.LinkedList;
import java.util.List;

import stoneframe.serena.balancers.BalancerContainer;
import stoneframe.serena.balancers.BalancerManager;
import stoneframe.serena.checklists.ChecklistContainer;
import stoneframe.serena.checklists.ChecklistManager;
import stoneframe.serena.chores.ChoreContainer;
import stoneframe.serena.chores.ChoreManager;
import stoneframe.serena.chores.ChoreSelector;
import stoneframe.serena.chores.EffortTracker;
import stoneframe.serena.notes.NoteContainer;
import stoneframe.serena.notes.NoteManager;
import stoneframe.serena.routines.RoutineContainer;
import stoneframe.serena.routines.RoutineManager;
import stoneframe.serena.tasks.TaskContainer;
import stoneframe.serena.tasks.TaskManager;
import stoneframe.serena.timeservices.TimeService;

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
