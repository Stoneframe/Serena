package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class ChoreManager
{
    private final EffortTracker effortTracker;
    private final ChoreSelector choreSelector;

    private final List<Chore> chores = new LinkedList<>();

    public ChoreManager(EffortTracker effortTracker, ChoreSelector choreSelector)
    {
        this.effortTracker = effortTracker;
        this.choreSelector = choreSelector;
    }

    public EffortTracker getEffortTracker()
    {
        return effortTracker;
    }

    public void addChore(Chore chore)
    {
        chores.add(chore);
    }

    public void removeChore(Chore chore)
    {
        chores.remove(chore);
    }

    public List<Chore> getAllChores()
    {
        chores.sort(Comparator.comparing(Chore::getDescription));
        return Collections.unmodifiableList(chores);
    }

    public List<Chore> getChores()
    {
        return getChores(DateTime.now());
    }

    public List<Chore> getChores(DateTime now)
    {
        chores.sort(new Chore.ChoreComparator(now));

        int effort = effortTracker.getTodaysEffort(now);

        List<Chore> list = new LinkedList<>();

        for (Chore chore : chores)
        {
            if (chore.getNext().isAfter(now))
            {
                break;
            }
            else
            {
                list.add(chore);
            }
        }

        list = choreSelector.selectChores(list, effort);

        return Collections.unmodifiableList(list);
    }

    public void complete(Chore chore)
    {
        complete(chore, DateTime.now());
    }

    public void complete(Chore chore, DateTime now)
    {
        chore.reschedule(now);
        effortTracker.spend(chore.getEffort());
    }

    public void skip(Chore chore)
    {
        skip(chore, DateTime.now());
    }

    public void skip(Chore chore, DateTime now)
    {
        chore.reschedule(now);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ChoreManager))
        {
            return false;
        }

        ChoreManager other = (ChoreManager)obj;

        return this.chores.equals(other.chores);
    }
}
