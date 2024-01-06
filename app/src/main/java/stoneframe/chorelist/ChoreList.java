package stoneframe.chorelist;

import org.joda.time.DateTime;

import java.util.List;

import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleChoreSelector;
import stoneframe.chorelist.model.WeeklyEffortTracker;

public class ChoreList
{
    private final Schedule schedule;

    public ChoreList()
    {
        schedule = new Schedule(
            new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30),
            new SimpleChoreSelector());
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
}
