package stoneframe.chorelist;

import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import stoneframe.chorelist.model.Duty;
import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleEffortTracker;
import stoneframe.chorelist.model.SimpleTaskSelector;
import stoneframe.chorelist.model.Task;
import stoneframe.chorelist.model.ToDoList;

import static junit.framework.Assert.assertEquals;

public class Test_ToDoList_getTasks {

    private ToDoList todoList;

    @Before
    public void setUp() {
        Schedule schedule = new Schedule();

        schedule.addDuty(new Duty("Task", 1, 10, TestUtils.createDateTime(2017, 2, 8), Duty.DAILY, 3));

        todoList = new ToDoList(schedule, new SimpleTaskSelector(), new SimpleEffortTracker(100));
    }

    @Test
    public void list_single_task_same_day() {
        List<Task> expected = new LinkedList<>();
        expected.add(new Task(TestUtils.createDateTime(2017, 2, 8), "Task", 1, 10));

        List<Task> actual = todoList.getTasks(TestUtils.createDateTime(2017, 2, 8));

        assertEquals(1, actual.size());
        assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void list_no_task_day_before() {
        List<Task> list = todoList.getTasks(TestUtils.createDateTime(2017, 2, 7));

        assertEquals(0, list.size());
    }

    @Test
    public void list_single_task_day_after() {
        List<Task> expected = new LinkedList<>();
        expected.add(new Task(TestUtils.createDateTime(2017, 2, 8), "Task", 1, 10));

        List<Task> actual = todoList.getTasks(TestUtils.createDateTime(2017, 2, 9));

        assertEquals(1, actual.size());
        assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void list_same_task_twice_same_day() {
        todoList.getTasks(TestUtils.createDateTime(2017, 2, 8));

        List<Task> expected = new LinkedList<>();
        expected.add(new Task(TestUtils.createDateTime(2017, 2, 8), "Task", 1, 10));

        List<Task> actual = todoList.getTasks(TestUtils.createDateTime(2017, 2, 8));

        assertEquals(1, actual.size());
        assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void compete_task_then_list_no_tasks_same_day() {
        List<Task> list = todoList.getTasks(TestUtils.createDateTime(2017, 2, 8));
        todoList.complete(list.get(0));

        List<Task> actual = todoList.getTasks(TestUtils.createDateTime(2017, 2, 8));

        assertEquals(0, actual.size());
    }

    @Test
    public void compete_task_then_list_single_task_three_days_later() {
        List<Task> list = todoList.getTasks(TestUtils.createDateTime(2017, 2, 8));
        todoList.complete(list.get(0));

        List<Task> expected = new LinkedList<>();
        expected.add(new Task(TestUtils.createDateTime(2017, 2, 11), "Task", 1, 10));

        List<Task> actual = todoList.getTasks(TestUtils.createDateTime(2017, 2, 11));

        assertEquals(1, actual.size());
        assertEquals(expected.get(0), actual.get(0));
    }

    @Test
    public void test_no_dublicate_tasks_when_getting_tasks_at_a_later_time_without_completing() {
        todoList.getTasks(TestUtils.createDateTime(2017, 2, 8));
        List<Task> list = todoList.getTasks(TestUtils.createDateTime(2017, 2, 12));

        assertEquals(1, list.size());
    }

}
