package stoneframe.chorelist.model.tasks;

import org.joda.time.LocalDate;

class TaskData
{
    String description;
    LocalDate deadline;
    LocalDate ignoreBefore;
    LocalDate completed;
    boolean isDone;

    TaskData(
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
