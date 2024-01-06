package stoneframe.chorelist;

import org.joda.time.DateTime;

import java.util.List;

import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.ChoreSelector;
import stoneframe.chorelist.model.EffortTracker;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleChoreSelector;

public class ChoreList
{
    private final Schedule schedule;

    public ChoreList(EffortTracker effortTracker, ChoreSelector choreSelector)
    {
        schedule = new Schedule(effortTracker, choreSelector);
    }

    public List<Chore> getAllChores()
    {
        return schedule.getAllChores();
    }

    public void addChore(Chore chore)
    {
        schedule.addChore(chore);
    }

    public void removeChore(Chore chore)
    {
        schedule.removeChore(chore);
    }

    public List<Chore> getTodaysChores(DateTime now)
    {
        return schedule.getChores(now);
    }

    public void choreDone(Chore chore, DateTime now)
    {
        schedule.complete(chore, now);
    }

    public int getRemainingEffort(DateTime now)
    {
        return schedule.getEffortTracker().getTodaysEffort(now);
    }
}
