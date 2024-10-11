package stoneframe.chorelist.model.tasks;

import org.joda.time.LocalDate;

import stoneframe.chorelist.model.Editor;
import stoneframe.chorelist.model.timeservices.TimeService;

public class TaskEditor extends Editor<TaskEditor.TaskEditorListener>
{
    private final TaskManager taskManager;
    private final Task task;

    private final PropertyUtil<String> descriptionProperty;
    private final PropertyUtil<LocalDate> deadlineProperty;
    private final PropertyUtil<LocalDate> ignoreBeforeProperty;
    private final PropertyUtil<Boolean> isDoneProperty;

    public TaskEditor(TaskManager taskManager, Task task, TimeService timeService)
    {
        super(timeService);

        this.taskManager = taskManager;
        this.task = task;

        descriptionProperty = new PropertyUtil<>(
            task::getDescription,
            task::setDescription,
            v -> notifyListeners(TaskEditorListener::descriptionChanged));

        deadlineProperty = new PropertyUtil<>(
            task::getDeadline,
            task::setDeadline,
            v -> notifyListeners(TaskEditorListener::deadlineChanged));

        ignoreBeforeProperty = new PropertyUtil<>(
            task::getIgnoreBefore,
            task::setIgnoreBefore,
            v -> notifyListeners(TaskEditorListener::ignoreBeforeChanged));

        isDoneProperty = new PropertyUtil<>(
            task::isDone,
            isDone ->
            {
                if (isDone)
                {
                    taskManager.complete(task, timeService.getToday());
                }
                else
                {
                    taskManager.undo(task);
                }
            },
            v -> notifyListeners(TaskEditorListener::isDoneChanged));
    }

    public String getDescription()
    {
        return descriptionProperty.getValue();
    }

    public void setDescription(String description)
    {
        descriptionProperty.setValue(description);
    }

    public LocalDate getDeadline()
    {
        return deadlineProperty.getValue();
    }

    public void setDeadline(LocalDate deadline)
    {
        deadlineProperty.setValue(deadline);
    }

    public LocalDate getIgnoreBefore()
    {
        return ignoreBeforeProperty.getValue();
    }

    public void setIgnoreBefore(LocalDate ignoreBefore)
    {
        ignoreBeforeProperty.setValue(ignoreBefore);
    }

    public boolean isDone()
    {
        return isDoneProperty.getValue();
    }

    public void setDone(boolean isDone)
    {
        isDoneProperty.setValue(isDone);
    }

    public void save()
    {
        if (!taskManager.containsTask(task))
        {
            taskManager.addTask(task);
        }
    }

    public void remove()
    {
        if (taskManager.containsTask(task))
        {
            taskManager.removeTask(task);
        }
    }

    public interface TaskEditorListener
    {
        void descriptionChanged();

        void deadlineChanged();

        void ignoreBeforeChanged();

        void isDoneChanged();
    }
}
