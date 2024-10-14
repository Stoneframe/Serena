package stoneframe.chorelist.model;

import stoneframe.chorelist.model.checklists.ChecklistContainer;
import stoneframe.chorelist.model.chores.ChoreContainer;

public class Container
{
    public int Version = 0;

    public ChoreContainer ChoreManager;

    public stoneframe.chorelist.model.tasks.TaskManager TaskManager;

    public stoneframe.chorelist.model.routines.RoutineManager RoutineManager;

    public ChecklistContainer ChecklistManager;

    public stoneframe.chorelist.model.limiters.LimiterManager LimiterManager;
}
