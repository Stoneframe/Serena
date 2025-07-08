package stoneframe.serena.sleep;

import org.joda.time.LocalDateTime;

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

    public boolean isEnabled()
    {
        return container.get().sleep.isEnabled();
    }

    public void setEnabled(boolean isEnabled)
    {
        container.get().sleep.setEnabled(isEnabled);
    }

    public int getState()
    {
        return container.get().sleep.getState();
    }

    public boolean isAhead()
    {
        return container.get().sleep.getPercent(timeService.getNow()) >= 60;
    }

    public int getPercent()
    {
        return container.get().sleep.getPercent(timeService.getNow());
    }

    public void toggle()
    {
        container.get().sleep.toggle(timeService.getNow());
    }

    public Sleep.SleepSession getPreviousSession()
    {
        return container.get().sleep.getPreviousSession();
    }

    public void addSession(LocalDateTime start, LocalDateTime end)
    {
        container.get().sleep.addSession(start, end);
    }
}
