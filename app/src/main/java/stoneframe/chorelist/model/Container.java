package stoneframe.chorelist.model;

import stoneframe.chorelist.model.checklists.ChecklistContainer;
import stoneframe.chorelist.model.chores.ChoreContainer;
import stoneframe.chorelist.model.limiters.LimiterContainer;
import stoneframe.chorelist.model.routines.RoutineContainer;
import stoneframe.chorelist.model.tasks.TaskContainer;

public class Container
{
    public int Version = 0;

    public ChoreContainer ChoreContainer;

    public TaskContainer TaskContainer;

    public RoutineContainer RoutineContainer;

    public ChecklistContainer ChecklistContainer;

    public LimiterContainer LimiterContainer;
}
