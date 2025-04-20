package stoneframe.serena.chores.efforttrackers;

import org.joda.time.LocalDate;

import stoneframe.serena.chores.EffortTracker;

public class SimpleEffortTracker implements EffortTracker
{
    private final int maxEffort;

    private int remainingEffort;

    private LocalDate previous;

    public SimpleEffortTracker(int effort)
    {
        maxEffort = effort;
        remainingEffort = maxEffort;
    }

    @Override
    public int getTodaysEffort(LocalDate today)
    {
        if (previous == null) previous = today;

        if (!isSameDay(previous, today))
        {
            previous = today;
            remainingEffort = maxEffort;
        }

        return remainingEffort;
    }

    public void setTodaysEffort(int effort)
    {
        remainingEffort = effort;
    }

    @Override
    public void spend(int effort)
    {
        if (effort > remainingEffort)
        {
            remainingEffort = 0;
        }
        else
        {
            remainingEffort -= effort;
        }
    }

    @Override
    public void reset(LocalDate today)
    {
        remainingEffort = maxEffort;
    }

    private boolean isSameDay(LocalDate d1, LocalDate d2)
    {
        return d1 != null && d2 != null
            && d1.getYear() == d2.getYear()
            && d1.getMonthOfYear() == d2.getMonthOfYear()
            && d1.getDayOfMonth() == d2.getDayOfMonth();
    }
}
