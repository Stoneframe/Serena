package stoneframe.chorelist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import stoneframe.chorelist.json.ContainerJsonConverter;
import stoneframe.chorelist.json.SimpleChoreSelectorConverter;
import stoneframe.chorelist.json.WeeklyEffortTrackerConverter;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.Storage;
import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.efforttrackers.WeeklyEffortTracker;
import stoneframe.chorelist.model.storages.JsonConverter;
import stoneframe.chorelist.model.timeservices.RealTimeService;

public class JsonConverterTest
{
    private String json;

    private JsonConverter jsonConverter = new JsonConverter(
        new SimpleChoreSelectorConverter(),
        new WeeklyEffortTrackerConverter());

    @Test
    @Ignore
    public void test()
    {
        ChoreList choreList = new ChoreList(
            new Storage()
            {
                @Nullable
                @Override
                public Container load()
                {
                    return null;
                }

                @Override
                public void save(@NonNull Container container)
                {
                    json = ContainerJsonConverter.toJson(container);
                }
            },
            new RealTimeService(),
            new WeeklyEffortTracker(10, 10, 10, 10, 10, 10, 10),
            new SimpleChoreSelector());

        choreList.load();

        Task task1 = new Task(
            "Uncompleted Task",
            new DateTime(2024, 2, 25, 0, 0),
            new DateTime(2024, 2, 24, 0, 0));

        Task task2 = new Task(
            "Completed Task",
            new DateTime(2024, 2, 25, 0, 0),
            new DateTime(2024, 2, 24, 0, 0));

        choreList.taskDone(task2);

        choreList.addTask(task1);
        choreList.addTask(task2);

        choreList.save();

        assertNotNull(json);
    }

    @Test
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
