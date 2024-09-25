package stoneframe.serena.model;

import org.joda.time.LocalDateTime;

import java.util.List;

import stoneframe.serena.model.checklists.Checklist;
import stoneframe.serena.model.checklists.ChecklistManager;
import stoneframe.serena.model.chores.Chore;
import stoneframe.serena.model.chores.ChoreManager;
import stoneframe.serena.model.chores.ChoreSelector;
import stoneframe.serena.model.chores.EffortTracker;
import stoneframe.serena.model.limiters.Limiter;
import stoneframe.serena.model.limiters.LimiterEditor;
import stoneframe.serena.model.limiters.LimiterManager;
import stoneframe.serena.model.routines.PendingProcedure;
import stoneframe.serena.model.routines.Routine;
import stoneframe.serena.model.routines.RoutineManager;
import stoneframe.serena.model.tasks.Task;
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
            container.RoutineManager = new RoutineManager();
            container.ChoreManager = new ChoreManager(effortTracker, choreSelector);
            container.TaskManager = new TaskManager();
            container.ChecklistManager = new ChecklistManager();
            container.LimiterManager = new LimiterManager();
        }

        container.TaskManager.clean(timeService.getToday());
    }

    public void save()
    {
        storage.save(container);
    }

    public List<Chore> getAllChores()
    {
        return container.ChoreManager.getAllChores();
    }

    public void addChore(Chore chore)
    {
        container.ChoreManager.addChore(chore);
    }

    public void removeChore(Chore chore)
    {
        container.ChoreManager.removeChore(chore);
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

    public List<Task> getAllTasks()
    {
        return container.TaskManager.getAllTasks(true);
    }

    public List<Task> getAllTasks(boolean includeCompleted)
    {
        return container.TaskManager.getAllTasks(includeCompleted);
    }

    public void addTask(Task task)
    {
        container.TaskManager.addTask(task);
    }

    public void removeTask(Task task)
    {
        container.TaskManager.removeTask(task);
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

    public void taskPostpone(Task task)
    {
        container.TaskManager.postpone(task, timeService.getToday());
    }

    public List<Routine> getAllRoutines()
    {
        return container.RoutineManager.getAllRoutines();
    }

    public void addRoutine(Routine routine)
    {
        container.RoutineManager.addRoutine(routine);
    }

    public void removeRoutine(Routine routine)
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

    public void resetRoutine(Routine routine)
    {
        container.RoutineManager.resetRoutine(routine, timeService.getNow());
    }

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

    public List<Limiter> getLimiters()
    {
        return container.LimiterManager.getLimiters();
    }

    public Limiter createLimiter(String name)
    {
        return container.LimiterManager.createLimiter(name, timeService.getToday());
    }

    public LimiterEditor getLimiterEditor(Limiter limiter)
    {
        return container.LimiterManager.getEditor(limiter, timeService);
    }
}
