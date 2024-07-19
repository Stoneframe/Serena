package stoneframe.chorelist.model;

public class Container
{
    public int Version = 0;

    public stoneframe.chorelist.model.chores.ChoreManager ChoreManager;

    public stoneframe.chorelist.model.tasks.TaskManager TaskManager;

    public stoneframe.chorelist.model.routines.RoutineManager RoutineManager;

    public stoneframe.chorelist.model.checklists.ChecklistManager ChecklistManager;

    public stoneframe.chorelist.model.limiters.LimiterManager LimiterManager;
}
