package stoneframe.serena;

import stoneframe.serena.checklists.ChecklistContainer;
import stoneframe.serena.chores.ChoreContainer;
import stoneframe.serena.balancers.BalancerContainer;
import stoneframe.serena.notes.NoteContainer;
import stoneframe.serena.routines.RoutineContainer;
import stoneframe.serena.sleep.SleepContainer;
import stoneframe.serena.tasks.TaskContainer;

public class Container
{
    public int Version = 0;

    public ChoreContainer ChoreContainer;

    public TaskContainer TaskContainer;

    public RoutineContainer RoutineContainer;

    public ChecklistContainer ChecklistContainer;

    public BalancerContainer BalancerContainer;

    public SleepContainer SleepContainer;

    public NoteContainer NoteContainer;
}
