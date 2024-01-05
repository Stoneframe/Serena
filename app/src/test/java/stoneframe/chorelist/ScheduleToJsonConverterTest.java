package stoneframe.chorelist;

import org.junit.Test;

import stoneframe.chorelist.json.SimpleEffortTrackerConverter;
import stoneframe.chorelist.json.SimpleTaskSelectorConverter;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleEffortTracker;
import stoneframe.chorelist.model.SimpleTaskSelector;
import stoneframe.chorelist.json.ScheduleToJsonConverter;
import stoneframe.chorelist.model.Task;

import static junit.framework.Assert.assertEquals;

public class ScheduleToJsonConverterTest
{
    @Test
    public void convert_ToDoList_to_json_and_back_to_TodoList()
    {
        Schedule schedule1 = new Schedule(new SimpleEffortTracker(20), new SimpleTaskSelector());

        schedule1.addTask(new Task("Test1", 1, 10, TestUtils.createDateTime(2017, 2, 9), Task.DAILY, 3));
        schedule1.addTask(new Task("Test2", 3, 2, TestUtils.createDateTime(2017, 2, 9), Task.DAILY, 1));

        String json = ScheduleToJsonConverter.convertToJson(schedule1);

        Schedule schedule2 = ScheduleToJsonConverter.convertFromJson(json, new SimpleEffortTrackerConverter(), new SimpleTaskSelectorConverter());

        assertEquals(schedule1, schedule2);
    }
}
