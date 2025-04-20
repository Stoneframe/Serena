package stoneframe.serena;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import stoneframe.serena.mocks.TestContext;
import stoneframe.serena.chores.Chore;
import stoneframe.serena.chores.ChoreEditor;
import stoneframe.serena.chores.ChoreManager;
import stoneframe.serena.chores.IntervalRepetition;

public class ChoreComparatorTest
{
    private static final LocalDate TODAY = new LocalDate(2024, 1, 1);

    private TestContext context;

    private ChoreManager choreManager;

    private Chore.ChoreComparator comparator;

    @Before
    public void before()
    {
        context = new TestContext();

        choreManager = context.getChoreManager();

        comparator = new Chore.ChoreComparator(TODAY);
    }

    @Test
    public void compare_differentNextDate1_earlierIsSortedFirst()
    {
        Chore chore1 = createChore(3, 10, TODAY.minusDays(1));
        Chore chore2 = createChore(1, 10, TODAY.plusDays(1));

        int expected = -1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void compare_differentNextDate2_earlierIsSortedFirst()
    {
        Chore chore1 = createChore(3, 10, TODAY.plusDays(2));
        Chore chore = createChore(6, 10, TODAY.plusDays(1));

        int expected = 1;
        int actual = comparator.compare(chore1, chore);

        assertEquals(expected, actual);
    }

    @Test
    public void compare_differentNextDate3_earlierIsSortedFirst()
    {
        Chore chore1 = createChore(1, 10, TODAY.plusDays(1));
        Chore chore2 = createChore(1, 10, TODAY.plusDays(0));

        int expected = 1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void compare_differentNextDate4_earlierIsSortedFirst()
    {
        Chore chore1 = createChore(1, 10, TODAY.plusWeeks(0));
        Chore chore2 = createChore(1, 10, TODAY.plusWeeks(2));

        int expected = -1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void compare_differentPriority1_lowestValueIsSortedFirst()
    {
        Chore chore1 = createChore(2, 10, TODAY);
        Chore chore2 = createChore(6, 10, TODAY);

        int expected = -1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void compare_differentPriority2_lowestValueIsSortedFirst()
    {
        Chore chore1 = createChore(8, 10, TODAY);
        Chore chore2 = createChore(1, 10, TODAY);

        int expected = 1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void compare_differentEffort1_lowestValueIsSortedFirst()
    {
        Chore chore1 = createChore(2, 3, TODAY);
        Chore chore2 = createChore(2, 5, TODAY);

        int expected = -1;
        int actual = comparator.compare(chore1, chore2);

        assertEquals(expected, actual);
    }

    @Test
    public void compare_differentEffort2_lowestValueIsSortedFirst()
    {
        Chore chore1 = createChore(2, 60, TODAY);
        Chore chore2 = createChore(2, 25, TODAY);

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
