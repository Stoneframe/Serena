package stoneframe.serena.mocks;

import org.joda.time.LocalDate;

import stoneframe.serena.chores.EffortTracker;

public class MockEffortTracker implements EffortTracker
{
    @Override
    public int getTodaysEffort(LocalDate today)
    {
        return 10;
    }

    @Override
    public void spend(int effort)
    {

    }

    @Override
    public void reset(LocalDate today)
    {

    }
}
