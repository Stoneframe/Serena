package stoneframe.chorelist.model;

import stoneframe.chorelist.model.checklists.ChecklistContainer;
import stoneframe.chorelist.model.chores.ChoreContainer;
import stoneframe.chorelist.model.limiters.LimiterContainer;
import stoneframe.chorelist.model.routines.RoutineContainer;
import stoneframe.chorelist.model.tasks.TaskContainer;

public class Container
{
    public int Version = 0;

    public ChoreContainer ChoreManager;

    public TaskContainer TaskManager;

    public RoutineContainer RoutineManager;

    public ChecklistContainer ChecklistManager;

    public LimiterContainer LimiterManager;
}
