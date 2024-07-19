package stoneframe.chorelist.model.limiters;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class LimiterManager
{
    private final List<Limiter> limiters = new LinkedList<>();

    public List<Limiter> getLimiters()
    {
        return limiters.stream()
            .sorted(Comparator.comparing(Limiter::getName))
            .collect(Collectors.toList());
    }

    public void addLimiter(Limiter limiter)
    {
        limiters.add(limiter);
    }

    public void removeLimiter(Limiter limiter)
    {
        limiters.remove(limiter);
    }
}
