package stoneframe.chorelist;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleEffortTracker;
import stoneframe.chorelist.model.SimpleTaskSelector;
import stoneframe.chorelist.model.Task;

import static org.junit.Assert.*;

public class ScheduleTest
{
    @Test
    public void two_tasks_scheduled_for_before_now()
    {
        Task t1 = new Task("Task1", 5, 5, TestUtils.MOCK_NOW.minusDays(2), Task.DAILY, 1);
        Task t2 = new Task("Task2", 3, 8, TestUtils.MOCK_NOW.minusDays(1), Task.DAILY, 1);

        Schedule schedule = new Schedule(new SimpleEffortTracker(15), new SimpleTaskSelector());

        schedule.addTask(t1);
        schedule.addTask(t2);

        List<Task> expected = new LinkedList<>();
        expected.add(t2);
        expected.add(t1);

        List<Task> actual = schedule.getTasks(TestUtils.MOCK_NOW);

        assertEquals(expected, actual);
    }
}
