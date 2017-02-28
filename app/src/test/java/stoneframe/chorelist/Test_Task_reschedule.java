package stoneframe.chorelist;

import org.joda.time.DateTime;
import org.junit.Test;

import stoneframe.chorelist.model.Task;

import static org.junit.Assert.*;

public class Test_Task_reschedule {

    @Test
    public void test_daliy_freq_3() {
        Task task = new Task("", 0, 0, TestUtils.createDateTime(2017, 2, 4), Task.DAILY, 3);

        task.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 7);
        DateTime actual = task.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_daliy_freq_5() {
        Task task = new Task("", 0, 0, TestUtils.createDateTime(2017, 2, 4), Task.DAILY, 5);

        task.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 9);
        DateTime actual = task.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_daily_freq_5_previous_one_week_ago() {
        Task task = new Task("", 0, 0, TestUtils.createDateTime(2017, 1, 29), Task.DAILY, 5);

        task.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 8);
        DateTime actual = task.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_weekly_freq_1_day_3() {
        Task task = new Task("", 0, 0, TestUtils.createDateTime(2017, 2, 1), Task.WEEKLY, 1);

        task.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 8);
        DateTime actual = task.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_weekly_freq_4_day_7() {
        Task task = new Task("", 0, 0, TestUtils.createDateTime(2017, 1, 29), Task.WEEKLY, 4);

        task.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 26);
        DateTime actual = task.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_monthly_freq_2_day_5() {
        Task task = new Task("", 0, 0, TestUtils.createDateTime(2017, 1, 5), Task.MONTHLY, 2);

        task.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 3, 5);
        DateTime actual = task.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_monthly_freq_3_day_23() {
        Task task = new Task("", 0, 0, TestUtils.createDateTime(2017, 1, 23), Task.MONTHLY, 3);

        task.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 4, 23);
        DateTime actual = task.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_yearly_freq_2_day_253() {
        Task task = new Task("", 0, 0, TestUtils.createDateTime(2016, 9, 9), Task.YEARLY, 2);

        task.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2018, 9, 9);
        DateTime actual = task.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_yearly_freq_5_day_63() {
        Task task = new Task("", 0, 0, TestUtils.createDateTime(2016, 3, 3), Task.YEARLY, 5);

        task.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2021, 3, 3);
        DateTime actual = task.getNext();

        assertEquals(expected, actual);
    }

}
