package stoneframe.chorelist;

import org.joda.time.DateTime;
import org.junit.Test;

import stoneframe.chorelist.model.Duty;

import static org.junit.Assert.*;

public class Test_Duty_reschedule {

    @Test
    public void test_daliy_freq_3() {
        Duty duty = new Duty("", 0, 0, TestUtils.createDateTime(2017, 2, 4), Duty.DAILY, 3);

        duty.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 7);
        DateTime actual = duty.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_daliy_freq_5() {
        Duty duty = new Duty("", 0, 0, TestUtils.createDateTime(2017, 2, 4), Duty.DAILY, 5);

        duty.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 9);
        DateTime actual = duty.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_daily_freq_5_previous_one_week_ago() {
        Duty duty = new Duty("", 0, 0, TestUtils.createDateTime(2017, 1, 29), Duty.DAILY, 5);

        duty.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 8);
        DateTime actual = duty.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_weekly_freq_1_day_3() {
        Duty duty = new Duty("", 0, 0, TestUtils.createDateTime(2017, 2, 1), Duty.WEEKLY, 1, 3);

        duty.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 8);
        DateTime actual = duty.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_weekly_freq_4_day_7() {
        Duty duty = new Duty("", 0, 0, TestUtils.createDateTime(2017, 1, 29), Duty.WEEKLY, 4, 7);

        duty.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 26);
        DateTime actual = duty.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_monthly_freq_2_day_5() {
        Duty duty = new Duty("", 0, 0, TestUtils.createDateTime(2017, 1, 5), Duty.MONTHLY, 2, 5);

        duty.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 3, 5);
        DateTime actual = duty.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_monthly_freq_3_day_23() {
        Duty duty = new Duty("", 0, 0, TestUtils.createDateTime(2017, 1, 23), Duty.MONTHLY, 3, 23);

        duty.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 4, 23);
        DateTime actual = duty.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_yearly_freq_2_day_253() {
        Duty duty = new Duty("", 0, 0, TestUtils.createDateTime(2016, 9, 9), Duty.YEARLY, 2, 253);

        duty.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2018, 9, 9);
        DateTime actual = duty.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_yearly_freq_5_day_63() {
        Duty duty = new Duty("", 0, 0, TestUtils.createDateTime(2016, 3, 3), Duty.YEARLY, 5, 63);

        duty.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2021, 3, 3);
        DateTime actual = duty.getNext();

        assertEquals(expected, actual);
    }

}
