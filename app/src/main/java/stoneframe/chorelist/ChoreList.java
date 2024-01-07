package stoneframe.chorelist;

import org.joda.time.DateTime;

import java.util.List;

import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.ChoreSelector;
import stoneframe.chorelist.model.EffortTracker;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.TimeService;

public class ChoreList
{
    private final TimeService timeService;
    private final Schedule schedule;

    public ChoreList(
        TimeService timeService,
        EffortTracker effortTracker,
        ChoreSelector choreSelector)
    {
        this.timeService = timeService;

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

    public List<Chore> getTodaysChores()
    {
        return schedule.getChores(timeService.getNow());
    }

    public void choreDone(Chore chore)
    {
        schedule.complete(chore, timeService.getNow());
    }

    public int getRemainingEffort()
    {
        return schedule.getEffortTracker().getTodaysEffort(timeService.getNow());
    }
}
