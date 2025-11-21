package stoneframe.serena.balancers;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import stoneframe.serena.timeservices.TimeService;

public class BalancerManager
{
    private final Supplier<BalancerContainer> container;

    private final TimeService timeService;

    public BalancerManager(Supplier<BalancerContainer> container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public List<Balancer> getBalancers()
    {
        return getContainer().balancers.stream()
            .sorted(Comparator.comparing(Balancer::getName))
            .collect(Collectors.toList());
    }

    public boolean isAboveThreshold()
    {
        return getContainer().balancers.stream()
            .filter(Balancer::isEnabled)
            .filter(b -> b.getType() != Balancer.COUNTER)
            .allMatch(b -> b.isAboveThreshold(timeService.getNow()));
    }

    public BalancerEditor getBalancerEditor(Balancer balancer)
    {
        return new BalancerEditor(this, balancer, timeService);
    }

    public Balancer createBalancer(String name)
    {
        Balancer balancer = new Balancer(name, timeService.getToday(), 0, true);

        getContainer().balancers.add(balancer);

        return balancer;
    }

    public void removeBalancer(Balancer balancer)
    {
        getContainer().balancers.remove(balancer);
    }

    private BalancerContainer getContainer()
    {
        return container.get();
    }
}
