package stoneframe.chorelist.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

public class Task
{
    private String description;
    private DateTime deadline;
    private DateTime ignoreBefore;
    private DateTime completed;
    private boolean isDone;

    public Task(String description, DateTime deadline, DateTime ignoreBefore)
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

    public DateTime getCompleted()
    {
        return completed;
    }

    void setDone(boolean done, DateTime now)
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
