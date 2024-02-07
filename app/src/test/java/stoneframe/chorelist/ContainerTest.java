package stoneframe.chorelist;

import org.joda.time.LocalTime;
import org.junit.Test;

import stoneframe.chorelist.json.ContainerJsonConverter;
import stoneframe.chorelist.model.ChoreManager;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.Procedure;
import stoneframe.chorelist.model.Routine;
import stoneframe.chorelist.model.RoutineManager;
import stoneframe.chorelist.model.TaskManager;
import stoneframe.chorelist.model.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.efforttrackers.WeeklyEffortTracker;
import stoneframe.chorelist.model.timeservices.RealTimeService;

public class ContainerTest
{
    @Test
    public void defaultContainer()
    {
        Container container = new Container();

        WeeklyEffortTracker effortTracker = new WeeklyEffortTracker(
            10,
            10,
            10,
            10,
            10,
            15,
            15);

        container.ChoreManager = new ChoreManager(effortTracker, new SimpleChoreSelector());
        container.TaskManager = new TaskManager();
        container.RoutineManager = new RoutineManager();

//        Routine routine = new Routine("Routine", LocalTime.now());
//
//        Procedure procedure = new Procedure("Procedure", new LocalTime(10, 0));
//
//        routine.addProcedure(procedure);
//
//        String json = ContainerJsonConverter.toJson(container);
    }
}
