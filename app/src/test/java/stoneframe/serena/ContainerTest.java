package stoneframe.serena;

import org.junit.Test;

import stoneframe.serena.model.chores.ChoreManager;
import stoneframe.serena.model.Container;
import stoneframe.serena.model.routines.RoutineManager;
import stoneframe.serena.model.tasks.TaskManager;
import stoneframe.serena.model.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.model.chores.efforttrackers.WeeklyEffortTracker;

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
