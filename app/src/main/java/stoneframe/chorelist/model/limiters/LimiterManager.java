package stoneframe.chorelist.model.limiters;

import org.joda.time.LocalDate;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import stoneframe.chorelist.model.timeservices.TimeService;

public class LimiterManager
{
    private final List<Limiter> limiters = new LinkedList<>();

    public List<Limiter> getLimiters()
    {
        return limiters.stream()
            .sorted(Comparator.comparing(Limiter::getName))
            .collect(Collectors.toList());
    }

    public Limiter createLimiter(String name, LocalDate today)
    {
        Limiter limiter = new Limiter(name, today, 300, true);

        limiters.add(limiter);

        return limiter;
    }

    public LimiterEditor getEditor(Limiter limiter, TimeService timeService)
    {
        return new LimiterEditor(this, limiter, timeService);
    }

    public void removeLimiter(Limiter limiter)
    {
        limiters.remove(limiter);
    }
}
