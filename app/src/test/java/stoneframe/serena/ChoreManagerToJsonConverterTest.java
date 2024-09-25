package stoneframe.serena;

import static junit.framework.Assert.assertEquals;

import org.junit.Test;

import stoneframe.serena.json.ChoreManagerToJsonConverter;
import stoneframe.serena.json.SimpleChoreSelectorConverter;
import stoneframe.serena.json.SimpleEffortTrackerConverter;
import stoneframe.serena.model.chores.Chore;
import stoneframe.serena.model.chores.ChoreManager;
import stoneframe.serena.model.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.model.chores.efforttrackers.SimpleEffortTracker;

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
