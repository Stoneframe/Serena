package stoneframe.chorelist;

import org.joda.time.DateTime;

import java.util.List;

import stoneframe.chorelist.model.Checklist;
import stoneframe.chorelist.model.ChecklistManager;
import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.ChoreManager;
import stoneframe.chorelist.model.ChoreSelector;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.EffortTracker;
import stoneframe.chorelist.model.PendingProcedure;
import stoneframe.chorelist.model.Routine;
import stoneframe.chorelist.model.RoutineManager;
import stoneframe.chorelist.model.Storage;
import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.TaskManager;
import stoneframe.chorelist.model.TimeService;
import stoneframe.chorelist.model.calories.CalorieConsumption;
import stoneframe.chorelist.model.calories.CaloriesManager;

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
            container.CaloriesManager = new CaloriesManager(
                timeService.getNow().toLocalDate(),
                250);
        }

        container.TaskManager.clean(timeService.getNow());
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
        return container.ChoreManager.getChores(timeService.getNow());
    }

    public void choreDone(Chore chore)
    {
        container.ChoreManager.complete(chore, timeService.getNow());
    }

    public void choreSkip(Chore chore)
    {
        container.ChoreManager.skip(chore, timeService.getNow());
    }

    public void chorePostpone(Chore chore)
    {
        container.ChoreManager.postpone(chore, timeService.getNow());
    }

    public int getRemainingEffort()
    {
        return container.ChoreManager.getEffortTracker().getTodaysEffort(timeService.getNow());
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
        return container.TaskManager.getTodaysTasks(timeService.getNow());
    }

    public void taskDone(Task task)
    {
        container.TaskManager.complete(task, timeService.getNow());
    }

    public void taskUndone(Task task)
    {
        container.TaskManager.undo(task);
    }

    public void taskPostpone(Task task)
    {
        container.TaskManager.postpone(task, timeService.getNow());
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

    public DateTime getNextRoutineProcedureTime()
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

    public int getCalorieIncrementPerDay()
    {
        return container.CaloriesManager.getIncrementPerDay();
    }

    public void setCalorieIncrementPerDay(int incrementPerDay)
    {
        container.CaloriesManager.setIncrementPerDay(
            timeService.getNow().toLocalDate(),
            incrementPerDay);
    }

    public int getAvailableCalories()
    {
        return container.CaloriesManager.getAvailable(timeService.getNow().toLocalDateTime());
    }

    public void addCalorieConsumption(String description, int calories)
    {
        CalorieConsumption consumption = new CalorieConsumption(
            description,
            calories,
            timeService.getNow().toLocalDateTime());

        container.CaloriesManager.addConsumption(consumption);
    }
}
