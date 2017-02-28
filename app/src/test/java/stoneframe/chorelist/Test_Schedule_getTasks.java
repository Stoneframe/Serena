package stoneframe.chorelist;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleEffortTracker;
import stoneframe.chorelist.model.SimpleTaskSelector;
import stoneframe.chorelist.model.Task;

import static org.junit.Assert.*;

public class Test_Schedule_getTasks {

    @Test
    public void two_tasks_scheduled_for_before_now() {
        Task t1 = new Task("Task1", 5, 5, TestUtils.MOCK_NOW.minusDays(2), Task.DAILY, 1);
        Task t2 = new Task("Task2", 3, 8, TestUtils.MOCK_NOW.minusDays(1), Task.DAILY, 1);

        Schedule schedule = new Schedule(new SimpleEffortTracker(15), new SimpleTaskSelector());

        schedule.addTask(t1);
        schedule.addTask(t2);

        List<Task> expected = new LinkedList<>();
        expected.add(t1);
        expected.add(t1);

        List<Task> actual = schedule.getTasks(TestUtils.MOCK_NOW);

        assertEquals(expected, actual);
    }

//    @Test
//    public void add_two_duties_list_single_task() {
//        Duty d1 = new Duty("", 1, 1, TestUtils.MOCK_NOW, Duty.DAILY, 1);
//        Duty d2 = new Duty("", 1, 1, TestUtils.MOCK_NOW.plusDays(3), Duty.DAILY, 3);
//
//        Schedule schedule = new Schedule();
//
//        schedule.addTask(d1);
//        schedule.addTask(d2);
//
//        List<Task> exptected = new LinkedList<Task>();
//        exptected.add(new Task(TestUtils.MOCK_NOW, "", 1, 1));
//
//        List<Task> actual = schedule.getTasks(TestUtils.MOCK_NOW);
//
//        assertEquals(exptected.get(0), actual.get(0));
//    }
//
//    @Test
//    public void add_two_duties_list_two_tasks() {
//        Schedule schedule = new Schedule();
//
//        schedule.addTask(new Duty("Duty1", 1, 1, TestUtils.MOCK_NOW, Duty.DAILY, 1));
//        schedule.addTask(new Duty("Duty2", 1, 1, TestUtils.MOCK_NOW.plusDays(3), Duty.DAILY, 3));
//
//        List<Task> exptected = new LinkedList<Task>();
//        exptected.add(new Task(TestUtils.createDateTime(2017, 2, 5), "Duty1", 1, 1));
//        exptected.add(new Task(TestUtils.createDateTime(2017, 2, 8), "Duty2", 1, 1));
//
//        List<Task> actual = schedule.getTasks(TestUtils.MOCK_NOW.plusDays(3));
//
//        assertEquals(exptected.get(0), actual.get(0));
//        assertEquals(exptected.get(1), actual.get(1));
//    }

}
