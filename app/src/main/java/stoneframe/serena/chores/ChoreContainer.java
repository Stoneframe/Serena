package stoneframe.serena.chores;

import java.util.LinkedList;
import java.util.List;

public class ChoreContainer
{
    final List<Chore> chores = new LinkedList<>();

    final EffortTracker effortTracker;
    final ChoreSelector choreSelector;

    public ChoreContainer(EffortTracker effortTracker, ChoreSelector choreSelector)
    {
        this.effortTracker = effortTracker;
        this.choreSelector = choreSelector;
    }
}
