package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TodoList
{
    private final List<Task> tasks = new LinkedList<>();

    private final TimeService timeService;

    public TodoList(TimeService timeService)
    {
        this.timeService = timeService;
    }

    public List<Task> getAllTasks(boolean includeCompleted)
    {
        List<Task> sortedTasks = tasks.stream()
            .filter(t -> !t.isDone() || includeCompleted)
            .sorted(Comparator.comparing(Task::getDeadline))
            .collect(Collectors.toList());

        return Collections.unmodifiableList(sortedTasks);
    }

    public void addTask(Task task)
    {
        tasks.add(task);
    }

    public void removeTask(Task task)
    {
        tasks.remove(task);
    }

    public List<Task> getTodaysTasks()
    {
        DateTime now = timeService.getNow();

        return getAllTasks(false).stream()
            .filter(t -> t.getIgnoreBefore().isBefore(now) || t.getIgnoreBefore().isEqual(now))
            .collect(Collectors.toList());
    }

    public void complete(Task task)
    {
        task.markAsDone();
    }
}
