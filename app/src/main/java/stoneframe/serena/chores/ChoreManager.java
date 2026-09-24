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

    public boolean skip(Chore chore)
    {
        LocalDate today = timeService.getToday();

        if (!isValidChoreAndDueToday(chore, today))
        {
            return false;
        }

        chore.reschedule(today);

        return true;
    }

    public boolean postpone(Chore chore)
    {
        LocalDate today = timeService.getToday();

        if (!isValidChoreAndDueToday(chore, today))
        {
            return false;
        }

        chore.postpone(today);

        return true;
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

    private boolean isValidChoreAndDueToday(Chore chore, LocalDate today)
    {
        if (chore == null)
        {
            return false;
        }

        boolean isCurrentChore = getContainer().chores.stream().anyMatch(c -> c == chore);

        return isCurrentChore && chore.isEnabled() && chore.isTimeToDo(today);
    }

    private int getEffortSpent(Chore chore)
    {
        List<Chore> todaysChores = getTodaysChores();

        if (!isLastChoreInList(chore, todaysChores))
        {
            return chore.getEffort();
        }

        return getLastChoreEffort(chore, todaysChores);
    }

    private int getLastChoreEffort(Chore chore, List<Chore> todaysChores)
    {
        int todaysEffort = getEffortTracker().getTodaysEffort(timeService.getToday());

        return Math.max(0, Math.min(chore.getEffort(), todaysEffort - getSumOfEffortExceptLast(todaysChores)));
    }

    private static boolean isLastChoreInList(Chore chore, List<Chore> todaysChores)
    {
        return !todaysChores.isEmpty() && todaysChores.get(todaysChores.size() - 1).equals(chore);
    }

    private static int getSumOfEffortExceptLast(List<Chore> todaysChores)
    {
        return todaysChores.subList(0, todaysChores.size() - 1).stream()
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
