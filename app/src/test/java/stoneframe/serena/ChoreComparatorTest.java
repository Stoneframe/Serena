package stoneframe.serena;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import stoneframe.serena.mocks.TestContext;
import stoneframe.serena.model.chores.Chore;
import stoneframe.serena.model.chores.ChoreEditor;
import stoneframe.serena.model.chores.ChoreManager;
import stoneframe.serena.model.chores.IntervalRepetition;

public class ChoreComparatorTest
{
    private TestContext context;

    private ChoreManager choreManager;

    private Chore.ChoreComparator comparator;

    @Before
    public void setUp()
    {
        context = new TestContext();

        choreManager = context.getChoreManager();

        comparator = new Chore.ChoreComparator(TestUtils.MOCK_TODAY);
    }

    @Test
    public void chore_scheduled_before_now_and_chore_scheduled_after_now()
    {
        Chore chore1 = createChore(3, 10, TestUtils.MOCK_TODAY.minusDays(1));
        Chore chore2 = createChore(1, 10, TestUtils.MOCK_TODAY.plusDays(1));

        int expected = -1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void two_chores_scheduled_after_now()
    {
        Chore chore1 = createChore(3, 10, TestUtils.MOCK_TODAY.plusDays(2));
        Chore chore = createChore(6, 10, TestUtils.MOCK_TODAY.plusDays(1));

        int expected = 1;
        int actual = comparator.compare(chore1, chore);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_date_1()
    {
        Chore chore1 = createChore(1, 10, TestUtils.MOCK_TODAY.plusDays(1));
        Chore chore2 = createChore(1, 10, TestUtils.MOCK_TODAY.plusDays(0));

        int expected = 1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_date_2()
    {
        Chore chore1 = createChore(1, 10, TestUtils.MOCK_TODAY.plusWeeks(0));
        Chore chore2 = createChore(1, 10, TestUtils.MOCK_TODAY.plusWeeks(2));

        int expected = -1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_priority_1()
    {
        Chore chore1 = createChore(2, 10, TestUtils.MOCK_TODAY);
        Chore chore2 = createChore(6, 10, TestUtils.MOCK_TODAY);

        int expected = -1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_priority_2()
    {
        Chore chore1 = createChore(8, 10, TestUtils.MOCK_TODAY);
        Chore chore2 = createChore(1, 10, TestUtils.MOCK_TODAY);

        int expected = 1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_effort_1()
    {
        Chore chore1 = createChore(2, 3, TestUtils.MOCK_TODAY);
        Chore chore2 = createChore(2, 5, TestUtils.MOCK_TODAY);

        int expected = -1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void test_compare_effort_2()
    {
        Chore chore1 = createChore(2, 60, TestUtils.MOCK_TODAY);
        Chore chore2 = createChore(2, 25, TestUtils.MOCK_TODAY);

        int expected = 1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    private Chore createChore(int priority, int effort, LocalDate next)
    {
        Chore chore = choreManager.createChore();

        ChoreEditor choreEditor = choreManager.getChoreEditor(chore);

        choreEditor.setDescription("Chore");
        choreEditor.setPriority(priority);
        choreEditor.setEffort(effort);

        IntervalRepetition repetition = (IntervalRepetition)choreEditor.getRepetition();

        repetition.setNext(next);
        repetition.setIntervalLength(3);
        repetition.setIntervalUnit(IntervalRepetition.DAYS);

        choreEditor.save();

        return chore;
    }
}
