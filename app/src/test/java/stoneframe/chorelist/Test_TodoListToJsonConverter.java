package stoneframe.chorelist;

import org.junit.Test;

import stoneframe.chorelist.json.SimpleEffortTrackerConverter;
import stoneframe.chorelist.json.SimpleTaskSelectorConverter;
import stoneframe.chorelist.model.Duty;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleEffortTracker;
import stoneframe.chorelist.model.SimpleTaskSelector;
import stoneframe.chorelist.model.ToDoList;
import stoneframe.chorelist.json.TodoListToJsonConverter;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

public class Test_TodoListToJsonConverter {

    @Test
    public void convert_ToDoList_to_json_and_back_to_TodoList() {
        Schedule schedule = new Schedule();
        schedule.addDuty(new Duty("Test1", 1, 10, TestUtils.createDateTime(2017, 2, 9), Duty.DAILY, 3));
        schedule.addDuty(new Duty("Test2", 3, 2, TestUtils.createDateTime(2017, 2, 9), Duty.DAILY, 1));

        ToDoList todoList1 = new ToDoList(schedule, new SimpleTaskSelector(), new SimpleEffortTracker(20));
        todoList1.getTasks(TestUtils.createDateTime(2017, 2, 9));

        String json = TodoListToJsonConverter.convertToJson(todoList1);

        ToDoList todoList2 = TodoListToJsonConverter.convertFromJson(json,
                new SimpleTaskSelectorConverter(), new SimpleEffortTrackerConverter());

        assertEquals(todoList1, todoList2);
    }

}
