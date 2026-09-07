package stoneframe.serena.tasks;

import org.joda.time.LocalDate;

import java.util.LinkedList;
import java.util.List;

public class TaskContainer
{
    final List<Task> tasks = new LinkedList<>();

    Integer maximumNumberOfTasksPerDay;
    int numberOfTasksCompletedToday;
    LocalDate taskCompletionCountDate;
}
