package stoneframe.serena;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Test;

import stoneframe.serena.model.chores.Chore;

public class ChoreTest
{
    @Test
    public void test_daily_freq_3()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDate(2017, 2, 4), 3, Chore.DAYS);

        chore.reschedule(TestUtils.createDate(2017, 2, 5));

        LocalDate expected = TestUtils.createDate(2017, 2, 8);
        LocalDate actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_daily_freq_5()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDate(2017, 2, 4), 5, Chore.DAYS);

        chore.reschedule(TestUtils.createDate(2017, 2, 5));

        LocalDate expected = TestUtils.createDate(2017, 2, 10);
        LocalDate actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_daily_freq_5_previous_one_week_ago()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDate(2017, 1, 29), 5, Chore.DAYS);

        chore.reschedule(TestUtils.createDate(2017, 2, 5));

        LocalDate expected = TestUtils.createDate(2017, 2, 10);
        LocalDate actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_weekly_freq_1_day_3()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDate(2017, 2, 1), 1, Chore.WEEKS);

        chore.reschedule(TestUtils.createDate(2017, 2, 5));

        LocalDate expected = TestUtils.createDate(2017, 2, 8);
        LocalDate actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_weekly_freq_4_day_7()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDate(2017, 1, 29), 4, Chore.WEEKS);

        chore.reschedule(TestUtils.createDate(2017, 2, 5));

        LocalDate expected = TestUtils.createDate(2017, 2, 26);
        LocalDate actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_monthly_freq_2_day_5()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDate(2017, 1, 5), 2, Chore.MONTHS);

        chore.reschedule(TestUtils.createDate(2017, 2, 5));

        LocalDate expected = TestUtils.createDate(2017, 3, 5);
        LocalDate actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_monthly_freq_3_day_23()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDate(2017, 1, 23), 3, Chore.MONTHS);

        chore.reschedule(TestUtils.createDate(2017, 2, 5));

        LocalDate expected = TestUtils.createDate(2017, 4, 23);
        LocalDate actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_yearly_freq_2_day_253()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDate(2016, 9, 9), 2, Chore.YEARS);

        chore.reschedule(TestUtils.createDate(2017, 2, 5));

        LocalDate expected = TestUtils.createDate(2018, 9, 9);
        LocalDate actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void test_yearly_freq_5_day_63()
    {
        Chore chore = new Chore("", 0, 0, TestUtils.createDate(2016, 3, 3), 5, Chore.YEARS);

        chore.reschedule(TestUtils.createDate(2017, 2, 5));

        LocalDate expected = TestUtils.createDate(2021, 3, 3);
        LocalDate actual = chore.getNext();

        assertEquals(expected, actual);
    }
}
