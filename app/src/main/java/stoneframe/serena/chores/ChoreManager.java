package stoneframe.serena.chores;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import stoneframe.serena.timeservices.TimeService;

public class ChoreManager
{
    private final Supplier<ChoreContainer> container;

    private final TimeService timeService;

    public ChoreManager(Supplier<ChoreContainer> container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public EffortTracker getEffortTracker()
    {
        return getContainer().effortTracker;
    }

    public int getRemainingEffort()
    {
        return getContainer().effortTracker.getTodaysEffort(timeService.getToday());
    }

    public ChoreEditor getChoreEditor(Chore chore)
    {
        return new ChoreEditor(this, chore, timeService);
    }

    public Chore createChore()
    {
        return new Chore("", 1, 1, timeService.getToday(), 1, IntervalRepetition.DAYS);
    }

    public List<Chore> getAllChores()
    {
        getContainer().chores.sort(Comparator.comparing(Chore::getDescription));
        return Collections.unmodifiableList(getContainer().chores);
    }

    public boolean containsChore(Chore chore)
    {
        return getContainer().chores.contains(chore);
    }

    public List<Chore> getTodaysChores()
    {
        LocalDate today = timeService.getToday();

        List<Chore> eligibleChores = getAllEligibleChores(today);

        return getChoreSelector().selectChores(
                eligibleChores,
                getEffortTracker().getTodaysEffort(today))
            .stream()
            .sorted(new Chore.ChoreComparator(today))
            .collect(Collectors.toList());
    }

    public void complete(Chore chore)
    {
        int effortSpent = getEffortSpent(chore);

        getEffortTracker().spend(effortSpent);

        chore.reschedule(timeService.getToday());
    }

    public void skip(Chore chore)
    {
        chore.reschedule(timeService.getToday());
    }

    public void postpone(Chore chore)
    {
        chore.postpone(timeService.getToday());
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof ChoreManager))
        {
            return false;
        }

        ChoreManager other = (ChoreManager)obj;

        return this.getContainer().chores.equals(other.getContainer().chores);
    }

    void addChore(Chore chore)
    {
        getContainer().chores.add(chore);
    }

    void removeChore(Chore chore)
    {
        getContainer().chores.remove(chore);
    }

    private @NonNull List<Chore> getAllEligibleChores(LocalDate today)
    {
        return getContainer().chores.stream()
            .sorted(new Chore.ChoreComparator(today))
            .filter(Chore::isEnabled)
            .filter(c -> c.isTimeToDo(today))
            .collect(Collectors.toList());
    }

    private int getEffortSpent(Chore chore)
    {
        List<Chore> todaysChores = getTodaysChores();

        if (todaysChores.size() <= 1 || !isLastChoreInList(chore, todaysChores))
        {
            return chore.getEffort();
        }

        return getEffortTracker().getTodaysEffort(timeService.getToday()) - getSumOfEffortExceptLast(
            todaysChores);
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

    private ChoreSelector getChoreSelector()
    {
        return getContainer().choreSelector;
    }

    private ChoreContainer getContainer()
    {
        return container.get();
    }
}
