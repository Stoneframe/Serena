package stoneframe.serena.model;

public class Container
{
    public int Version = 0;

    public stoneframe.serena.model.chores.ChoreManager ChoreManager;

    public stoneframe.serena.model.tasks.TaskManager TaskManager;

    public stoneframe.serena.model.routines.RoutineManager RoutineManager;

    public stoneframe.serena.model.checklists.ChecklistManager ChecklistManager;

    public stoneframe.serena.model.limiters.LimiterManager LimiterManager;
}
