package stoneframe.chorelist;

import java.util.List;

import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.ChoreManager;
import stoneframe.chorelist.model.ChoreSelector;
import stoneframe.chorelist.model.EffortTracker;
import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.TaskManager;
import stoneframe.chorelist.model.TimeService;

public class ChoreList
{
    private final TimeService timeService;
    private final ChoreManager choreManager;
    private final TaskManager taskManager;

    public ChoreList(
        TimeService timeService,
        EffortTracker effortTracker,
        ChoreSelector choreSelector)
    {
        this.timeService = timeService;

        choreManager = new ChoreManager(effortTracker, choreSelector);
        taskManager = new TaskManager();
    }

    public List<Chore> getAllChores()
    {
        return choreManager.getAllChores();
    }

    public void addChore(Chore chore)
    {
        choreManager.addChore(chore);
    }

    public void removeChore(Chore chore)
    {
        choreManager.removeChore(chore);
    }

    public List<Chore> getTodaysChores()
    {
        return choreManager.getChores(timeService.getNow());
    }

    public void choreDone(Chore chore)
    {
        choreManager.complete(chore, timeService.getNow());
    }

    public void choreSkip(Chore chore)
    {
        choreManager.skip(chore, timeService.getNow());
    }

    public int getRemainingEffort()
    {
        return choreManager.getEffortTracker().getTodaysEffort(timeService.getNow());
    }

    public EffortTracker getEffortTracker()
    {
        return choreManager.getEffortTracker();
    }

    public List<Task> getAllTasks(boolean includeCompleted)
    {
        return taskManager.getAllTasks(includeCompleted);
    }

    public void addTask(Task task)
    {
        taskManager.addTask(task);
    }

    public void removeTask(Task task)
    {
        taskManager.removeTask(task);
    }

    public List<Task> getTodaysTasks()
    {
        return taskManager.getTodaysTasks(timeService.getNow());
    }

    public void taskDone(Task task)
    {
        taskManager.complete(task);
    }
}
