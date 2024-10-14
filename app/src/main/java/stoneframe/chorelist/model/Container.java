package stoneframe.chorelist.model;

import stoneframe.chorelist.model.checklists.ChecklistContainer;
import stoneframe.chorelist.model.chores.ChoreContainer;
import stoneframe.chorelist.model.limiters.LimiterContainer;
import stoneframe.chorelist.model.routines.RoutineContainer;

public class Container
{
    public int Version = 0;

    public ChoreContainer ChoreManager;

    public stoneframe.chorelist.model.tasks.TaskManager TaskManager;

    public RoutineContainer RoutineManager;

    public ChecklistContainer ChecklistManager;

    public LimiterContainer LimiterManager;
}
