package stoneframe.serena.mocks;

import androidx.annotation.Nullable;

import stoneframe.serena.Container;
import stoneframe.serena.Storage;
import stoneframe.serena.balancers.BalancerContainer;
import stoneframe.serena.chores.ChoreContainer;
import stoneframe.serena.chores.ChoreSelector;
import stoneframe.serena.chores.EffortTracker;
import stoneframe.serena.routines.RoutineContainer;

public class MockStorage implements Storage
{
    private final Container container;

    public MockStorage(EffortTracker effortTracker, ChoreSelector choreSelector)
    {
        container = new Container();
        container.ChoreContainer = new ChoreContainer(effortTracker, choreSelector);
        container.RoutineContainer = new RoutineContainer();
        container.BalancerContainer = new BalancerContainer();
    }

    @Nullable
    @Override
    public Container load()
    {
        return container;
    }

    @Override
    public void save(@Nullable Container container)
    {

    }

    @Override
    public int getCurrentVersion()
    {
        return 0;
    }
}
