package stoneframe.serena.tasks;

import androidx.annotation.NonNull;

import org.joda.time.DateTimeZone;
import org.joda.time.LocalDate;

import stoneframe.serena.util.Revertible;

public class Task extends Revertible<TaskData>
{
    static final LocalDate MAXIMUM_DEADLINE = new LocalDate(Long.MAX_VALUE, DateTimeZone.UTC);

    Task(String description, LocalDate deadline, LocalDate ignoreBefore)
    {
        super(new TaskData(
            description,
            deadline,
            ignoreBefore,
            null,
            false));
    }

    public String getDescription()
    {
        return data().description;
    }

    void setDescription(String description)
    {
        data().description = description;
    }

    public LocalDate getDeadline()
    {
        return data().deadline;
    }

    void setDeadline(LocalDate deadline)
    {
        data().deadline = deadline;
    }

    public boolean hasDeadline()
    {
        return !MAXIMUM_DEADLINE.equals(getDeadline());
    }

    public LocalDate getIgnoreBefore()
    {
        return data().ignoreBefore == null ? new LocalDate(-292275055, 1, 1) : data().ignoreBefore;
    }

    void setIgnoreBefore(LocalDate ignoreBefore)
    {
        data().ignoreBefore = ignoreBefore;
    }

    public boolean isDone()
    {
        return data().isDone;
    }

    public LocalDate getCompleted()
    {
        return data().completed;
    }

    @NonNull
    @Override
    public String toString()
    {
        return data().description;
    }

    void setDone(boolean done, LocalDate now)
    {
        data().isDone = done;
        data().completed = done ? now : null;
    }

}
