package stoneframe.chorelist.model;

import org.joda.time.LocalDateTime;

import java.util.List;

import stoneframe.chorelist.model.checklists.ChecklistContainer;
import stoneframe.chorelist.model.checklists.ChecklistManager;
import stoneframe.chorelist.model.chores.ChoreContainer;
import stoneframe.chorelist.model.chores.ChoreManager;
import stoneframe.chorelist.model.chores.ChoreSelector;
import stoneframe.chorelist.model.chores.EffortTracker;
import stoneframe.chorelist.model.limiters.LimiterContainer;
import stoneframe.chorelist.model.limiters.LimiterManager;
import stoneframe.chorelist.model.routines.DayRoutine;
import stoneframe.chorelist.model.routines.DayRoutineEditor;
import stoneframe.chorelist.model.routines.FortnightRoutine;
import stoneframe.chorelist.model.routines.FortnightRoutineEditor;
import stoneframe.chorelist.model.routines.PendingProcedure;
import stoneframe.chorelist.model.routines.Routine;
import stoneframe.chorelist.model.routines.RoutineContainer;
import stoneframe.chorelist.model.routines.RoutineManager;
import stoneframe.chorelist.model.routines.WeekRoutine;
import stoneframe.chorelist.model.routines.WeekRoutineEditor;
import stoneframe.chorelist.model.tasks.Task;
import stoneframe.chorelist.model.tasks.TaskEditor;
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
            container.RoutineManager = new RoutineContainer();
            container.ChoreManager = new ChoreContainer(effortTracker, choreSelector);
            container.TaskManager = new TaskManager();
            container.ChecklistManager = new ChecklistContainer();
            container.LimiterManager = new LimiterContainer();
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
        return new ChoreManager(container.ChoreManager, timeService);
    }

    // ====================================================================
    // TASKS
    // ====================================================================

    public List<Task> getAllTasks()
    {
        return container.TaskManager.getAllTasks(true, timeService.getToday());
    }

    public TaskEditor getTaskEditor(Task task)
    {
        return container.TaskManager.getEditor(task, timeService);
    }

    public Task createTask()
    {
        return container.TaskManager.createTask(timeService);
    }

    public List<Task> getTodaysTasks()
    {
        return container.TaskManager.getTodaysTasks(timeService.getToday());
    }

    public void taskDone(Task task)
    {
        container.TaskManager.complete(task, timeService.getToday());
    }

    public void taskUndone(Task task)
    {
        container.TaskManager.undo(task);
    }

    // ====================================================================
    // ROUTINES
    // ====================================================================

    public RoutineManager getRoutineManager()
    {
        return new RoutineManager(container.RoutineManager, timeService);
    }

    // ====================================================================
    // CHECKLISTS
    // ====================================================================

    public ChecklistManager getChecklistManager()
    {
        return new ChecklistManager(container.ChecklistManager, timeService);
    }

    // ====================================================================
    // LIMITERS
    // ====================================================================

    public LimiterManager getLimiterManager()
    {
        return new LimiterManager(container.LimiterManager, timeService);
    }
}
