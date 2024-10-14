package stoneframe.chorelist.model.tasks;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import stoneframe.chorelist.model.timeservices.TimeService;

public class TaskManager
{
    private final TaskContainer container;

    private final TimeService timeService;

    public TaskManager(TaskContainer container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public List<Task> getAllTasks()
    {
        removeOldCompletedTasks(timeService.getToday());

        List<Task> sortedTasks = container.tasks.stream()
            .sorted(getTaskComparator())
            .collect(Collectors.toList());

        return Collections.unmodifiableList(sortedTasks);
    }

    public List<Task> getTodaysTasks()
    {
        LocalDate today = timeService.getToday();

        removeOldCompletedTasks(today);

        return container.tasks.stream()
            .filter(t -> !t.isDone())
            .filter(t -> t.getIgnoreBefore().isBefore(today) || t.getIgnoreBefore().isEqual(today))
            .sorted(getTaskComparator())
            .collect(Collectors.toList());
    }

    public TaskEditor getTaskEditor(Task task)
    {
        return new TaskEditor(this, task, timeService);
    }

    public Task createTask()
    {
        return new Task("", timeService.getToday(), timeService.getToday());
    }

    public void complete(Task task)
    {
        task.setDone(true, timeService.getToday());
    }

    public void undo(Task task)
    {
        task.setDone(false, null);
    }

    boolean containsTask(Task task)
    {
        return container.tasks.contains(task);
    }

    void addTask(Task task)
    {
        container.tasks.add(task);
    }

    void removeTask(Task task)
    {
        container.tasks.remove(task);
    }

    private void removeOldCompletedTasks(LocalDate today)
    {
        container.tasks.removeIf(t -> t.isDone() && t.getCompleted().plusWeeks(1).isBefore(today));
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
