package stoneframe.serena.mocks;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.List;

import stoneframe.serena.model.Serena;
import stoneframe.serena.model.chores.Chore;
import stoneframe.serena.model.chores.ChoreManager;
import stoneframe.serena.model.chores.ChoreSelector;
import stoneframe.serena.model.chores.EffortTracker;
import stoneframe.serena.model.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.model.routines.RoutineManager;

public class TestContext
{
    private static final LocalDateTime NOW = new LocalDateTime(2024, 1, 1, 0 ,0);

    private final MockTimeService timeService;

    private final EffortTrackerWrapper effortTracker;
    private final ChoreSelectorWrapper choreSelector;

    private final Serena serena;

    public TestContext()
    {
        effortTracker = new EffortTrackerWrapper();
        choreSelector = new ChoreSelectorWrapper();

        timeService = new MockTimeService(NOW);

        serena = new Serena(
            new MockStorage(effortTracker, choreSelector),
            timeService,
            effortTracker,
            choreSelector);

        serena.load();
    }

    public TestContext setCurrentTime(LocalDateTime now)
    {
        timeService.setNow(now);

        return this;
    }

    public TestContext setCurrentTime(LocalDate today)
    {
        timeService.setNow(today.toLocalDateTime(LocalTime.MIDNIGHT));

        return this;
    }

    public void setEffortTracker(EffortTracker effortTracker)
    {
        this.effortTracker.set(effortTracker);
    }

    public void setChoreSelector(ChoreSelector choreSelector)
    {
        this.choreSelector.set(choreSelector);
    }

    public ChoreManager getChoreManager()
    {
        return serena.getChoreManager();
    }

    public RoutineManager getRoutineManager()
    {
        return serena.getRoutineManager();
    }

    private static class EffortTrackerWrapper implements EffortTracker
    {
        private EffortTracker internalEffortTracker;

        public EffortTrackerWrapper()
        {
            internalEffortTracker = new MockEffortTracker();
        }

        @Override
        public int getTodaysEffort(LocalDate today)
        {
            return internalEffortTracker.getTodaysEffort(today);
        }

        @Override
        public void spend(int effort)
        {
            internalEffortTracker.spend(effort);
        }

        @Override
        public void reset(LocalDate today)
        {
            internalEffortTracker.reset(today);
        }

        public EffortTracker get()
        {
            return internalEffortTracker;
        }

        public void set(EffortTracker internalEffortTracker)
        {
            this.internalEffortTracker = internalEffortTracker;
        }
    }

    private static class ChoreSelectorWrapper implements ChoreSelector
    {
        private ChoreSelector internalChoreSelector;

        public ChoreSelectorWrapper()
        {
            internalChoreSelector = new SimpleChoreSelector();
        }

        @Override
        public List<Chore> selectChores(List<Chore> chores, int effort)
        {
            return internalChoreSelector.selectChores(chores, effort);
        }

        public ChoreSelector get()
        {
            return internalChoreSelector;
        }

        public void set(ChoreSelector internalChoreSelector)
        {
            this.internalChoreSelector = internalChoreSelector;
        }
    }
}
