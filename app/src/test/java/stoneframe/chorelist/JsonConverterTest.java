package stoneframe.chorelist;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import stoneframe.chorelist.json.ContainerJsonConverter;
import stoneframe.chorelist.model.Container;
import stoneframe.chorelist.model.Storage;
import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.efforttrackers.WeeklyEffortTracker;
import stoneframe.chorelist.model.timeservices.RealTimeService;

public class JsonConverterTest
{
    private String json = "{\"ChoreManager\":{\"effortTracker\":{\"mon\":10,\"tue\":10,\"wed\":10,\"thu\":10,\"fri\":10,\"sat\":10,\"sun\":10,\"remaining\":0},\"choreSelector\":{},\"chores\":[]},\"TaskManager\":{\"tasks\":[{\"description\":\"Test Task\",\"deadline\":\"2024-02-25T00:00:00.000+01:00\",\"ignoreBefore\":\"2024-02-24T00:00:00.000+01:00\",\"isDone\":false}]},\"RoutineManager\":{\"routines\":[]}}";

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
            "Test Task",
            new DateTime(2024, 2, 25, 0, 0),
            new DateTime(2024, 2, 24, 0, 0));

        choreList.addTask(task1);

        choreList.save();

        assertNotNull(json);
    }

    @Test
    public void testVersion0ToVersion1() throws JSONException
    {
        JSONObject jsonObject = new JSONObject(json);

        assertFalse(jsonObject.has("Version"));
    }
}
