package stoneframe.chorelist;

import org.junit.Before;
import org.junit.Test;

import stoneframe.chorelist.model.Chore;

import static org.junit.Assert.*;

public class ChoreComparatorTest
{
    private Chore.ChoreComparator comparator;

    @Before
    public void setUp()
    {
        comparator = new Chore.ChoreComparator(TestUtils.MOCK_NOW);
    }

    @Test
    public void chore_scheduled_before_now_and_chore_scheduled_after_now()
    {
        Chore t1 = new Chore("Chore1", 3, 10, TestUtils.MOCK_NOW.minusDays(1), 3, Chore.DAYS);
        Chore t2 = new Chore("Chore2", 1, 10, TestUtils.MOCK_NOW.plusDays(1), 6, Chore.DAYS);

        int expected = -1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void two_chores_scheduled_after_now()
    {
        Chore t1 = new Chore("Chore1", 3, 10, TestUtils.MOCK_NOW.plusDays(2), 3, Chore.DAYS);
        Chore t2 = new Chore("Chore2", 6, 10, TestUtils.MOCK_NOW.plusDays(1), 6, Chore.DAYS);

        int expected = 1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_date_1()
    {
        Chore t1 = new Chore("Chore1", 1, 10, TestUtils.MOCK_NOW.plusDays(1), 5, Chore.DAYS);
        Chore t2 = new Chore("Chore2", 1, 10, TestUtils.MOCK_NOW, 2, Chore.DAYS);

        int expected = 1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_date_2()
    {
        Chore t1 = new Chore("Chore1", 1, 10, TestUtils.MOCK_NOW, 3, Chore.DAYS);
        Chore t2 = new Chore("Chore2", 1, 10, TestUtils.MOCK_NOW.plusWeeks(2), 6, Chore.DAYS);

        int expected = -1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_priority_1()
    {
        Chore t1 = new Chore("Chore1", 2, 10, TestUtils.MOCK_NOW, 3, Chore.DAYS);
        Chore t2 = new Chore("Chore2", 6, 10, TestUtils.MOCK_NOW, 3, Chore.DAYS);

        int expected = -1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_priority_2()
    {
        Chore t1 = new Chore("Chore1", 8, 10, TestUtils.MOCK_NOW, 3, Chore.DAYS);
        Chore t2 = new Chore("Chore2", 1, 10, TestUtils.MOCK_NOW, 3, Chore.DAYS);

        int expected = 1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_effort_1()
    {
        Chore t1 = new Chore("Chore1", 2, 3, TestUtils.MOCK_NOW, 3, Chore.DAYS);
        Chore t2 = new Chore("Chore2", 2, 5, TestUtils.MOCK_NOW, 3, Chore.DAYS);

        int expected = -1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_effort_2()
    {
        Chore t1 = new Chore("Chore1", 2, 60, TestUtils.MOCK_NOW, 3, Chore.DAYS);
        Chore t2 = new Chore("Chore2", 2, 25, TestUtils.MOCK_NOW, 3, Chore.DAYS);

        int expected = 1;
        int actual = comparator.compare(t1, t2);

        assertEquals(expected, actual);
    }
}
