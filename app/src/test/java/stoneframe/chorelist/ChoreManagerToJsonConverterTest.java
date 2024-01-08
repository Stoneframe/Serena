package stoneframe.chorelist;

import org.junit.Test;

import stoneframe.chorelist.json.SimpleEffortTrackerConverter;
import stoneframe.chorelist.json.SimpleChoreSelectorConverter;
import stoneframe.chorelist.model.ChoreManager;
import stoneframe.chorelist.model.SimpleEffortTracker;
import stoneframe.chorelist.model.SimpleChoreSelector;
import stoneframe.chorelist.json.ScheduleToJsonConverter;
import stoneframe.chorelist.model.Chore;

import static junit.framework.Assert.assertEquals;

public class ChoreManagerToJsonConverterTest
{
    @Test
    public void convert_ToDoList_to_json_and_back_to_TodoList()
    {
        ChoreManager choreManager1 = new ChoreManager(new SimpleEffortTracker(20), new SimpleChoreSelector());

        choreManager1.addChore(new Chore("Test1", 1, 10, TestUtils.createDateTime(2017, 2, 9),
            3,
            Chore.DAYS));
        choreManager1.addChore(new Chore("Test2", 3, 2, TestUtils.createDateTime(2017, 2, 9),
            1,
            Chore.DAYS));

        String json = ScheduleToJsonConverter.convertToJson(choreManager1);

        ChoreManager choreManager2 = ScheduleToJsonConverter.convertFromJson(json, new SimpleEffortTrackerConverter(), new SimpleChoreSelectorConverter());

        assertEquals(choreManager1, choreManager2);
    }
}
