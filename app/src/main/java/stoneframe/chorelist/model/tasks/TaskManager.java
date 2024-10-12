package stoneframe.chorelist.model.tasks;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import stoneframe.chorelist.model.timeservices.TimeService;

public class TaskManager
{
    private final List<Task> tasks = new LinkedList<>();

    public List<Task> getTodaysTasks(LocalDate today)
    {
        return getAllTasks(false, today).stream()
            .filter(t -> t.getIgnoreBefore().isBefore(today) || t.getIgnoreBefore().isEqual(today))
            .collect(Collectors.toList());
    }

    public List<Task> getAllTasks(boolean includeCompleted, LocalDate today)
    {
        removeOldCompletedTasks(today);

        List<Task> sortedTasks = tasks.stream()
            .filter(t -> !t.isDone() || includeCompleted)
            .sorted(getTaskComparator())
            .collect(Collectors.toList());

        return Collections.unmodifiableList(sortedTasks);
    }

    public TaskEditor getEditor(Task task, TimeService timeService)
    {
        return new TaskEditor(this, task, timeService);
    }

    public Task createTask(TimeService timeService)
    {
        return new Task("", timeService.getToday(), timeService.getToday());
    }

    public void addTask(Task task)
    {
        tasks.add(task);
    }

    public void removeTask(Task task)
    {
        tasks.remove(task);
    }

    public void complete(Task task, LocalDate today)
    {
        task.setDone(true, today);
    }

    public void undo(Task task)
    {
        task.setDone(false, null);
    }

    boolean containsTask(Task task)
    {
        return tasks.contains(task);
    }

    private void removeOldCompletedTasks(LocalDate today)
    {
        tasks.removeIf(t -> t.isDone() && t.getCompleted().plusWeeks(1).isBefore(today));
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
