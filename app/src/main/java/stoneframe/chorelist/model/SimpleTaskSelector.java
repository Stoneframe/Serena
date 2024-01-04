package stoneframe.chorelist.model;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class SimpleTaskSelector implements TaskSelector
{
    @Override
    public List<Task> selectTasks(List<Task> tasks, int effort)
    {
        List<Task> selectedTasks = new LinkedList<>();

        if (effort <= 0)
        {
            return tasks.stream().filter(t -> t.getEffort() == 0).collect(Collectors.toList());
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
