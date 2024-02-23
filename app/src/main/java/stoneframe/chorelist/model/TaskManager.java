package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class TaskManager
{
    private final List<Task> tasks = new LinkedList<>();

    public List<Task> getAllTasks(boolean includeCompleted)
    {
        List<Task> sortedTasks = tasks.stream()
            .filter(t -> !t.isDone() || includeCompleted)
            .sorted(Comparator.comparing(Task::isDone).thenComparing(Task::getDeadline))
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

    public List<Task> getTodaysTasks(DateTime now)
    {
        return getAllTasks(false).stream()
            .filter(t -> t.getIgnoreBefore().isBefore(now) || t.getIgnoreBefore().isEqual(now))
            .collect(Collectors.toList());
    }

    public void complete(Task task, DateTime now)
    {
        task.setDone(true, now);
    }

    public void undo(Task task)
    {
        task.setDone(false, null);
    }
}
