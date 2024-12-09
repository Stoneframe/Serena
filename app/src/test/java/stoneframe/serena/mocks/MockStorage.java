package stoneframe.serena.mocks;

import androidx.annotation.Nullable;

import stoneframe.serena.model.Container;
import stoneframe.serena.model.Storage;
import stoneframe.serena.model.balancers.BalancerContainer;
import stoneframe.serena.model.chores.ChoreContainer;
import stoneframe.serena.model.chores.ChoreSelector;
import stoneframe.serena.model.chores.EffortTracker;
import stoneframe.serena.model.routines.RoutineContainer;

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
