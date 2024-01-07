package stoneframe.chorelist.model;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class TodoList
{
    private final List<Task> tasks = new LinkedList<>();

    public List<Task> getAllTasks()
    {
        return Collections.unmodifiableList(tasks);
    }

    public void addTask(Task task)
    {
        tasks.add(task);
    }
}
