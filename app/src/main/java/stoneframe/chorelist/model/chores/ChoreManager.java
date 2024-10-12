package stoneframe.chorelist.model.chores;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import stoneframe.chorelist.model.timeservices.TimeService;

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

    public ChoreEditor getChoreEditor(Chore chore, TimeService timeService)
    {
        return new ChoreEditor(this, chore, timeService);
    }

    public Chore createChore(LocalDate today)
    {
        return new Chore("", 1, 1, today, 1, Chore.DAYS);
    }

    public List<Chore> getAllChores()
    {
        chores.sort(Comparator.comparing(Chore::getDescription));
        return Collections.unmodifiableList(chores);
    }

    public boolean containsChore(Chore chore)
    {
        return chores.contains(chore);
    }

    public List<Chore> getChores(LocalDate today)
    {
        List<Chore> eligibleChores = getAllEligibleChores(today);

        return choreSelector.selectChores(eligibleChores, effortTracker.getTodaysEffort(today))
            .stream()
            .sorted(new Chore.ChoreComparator(today))
            .collect(Collectors.toList());
    }

    public void complete(Chore chore, LocalDate today)
    {
        int effortSpent = getEffortSpent(chore, today);

        effortTracker.spend(effortSpent);

        chore.reschedule(today);
    }

    public void skip(Chore chore, LocalDate today)
    {
        chore.reschedule(today);
    }

    public void postpone(Chore chore, LocalDate today)
    {
        chore.postpone(today);
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

    void addChore(Chore chore)
    {
        chores.add(chore);
    }

    void removeChore(Chore chore)
    {
        chores.remove(chore);
    }

    private @NonNull List<Chore> getAllEligibleChores(LocalDate today)
    {
        return chores.stream()
            .sorted(new Chore.ChoreComparator(today))
            .filter(Chore::isEnabled)
            .filter(c -> c.isTimeToDo(today))
            .collect(Collectors.toList());
    }

    private int getEffortSpent(Chore chore, LocalDate today)
    {
        List<Chore> todaysChores = getChores(today);

        if (todaysChores.size() <= 1 || !isLastChoreInList(chore, todaysChores))
        {
            return chore.getEffort();
        }

        return effortTracker.getTodaysEffort(today) - getSumOfEffortExceptLast(todaysChores);
    }

    private static boolean isLastChoreInList(Chore chore, List<Chore> todaysChores)
    {
        Chore lastChoreInList = todaysChores.get(todaysChores.size() - 1);

        return lastChoreInList.equals(chore);
    }

    private static int getSumOfEffortExceptLast(List<Chore> todaysChores)
    {
        return todaysChores.stream()
            .limit(todaysChores.size() - 1)
            .mapToInt(Chore::getEffort)
            .sum();
    }
}
