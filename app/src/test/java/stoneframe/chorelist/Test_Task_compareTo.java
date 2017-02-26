package stoneframe.chorelist;

import org.junit.Test;

import stoneframe.chorelist.model.Task;

import static org.junit.Assert.assertEquals;

public class Test_Task_compareTo {

    @Test
    public void test_compare_date_1() {
        Task t1 = new Task(TestUtils.createDateTime(2017, 2, 9), "Task1", 1, 10);
        Task t2 = new Task(TestUtils.createDateTime(2017, 2, 7), "Task2", 1, 10);

        int expected = 1;
        int actual = t1.compareTo(t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_date_2() {
        Task t1 = new Task(TestUtils.createDateTime(2017, 2, 14), "Task1", 1, 10);
        Task t2 = new Task(TestUtils.createDateTime(2017, 2, 21), "Task2", 1, 10);

        int expected = -1;
        int actual = t1.compareTo(t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_priority_1() {
        Task t1 = new Task(TestUtils.createDateTime(2017, 2, 14), "Task1", 2, 10);
        Task t2 = new Task(TestUtils.createDateTime(2017, 2, 14), "Task2", 6, 10);

        int expected = -1;
        int actual = t1.compareTo(t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_priority_2() {
        Task t1 = new Task(TestUtils.createDateTime(2017, 2, 14), "Task1", 8, 10);
        Task t2 = new Task(TestUtils.createDateTime(2017, 2, 14), "Task2", 1, 10);

        int expected = 1;
        int actual = t1.compareTo(t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_effort_1() {
        Task t1 = new Task(TestUtils.createDateTime(2017, 2, 14), "Task1", 2, 3);
        Task t2 = new Task(TestUtils.createDateTime(2017, 2, 14), "Task2", 2, 5);

        int expected = -1;
        int actual = t1.compareTo(t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_effort_2() {
        Task t1 = new Task(TestUtils.createDateTime(2017, 2, 14), "Task1", 2, 60);
        Task t2 = new Task(TestUtils.createDateTime(2017, 2, 14), "Task2", 2, 25);

        int expected = 1;
        int actual = t1.compareTo(t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_effort_3() {
        Task t1 = new Task(TestUtils.createDateTime(2017, 2, 14), "Task", 2, 30);
        Task t2 = new Task(TestUtils.createDateTime(2017, 2, 14), "Task", 2, 30);

        int expected = 0;
        int actual = t1.compareTo(t2);

        assertEquals(expected, actual);
    }

}
