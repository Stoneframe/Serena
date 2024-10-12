package stoneframe.chorelist.model;

import org.joda.time.LocalDateTime;

import java.util.List;

import stoneframe.chorelist.model.checklists.Checklist;
import stoneframe.chorelist.model.checklists.ChecklistManager;
import stoneframe.chorelist.model.chores.Chore;
import stoneframe.chorelist.model.chores.ChoreEditor;
import stoneframe.chorelist.model.chores.ChoreManager;
import stoneframe.chorelist.model.chores.ChoreSelector;
import stoneframe.chorelist.model.chores.EffortTracker;
import stoneframe.chorelist.model.limiters.Limiter;
import stoneframe.chorelist.model.limiters.LimiterEditor;
import stoneframe.chorelist.model.limiters.LimiterManager;
import stoneframe.chorelist.model.routines.PendingProcedure;
import stoneframe.chorelist.model.routines.Routine;
import stoneframe.chorelist.model.routines.RoutineManager;
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
            container.RoutineManager = new RoutineManager();
            container.ChoreManager = new ChoreManager(effortTracker, choreSelector);
            container.TaskManager = new TaskManager();
            container.ChecklistManager = new ChecklistManager();
            container.LimiterManager = new LimiterManager();
        }
    }

    public void save()
    {
        storage.save(container);
    }

    // ====================================================================
    // CHORES
    // ====================================================================

    public List<Chore> getAllChores()
    {
        return container.ChoreManager.getAllChores();
    }

    public ChoreEditor getChoreEditor(Chore chore)
    {
        return container.ChoreManager.getChoreEditor(chore, timeService);
    }

    public Chore createChore()
    {
        return container.ChoreManager.createChore(timeService.getToday());
    }

    public List<Chore> getTodaysChores()
    {
        return container.ChoreManager.getChores(timeService.getToday());
    }

    public void choreDone(Chore chore)
    {
        container.ChoreManager.complete(chore, timeService.getToday());
    }

    public void choreSkip(Chore chore)
    {
        container.ChoreManager.skip(chore, timeService.getToday());
    }

    public void chorePostpone(Chore chore)
    {
        container.ChoreManager.postpone(chore, timeService.getToday());
    }

    public int getRemainingEffort()
    {
        return container.ChoreManager.getEffortTracker().getTodaysEffort(timeService.getToday());
    }

    public EffortTracker getEffortTracker()
    {
        return container.ChoreManager.getEffortTracker();
    }

    // ====================================================================
    // TASKS
    // ====================================================================

    public List<Task> getAllTasks()
    {
        return container.TaskManager.getAllTasks(true, timeService.getToday());
    }

    public List<Task> getAllTasks(boolean includeCompleted)
    {
        return container.TaskManager.getAllTasks(includeCompleted, timeService.getToday());
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

    public List<Routine<?>> getAllRoutines()
    {
        return container.RoutineManager.getAllRoutines();
    }

    public void addRoutine(Routine<?> routine)
    {
        container.RoutineManager.addRoutine(routine);
    }

    public void removeRoutine(Routine<?> routine)
    {
        container.RoutineManager.removeRoutine(routine);
    }

    public LocalDateTime getNextRoutineProcedureTime()
    {
        return container.RoutineManager.getNextProcedureTime(timeService.getNow());
    }

    public List<PendingProcedure> getPendingProcedures()
    {
        return container.RoutineManager.getPendingProcedures(timeService.getNow());
    }

    public List<PendingProcedure> getFirstPendingProcedures()
    {
        return container.RoutineManager.getFirstPendingProcedures(timeService.getNow());
    }

    public void procedureDone(PendingProcedure procedure)
    {
        container.RoutineManager.procedureDone(procedure);
    }

    public void resetRoutine(Routine<?> routine)
    {
        container.RoutineManager.resetRoutine(routine, timeService.getNow());
    }

    // ====================================================================
    // CHECKLISTS
    // ====================================================================

    public List<Checklist> getChecklists()
    {
        return container.ChecklistManager.getChecklists();
    }

    public void createChecklist(String name)
    {
        container.ChecklistManager.createChecklist(name);
    }

    public void removeChecklist(Checklist checklist)
    {
        container.ChecklistManager.removeChecklist(checklist);
    }

    // ====================================================================
    // LIMITERS
    // ====================================================================

    public List<Limiter> getLimiters()
    {
        return container.LimiterManager.getLimiters();
    }

    public LimiterEditor getLimiterEditor(Limiter limiter)
    {
        return container.LimiterManager.getEditor(limiter, timeService);
    }

    public Limiter createLimiter(String name)
    {
        return container.LimiterManager.createLimiter(name, timeService.getToday());
    }
}
