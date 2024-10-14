package stoneframe.chorelist.model.limiters;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import stoneframe.chorelist.model.timeservices.TimeService;

public class LimiterManager
{
    private final LimiterContainer container;

    private final TimeService timeService;

    public LimiterManager(LimiterContainer container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public List<Limiter> getLimiters()
    {
        return container.limiters.stream()
            .sorted(Comparator.comparing(Limiter::getName))
            .collect(Collectors.toList());
    }

    public LimiterEditor getLimiterEditor(Limiter limiter)
    {
        return new LimiterEditor(this, limiter, timeService);
    }

    public Limiter createLimiter(String name)
    {
        Limiter limiter = new Limiter(name, timeService.getToday(), 300, true);

        container.limiters.add(limiter);

        return limiter;
    }

    public void removeLimiter(Limiter limiter)
    {
        container.limiters.remove(limiter);
    }
}
