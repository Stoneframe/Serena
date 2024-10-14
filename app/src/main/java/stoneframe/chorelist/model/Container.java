package stoneframe.chorelist.model;

import stoneframe.chorelist.model.checklists.ChecklistContainer;
import stoneframe.chorelist.model.chores.ChoreContainer;
import stoneframe.chorelist.model.limiters.LimiterContainer;

public class Container
{
    public int Version = 0;

    public ChoreContainer ChoreManager;

    public stoneframe.chorelist.model.tasks.TaskManager TaskManager;

    public stoneframe.chorelist.model.routines.RoutineManager RoutineManager;

    public ChecklistContainer ChecklistManager;

    public LimiterContainer LimiterManager;
}
