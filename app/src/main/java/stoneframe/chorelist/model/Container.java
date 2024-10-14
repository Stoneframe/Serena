package stoneframe.chorelist.model;

import stoneframe.chorelist.model.chores.ChoreContainer;

public class Container
{
    public int Version = 0;

    public ChoreContainer ChoreManager;

    public stoneframe.chorelist.model.tasks.TaskManager TaskManager;

    public stoneframe.chorelist.model.routines.RoutineManager RoutineManager;

    public stoneframe.chorelist.model.checklists.ChecklistManager ChecklistManager;

    public stoneframe.chorelist.model.limiters.LimiterManager LimiterManager;
}
