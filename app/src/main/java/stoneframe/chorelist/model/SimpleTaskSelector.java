package stoneframe.chorelist.model;

import java.util.LinkedList;
import java.util.List;

public class SimpleTaskSelector implements TaskSelector
{

    public SimpleTaskSelector()
    {
    }

    @Override
    public List<Task> selectTasks(List<Task> tasks, int effort)
    {
        List<Task> selectedTasks = new LinkedList<>();

        if (effort <= 0)
        {
            return selectedTasks;
        }

        int currEffort = 0;
        for (Task task : tasks)
        {
            currEffort += task.getEffort();
            selectedTasks.add(task);
            if (currEffort >= effort)
            {
                break;
            }
        }

        return selectedTasks;
    }

}
