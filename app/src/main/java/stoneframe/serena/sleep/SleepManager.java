package stoneframe.serena.sleep;

import java.util.function.Supplier;

import stoneframe.serena.timeservices.TimeService;

public class SleepManager
{
    private final Supplier<SleepContainer> container;

    private final TimeService timeService;

    public SleepManager(Supplier<SleepContainer> container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public int getState()
    {
        return container.get().sleep.getState();
    }

    public boolean isOnTrack()
    {
        return container.get().sleep.isOnTrack(timeService.getNow());
    }

    public int getPercent()
    {
        return container.get().sleep.getPercent(timeService.getNow());
    }

    public int getPoints()
    {
        return container.get().sleep.getPoints(timeService.getNow());
    }

    public void toggle()
    {
        container.get().sleep.toggle(timeService.getNow());
    }
}
