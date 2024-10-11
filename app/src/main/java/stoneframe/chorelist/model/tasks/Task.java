package stoneframe.chorelist.model.tasks;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

public class Task
{
    private String description;
    private LocalDate deadline;
    private LocalDate ignoreBefore;
    private LocalDate completed;
    private boolean isDone;

    public Task(String description, LocalDate deadline, LocalDate ignoreBefore)
    {
        this.description = description;
        this.deadline = deadline;
        this.ignoreBefore = ignoreBefore;
        this.completed = null;
    }

    public String getDescription()
    {
        return description;
    }

    public LocalDate getDeadline()
    {
        return deadline == null ? new LocalDate(Long.MIN_VALUE) : deadline;
    }

    public LocalDate getIgnoreBefore()
    {
        return ignoreBefore == null ? new LocalDate(-292275055, 1, 1) : ignoreBefore;
    }

    public boolean isDone()
    {
        return isDone;
    }

    public LocalDate getCompleted()
    {
        return completed;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    void setDeadline(LocalDate deadline)
    {
        this.deadline = deadline;
    }

    void setIgnoreBefore(LocalDate ignoreBefore)
    {
        this.ignoreBefore = ignoreBefore;
    }

    void setDone(boolean done, LocalDate now)
    {
        isDone = done;
        completed = done ? now : null;
    }

    @NonNull
    @Override
    public String toString()
    {
        return description;
    }
}
