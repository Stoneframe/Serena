package stoneframe.chorelist.model;

import androidx.annotation.NonNull;

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
            .sorted(getTaskComparator())
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

    public void postpone(Task task, DateTime now)
    {
        task.setIgnoreBefore(now.withTimeAtStartOfDay().plusDays(1));
    }

    public void clean(DateTime now)
    {
        tasks.removeIf(t -> t.isDone() && t.getCompleted().plusWeeks(1).isBefore(now));
    }

    @NonNull
    private static Comparator<Task> getTaskComparator()
    {
        return (task1, task2) ->
        {
            int comparison = Boolean.compare(task1.isDone(), task2.isDone());

            if (comparison != 0)
            {
                return comparison;
            }

            if (task1.isDone() && task2.isDone())
            {
                return task2.getCompleted().compareTo(task1.getCompleted());
            }

            return task1.getDeadline().compareTo(task2.getDeadline());
        };
    }
}
