package stoneframe.chorelist.model;

import org.joda.time.DateTime;

public class SimpleEffortTracker implements EffortTracker
{
    private final int maxEffort;

    private int remainingEffort;

    private DateTime previous;

    public SimpleEffortTracker(int effort)
    {
        maxEffort = effort;
        remainingEffort = maxEffort;
    }

    @Override
    public int getTodaysEffort(DateTime now)
    {
        if (previous == null) previous = now;

        if (!isSameDay(previous, now))
        {
            previous = now;
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

    private boolean isSameDay(DateTime d1, DateTime d2)
    {
        return d1 != null && d2 != null
            && d1.getYear() == d2.getYear()
            && d1.getMonthOfYear() == d2.getMonthOfYear()
            && d1.getDayOfMonth() == d2.getDayOfMonth();
    }
}
