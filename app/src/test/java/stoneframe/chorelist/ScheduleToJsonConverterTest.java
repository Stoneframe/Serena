package stoneframe.chorelist;

import org.junit.Test;

import stoneframe.chorelist.json.SimpleEffortTrackerConverter;
import stoneframe.chorelist.json.SimpleChoreSelectorConverter;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleEffortTracker;
import stoneframe.chorelist.model.SimpleChoreSelector;
import stoneframe.chorelist.json.ScheduleToJsonConverter;
import stoneframe.chorelist.model.Chore;

import static junit.framework.Assert.assertEquals;

public class ScheduleToJsonConverterTest
{
    @Test
    public void convert_ToDoList_to_json_and_back_to_TodoList()
    {
        Schedule schedule1 = new Schedule(new SimpleEffortTracker(20), new SimpleChoreSelector());

        schedule1.addChore(new Chore("Test1", 1, 10, TestUtils.createDateTime(2017, 2, 9), Chore.DAILY, 3));
        schedule1.addChore(new Chore("Test2", 3, 2, TestUtils.createDateTime(2017, 2, 9), Chore.DAILY, 1));

        String json = ScheduleToJsonConverter.convertToJson(schedule1);

        Schedule schedule2 = ScheduleToJsonConverter.convertFromJson(json, new SimpleEffortTrackerConverter(), new SimpleChoreSelectorConverter());

        assertEquals(schedule1, schedule2);
    }
}
