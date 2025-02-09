package stoneframe.serena.model;

import stoneframe.serena.model.checklists.ChecklistContainer;
import stoneframe.serena.model.chores.ChoreContainer;
import stoneframe.serena.model.balancers.BalancerContainer;
import stoneframe.serena.model.notes.NoteContainer;
import stoneframe.serena.model.routines.RoutineContainer;
import stoneframe.serena.model.tasks.TaskContainer;

public class Container
{
    public int Version = 0;

    public ChoreContainer ChoreContainer;

    public TaskContainer TaskContainer;

    public RoutineContainer RoutineContainer;

    public ChecklistContainer ChecklistContainer;

    public BalancerContainer BalancerContainer;

    public NoteContainer NoteContainer;
}
