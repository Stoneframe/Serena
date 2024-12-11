package stoneframe.serena.model.tasks;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

import stoneframe.serena.model.util.Revertible;

public class Task extends Revertible<TaskData>
{
    Task(String description, LocalDate deadline, LocalDate ignoreBefore)
    {
        super(new TaskData(
            description.trim(),
            deadline,
            ignoreBefore,
            null,
            false));
    }

    public String getDescription()
    {
        return data().description;
    }

    public LocalDate getDeadline()
    {
        return data().deadline == null ? new LocalDate(Long.MIN_VALUE) : data().deadline;
    }

    public LocalDate getIgnoreBefore()
    {
        return data().ignoreBefore == null ? new LocalDate(-292275055, 1, 1) : data().ignoreBefore;
    }

    public boolean isDone()
    {
        return data().isDone;
    }

    public LocalDate getCompleted()
    {
        return data().completed;
    }

    void setDescription(String description)
    {
        data().description = description.trim();
    }

    void setDeadline(LocalDate deadline)
    {
        data().deadline = deadline;
    }

    void setIgnoreBefore(LocalDate ignoreBefore)
    {
        data().ignoreBefore = ignoreBefore;
    }

    void setDone(boolean done, LocalDate now)
    {
        data().isDone = done;
        data().completed = done ? now : null;
    }

    @NonNull
    @Override
    public String toString()
    {
        return data().description;
    }
}
