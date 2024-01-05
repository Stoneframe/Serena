package stoneframe.chorelist;

import org.joda.time.DateTime;
import org.junit.Test;

import stoneframe.chorelist.model.Chore;

import static org.junit.Assert.*;

public class ChoreTest
{
    @Test
    public void test_daily_freq_3()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDateTime(2017, 2, 4), Chore.DAILY, 3);

        chore.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 8);
        DateTime actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_daily_freq_5()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDateTime(2017, 2, 4), Chore.DAILY, 5);

        chore.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 10);
        DateTime actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_daily_freq_5_previous_one_week_ago()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDateTime(2017, 1, 29), Chore.DAILY, 5);

        chore.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 10);
        DateTime actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_weekly_freq_1_day_3()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDateTime(2017, 2, 1), Chore.WEEKLY, 1);

        chore.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 8);
        DateTime actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_weekly_freq_4_day_7()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDateTime(2017, 1, 29), Chore.WEEKLY, 4);

        chore.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 2, 26);
        DateTime actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_monthly_freq_2_day_5()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDateTime(2017, 1, 5), Chore.MONTHLY, 2);

        chore.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 3, 5);
        DateTime actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_monthly_freq_3_day_23()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDateTime(2017, 1, 23), Chore.MONTHLY, 3);

        chore.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2017, 4, 23);
        DateTime actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_yearly_freq_2_day_253()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDateTime(2016, 9, 9), Chore.YEARLY, 2);

        chore.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2018, 9, 9);
        DateTime actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_yearly_freq_5_day_63()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDateTime(2016, 3, 3), Chore.YEARLY, 5);

        chore.reschedule(TestUtils.createDateTime(2017, 2, 5));

        DateTime expected = TestUtils.createDateTime(2021, 3, 3);
        DateTime actual = chore.getNext();

        assertEquals(expected, actual);
    }
}
