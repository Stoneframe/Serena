package stoneframe.chorelist;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.ChoreSelector;
import stoneframe.chorelist.model.EffortTracker;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.TimeService;
import stoneframe.chorelist.model.TodoList;

public class ChoreList
{
    private final TimeService timeService;
    private final Schedule schedule;
    private final TodoList todoList;

    public ChoreList(
        TimeService timeService,
        EffortTracker effortTracker,
        ChoreSelector choreSelector)
    {
        this.timeService = timeService;

        schedule = new Schedule(effortTracker, choreSelector);
        todoList = new TodoList();
    }

    public List<Chore> getAllChores()
    {
        return schedule.getAllChores();
    }

    public void addChore(Chore chore)
    {
        schedule.addChore(chore);
    }

    public void removeChore(Chore chore)
    {
        schedule.removeChore(chore);
    }

    public List<Chore> getTodaysChores()
    {
        return schedule.getChores(timeService.getNow());
    }

    public void choreDone(Chore chore)
    {
        schedule.complete(chore, timeService.getNow());
    }

    public void choreSkip(Chore chore)
    {
        schedule.skip(chore, timeService.getNow());
    }

    public int getRemainingEffort()
    {
        return schedule.getEffortTracker().getTodaysEffort(timeService.getNow());
    }

    public EffortTracker getEffortTracker()
    {
        return schedule.getEffortTracker();
    }

    public List<Task> getAllTasks()
    {
        return todoList.getAllTasks();
    }

    public void addTask(Task task)
    {
        todoList.addTask(task);
    }
}
