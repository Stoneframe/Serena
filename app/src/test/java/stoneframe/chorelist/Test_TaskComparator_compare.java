package stoneframe.chorelist;

import org.junit.Before;
import org.junit.Test;

import stoneframe.chorelist.model.Task;

import static org.junit.Assert.*;

public class Test_TaskComparator_compare {

    private Task.DutyComparator comparator;

    @Before
    public void setUp() {
        comparator = new Task.DutyComparator(TestUtils.MOCK_NOW);
    }

    @Test
    public void task_scheduled_before_now_and_task_scheduled_after_now() {
        Task t1 = new Task("Task1", 3, 10, TestUtils.MOCK_NOW.minusDays(1) ,Task.DAILY, 3);
        Task t2 = new Task("Task2", 1, 10, TestUtils.MOCK_NOW.plusDays(1), Task.DAILY, 6);

        int expected = -1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void two_tasks_scheduled_after_now() {
        Task t1 = new Task("Task1", 3, 10, TestUtils.MOCK_NOW.plusDays(2) ,Task.DAILY, 3);
        Task t2 = new Task("Task2", 6, 10, TestUtils.MOCK_NOW.plusDays(1), Task.DAILY, 6);

        int expected = 1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_date_1() {
        Task t1 = new Task("Task1", 1, 10, TestUtils.MOCK_NOW.plusDays(1), Task.DAILY, 5);
        Task t2 = new Task("Task2", 1, 10, TestUtils.MOCK_NOW, Task.DAILY, 2);

        int expected = 1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_date_2() {
        Task t1 = new Task("Task1", 1, 10, TestUtils.MOCK_NOW ,Task.DAILY, 3);
        Task t2 = new Task("Task2", 1, 10, TestUtils.MOCK_NOW.plusWeeks(2), Task.DAILY, 6);

        int expected = -1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_priority_1() {
        Task t1 = new Task("Task1", 2, 10, TestUtils.MOCK_NOW, Task.DAILY, 3);
        Task t2 = new Task("Task2", 6, 10, TestUtils.MOCK_NOW, Task.DAILY, 3);

        int expected = -1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_priority_2() {
        Task t1 = new Task("Task1", 8, 10, TestUtils.MOCK_NOW, Task.DAILY, 3);
        Task t2 = new Task("Task2", 1, 10, TestUtils.MOCK_NOW, Task.DAILY, 3);

        int expected = 1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_effort_1() {
        Task t1 = new Task("Task1", 2, 3, TestUtils.MOCK_NOW, Task.DAILY, 3);
        Task t2 = new Task("Task2", 2, 5, TestUtils.MOCK_NOW, Task.DAILY, 3);

        int expected = -1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_effort_2() {
        Task t1 = new Task("Task1", 2, 60, TestUtils.MOCK_NOW, Task.DAILY, 3);
        Task t2 = new Task("Task2", 2, 25, TestUtils.MOCK_NOW, Task.DAILY, 3);

        int expected = 1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

}
