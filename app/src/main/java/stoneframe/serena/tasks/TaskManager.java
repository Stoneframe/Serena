package stoneframe.serena.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import stoneframe.serena.timeservices.TimeService;

public class TaskManager
{
    private final Supplier<TaskContainer> container;

    private final TimeService timeService;

    public TaskManager(Supplier<TaskContainer> container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public List<Task> getAllTasks()
    {
        removeOldCompletedTasks(timeService.getToday());

        List<Task> sortedTasks = getContainer().tasks.stream()
            .sorted(getTaskComparator())
            .collect(Collectors.toList());

        return Collections.unmodifiableList(sortedTasks);
    }

    public List<Task> getTodaysTasks()
    {
        LocalDate today = timeService.getToday();

        removeOldCompletedTasks(today);

        List<Task> selectedTasks = new ArrayList<>();

        for (Task task : getAllEligibleTasks(today))
        {
            if (isUrgent(task, today) || selectedTasks.size() < getRemainingNumberOfTasksToday())
            {
                selectedTasks.add(task);
            }
        }

        return selectedTasks;
    }

    public @Nullable Integer getMaximumNumberOfTasksPerDay()
    {
        return getContainer().maximumNumberOfTasksPerDay;
    }

    public void setMaximumNumberOfTasksPerDay(@Nullable Integer maximumNumberOfTasksPerDay)
    {
        if (maximumNumberOfTasksPerDay != null && maximumNumberOfTasksPerDay < 1)
        {
            throw new IllegalArgumentException("Maximum number of tasks must be at least 1.");
        }

        getContainer().maximumNumberOfTasksPerDay = maximumNumberOfTasksPerDay;
    }

    public void incrementNumberOfTasksCompletedToday()
    {
        resetCompletionCountIfDateChanged();

        getContainer().numberOfTasksCompletedToday++;
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
        if (task.isDone())
        {
            return;
        }

        task.setDone(true, timeService.getToday());

        incrementNumberOfTasksCompletedToday();
    }

    public void undo(Task task)
    {
        if (!task.isDone())
        {
            return;
        }

        if (timeService.getToday().equals(task.getCompleted()))
        {
            decrementNumberOfTasksCompletedToday();
        }

        task.setDone(false, null);
    }

    boolean containsTask(Task task)
    {
        return getContainer().tasks.contains(task);
    }

    void addTask(Task task)
    {
        getContainer().tasks.add(task);
    }

    void removeTask(Task task)
    {
        getContainer().tasks.remove(task);
    }

    private void removeOldCompletedTasks(LocalDate today)
    {
        getContainer().tasks.removeIf(isCompletedOverOneWeekAgo(today));
    }

    private int getNumberOfTasksCompletedToday()
    {
        resetCompletionCountIfDateChanged();

        return getContainer().numberOfTasksCompletedToday;
    }

    private void decrementNumberOfTasksCompletedToday()
    {
        resetCompletionCountIfDateChanged();

        if (getContainer().numberOfTasksCompletedToday == 0)
        {
            return;
        }

        getContainer().numberOfTasksCompletedToday--;
    }

    private void resetCompletionCountIfDateChanged()
    {
        LocalDate today = timeService.getToday();

        if (!today.equals(getContainer().taskCompletionCountDate))
        {
            getContainer().taskCompletionCountDate = today;
            getContainer().numberOfTasksCompletedToday = 0;
        }
    }

    private static @NonNull Predicate<Task> isCompletedOverOneWeekAgo(LocalDate today)
    {
        return t -> t.isDone() && t.getCompleted().plusWeeks(1).isBefore(today);
    }

    private @NonNull List<Task> getAllEligibleTasks(LocalDate today)
    {
        return getContainer().tasks.stream()
            .filter(t -> !t.isDone())
            .filter(t -> t.getIgnoreBefore().isBefore(today) || t.getIgnoreBefore().isEqual(today))
            .sorted(getTaskComparator())
            .collect(Collectors.toList());
    }

    private int getRemainingNumberOfTasksToday()
    {
        Integer maxNumberOfTasksToday = getMaximumNumberOfTasksPerDay();

        if (maxNumberOfTasksToday == null)
        {
            maxNumberOfTasksToday = Integer.MAX_VALUE;
        }

        return Math.max(0, maxNumberOfTasksToday - getNumberOfTasksCompletedToday());
    }

    private static boolean isUrgent(Task task, LocalDate today)
    {
        LocalDate urgentDeadlineCutoff = today.plusWeeks(1);

        return task.getDeadline().isBefore(urgentDeadlineCutoff);
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

    private TaskContainer getContainer()
    {
        return container.get();
    }
}
