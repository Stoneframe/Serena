package stoneframe.chorelist.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

public class Task
{
    private String description;
    private DateTime deadline;
    private DateTime ignoreBefore;
    private boolean isDone;

    public Task(String description, DateTime deadline, DateTime ignoreBefore)
    {
        this.description = description;
        this.deadline = deadline;
        this.ignoreBefore = ignoreBefore;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public DateTime getDeadline()
    {
        return deadline == null ? new DateTime(Long.MIN_VALUE) : deadline;
    }

    public void setDeadline(DateTime deadline)
    {
        this.deadline = deadline;
    }

    public DateTime getIgnoreBefore()
    {
        return ignoreBefore == null ? new DateTime(Long.MIN_VALUE) : ignoreBefore;
    }

    public void setIgnoreBefore(DateTime ignoreBefore)
    {
        this.ignoreBefore = ignoreBefore;
    }

    public boolean isDone()
    {
        return isDone;
    }

    public void setDone(boolean done)
    {
        isDone = done;
    }

    @NonNull
    @Override
    public String toString()
    {
        return (isDone ? "X" : "O") + " - " + description;
    }
}
