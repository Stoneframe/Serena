package stoneframe.chorelist.model;

import org.joda.time.DateTime;

public class Task
{
    private final String description;
    private final DateTime deadline;
    private final DateTime ignoreBefore;

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

    public DateTime getDeadline()
    {
        return deadline == null ? new DateTime(Long.MIN_VALUE) : deadline;
    }

    public DateTime getIgnoreBefore()
    {
        return ignoreBefore == null ? new DateTime(Long.MIN_VALUE) : ignoreBefore;
    }

    public boolean isDone()
    {
        return isDone;
    }

    void markAsDone()
    {
        isDone = true;
    }
}
