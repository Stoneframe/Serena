package stoneframe.serena.tasks;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.LocalDate;

import stoneframe.serena.Editor;
import stoneframe.serena.timeservices.TimeService;

public class TaskEditor extends Editor<TaskEditor.TaskEditorListener>
{
    private final TaskManager taskManager;
    private final Task task;

    private final PropertyUtil<String> descriptionProperty;
    private final PropertyUtil<LocalDate> deadlineProperty;
    private final PropertyUtil<LocalDate> ignoreBeforeProperty;
    private final PropertyUtil<Boolean> isDoneProperty;
    private final PropertyUtil<String> senderNameProperty;

    public TaskEditor(TaskManager taskManager, Task task, TimeService timeService)
    {
        super(timeService);

        this.taskManager = taskManager;
        this.task = task;

        descriptionProperty = getDescriptionProperty();
        deadlineProperty = getDeadlineProperty();
        ignoreBeforeProperty = getIgnoreBeforeProperty();
        isDoneProperty = getIsDoneProperty();
        senderNameProperty = getSenderNameProperty();
    }

    public String getDescription()
    {
        return descriptionProperty.getValue();
    }

    public void setDescription(String description)
    {
        descriptionProperty.setValue(description);
    }

    public @Nullable LocalDate getDeadline()
    {
        LocalDate deadline = deadlineProperty.getValue();

        return Task.MAXIMUM_DEADLINE.equals(deadline) ? null : deadline;
    }

    public void setDeadline(@Nullable LocalDate deadline)
    {
        deadlineProperty.setValue(deadline == null ? Task.MAXIMUM_DEADLINE : deadline);
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

    @Nullable
    public String getSenderName()
    {
        return senderNameProperty.getValue();
    }

    public void setSenderName(@Nullable String senderName)
    {
        senderNameProperty.setValue(senderName);
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

    private @NonNull PropertyUtil<String> getDescriptionProperty()
    {
        return new PropertyUtil<>(
            task::getDescription,
            task::setDescription,
            v -> notifyListeners(TaskEditorListener::descriptionChanged));
    }

    private @NonNull PropertyUtil<LocalDate> getDeadlineProperty()
    {
        return new PropertyUtil<>(
            task::getDeadline,
            task::setDeadline,
            v -> notifyListeners(TaskEditorListener::deadlineChanged));
    }

    private @NonNull PropertyUtil<LocalDate> getIgnoreBeforeProperty()
    {
        return new PropertyUtil<>(
            task::getIgnoreBefore,
            task::setIgnoreBefore,
            v -> notifyListeners(TaskEditorListener::ignoreBeforeChanged));
    }

    private @NonNull PropertyUtil<Boolean> getIsDoneProperty()
    {
        return new PropertyUtil<>(
            task::isDone,
            isDone ->
            {
                if (isDone)
                {
                    taskManager.complete(task);
                }
                else
                {
                    taskManager.undo(task);
                }
            },
            v -> notifyListeners(TaskEditorListener::isDoneChanged));
    }

    private PropertyUtil<String> getSenderNameProperty()
    {
        return new PropertyUtil<>(
            task::getSenderName,
            task::setSenderName,
            v -> notifyListeners(TaskEditorListener::senderNameChanged));
    }

    public interface TaskEditorListener
    {
        void descriptionChanged();

        void deadlineChanged();

        void ignoreBeforeChanged();

        void isDoneChanged();

        void senderNameChanged();
    }
}
