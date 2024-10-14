package stoneframe.chorelist.model.tasks;

import org.joda.time.LocalDate;

public class TaskData
{
    String description;
    LocalDate deadline;
    LocalDate ignoreBefore;
    LocalDate completed;
    boolean isDone;

    public TaskData(
        String description,
        LocalDate deadline,
        LocalDate ignoreBefore,
        LocalDate completed,
        boolean isDone)
    {
        this.description = description;
        this.deadline = deadline;
        this.ignoreBefore = ignoreBefore;
        this.completed = completed;
        this.isDone = isDone;
    }
}
