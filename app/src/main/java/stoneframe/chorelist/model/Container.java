package stoneframe.chorelist.model;

import stoneframe.chorelist.model.calories.CaloriesManager;

public class Container
{
    public int Version = 0;

    public stoneframe.chorelist.model.chores.ChoreManager ChoreManager;

    public stoneframe.chorelist.model.tasks.TaskManager TaskManager;

    public stoneframe.chorelist.model.routines.RoutineManager RoutineManager;

    public stoneframe.chorelist.model.checklists.ChecklistManager ChecklistManager;

    public CaloriesManager CaloriesManager;
}
