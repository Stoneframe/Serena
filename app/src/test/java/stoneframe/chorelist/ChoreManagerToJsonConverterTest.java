package stoneframe.chorelist;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import stoneframe.chorelist.json.ChoreManagerToJsonConverter;
import stoneframe.chorelist.json.SimpleChoreSelectorConverter;
import stoneframe.chorelist.json.SimpleEffortTrackerConverter;
import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.ChoreManager;
import stoneframe.chorelist.model.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.efforttrackers.SimpleEffortTracker;

public class ChoreManagerToJsonConverterTest
{
    @Test
    public void convert_ToDoList_to_json_and_back_to_TodoList()
    {
        ChoreManager choreManager1 = new ChoreManager(
            new SimpleEffortTracker(20),
            new SimpleChoreSelector());

        choreManager1.addChore(new Chore("Test1", 1, 10, TestUtils.createDate(2017, 2, 9),
            3,
            Chore.DAYS));
        choreManager1.addChore(new Chore("Test2", 3, 2, TestUtils.createDate(2017, 2, 9),
            1,
            Chore.DAYS));

        String json = ChoreManagerToJsonConverter.convertToJson(choreManager1);

        ChoreManager choreManager2 = ChoreManagerToJsonConverter.convertFromJson(
            json,
            new SimpleEffortTrackerConverter(),
            new SimpleChoreSelectorConverter());

        assertEquals(choreManager1, choreManager2);
    }
}
