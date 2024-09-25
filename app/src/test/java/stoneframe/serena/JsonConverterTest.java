package stoneframe.serena;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.Nullable;

import org.joda.time.LocalDate;
import org.json.JSONException;
import org.junit.Ignore;
import org.junit.Test;

import java.util.List;

import stoneframe.serena.json.ContainerJsonConverter;
import stoneframe.serena.json.SimpleChoreSelectorConverter;
import stoneframe.serena.json.WeeklyEffortTrackerConverter;
import stoneframe.serena.model.Serena;
import stoneframe.serena.model.Container;
import stoneframe.serena.model.Storage;
import stoneframe.serena.model.tasks.Task;
import stoneframe.serena.model.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.model.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.serena.model.storages.JsonConverter;
import stoneframe.serena.model.timeservices.RealTimeService;

public class JsonConverterTest
{
    private String json;

    private final JsonConverter jsonConverter = new JsonConverter(
        new SimpleChoreSelectorConverter(),
        new WeeklyEffortTrackerConverter());

    @Test
    @Ignore
    public void test()
    {
        Serena serena = new Serena(
            new Storage()
            {
                @Nullable
                @Override
                public Container load()
                {
                    return null;
                }

                @Override
                public void save(Container container)
                {
                    json = ContainerJsonConverter.toJson(container);
                }

                @Override
                public int getCurrentVersion()
                {
                    return 0;
                }
            },
            new RealTimeService(),
            new WeeklyEffortTracker(10, 10, 10, 10, 10, 10, 10),
            new SimpleChoreSelector());

        serena.load();

        Task task1 = new Task(
            "Uncompleted Task",
            new LocalDate(2024, 2, 25),
            new LocalDate(2024, 2, 24));

        Task task2 = new Task(
            "Completed Task",
            new LocalDate(2024, 2, 25),
            new LocalDate(2024, 2, 24));

        serena.taskDone(task2);

        serena.addTask(task1);
        serena.addTask(task2);

        serena.save();

        assertNotNull(json);
    }

    @Test
    @Ignore("No long relevant?")
    public void testVersion0ToVersion1() throws JSONException
    {
        String version0Json = "{\"ChoreManager\":{\"effortTracker\":{\"mon\":10,\"tue\":10,\"wed\":10,\"thu\":10,\"fri\":10,\"sat\":10,\"sun\":10,\"remaining\":0},\"choreSelector\":{},\"chores\":[]},\"TaskManager\":{\"tasks\":[{\"description\":\"Uncompleted Task\",\"deadline\":\"2024-02-25T00:00:00.000+01:00\",\"ignoreBefore\":\"2024-02-24T00:00:00.000+01:00\",\"isDone\":false},{\"description\":\"Completed Task\",\"deadline\":\"2024-02-25T00:00:00.000+01:00\",\"ignoreBefore\":\"2024-02-24T00:00:00.000+01:00\",\"completed\":\"2024-02-23T23:09:58.416+01:00\",\"isDone\":true}]},\"RoutineManager\":{\"routines\":[]}}";

        Container container = jsonConverter.fromJson(version0Json);

        List<Task> allTasks = container.TaskManager.getAllTasks(true);

        assertFalse(allTasks.get(0).isDone());
        assertNull(allTasks.get(0).getCompleted());

        assertTrue(allTasks.get(1).isDone());
        assertNotNull(allTasks.get(1).getCompleted());
    }
}
