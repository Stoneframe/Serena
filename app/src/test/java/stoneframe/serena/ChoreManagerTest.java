package stoneframe.serena;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import stoneframe.serena.mocks.TestContext;
import stoneframe.serena.model.chores.Chore;
import stoneframe.serena.model.chores.ChoreEditor;
import stoneframe.serena.model.chores.ChoreManager;
import stoneframe.serena.model.chores.IntervalRepetition;


public class ChoreManagerTest
{
    private TestContext context;

    private ChoreManager choreManager;

    @Before
    public void setUp()
    {
        context = new TestContext();

        choreManager = context.getChoreManager();
    }

    @Test
    public void two_chores_scheduled_for_before_now()
    {
        context.setCurrentTime(TestUtils.MOCK_TODAY);

        Chore chore1 = createChore("Chore1", 5, 2, TestUtils.MOCK_TODAY.minusDays(2));
        Chore chore2 = createChore("Chore2", 3, 5, TestUtils.MOCK_TODAY.minusDays(1));

        List<Chore> expected = Arrays.asList(chore2, chore1);
        List<Chore> actual = choreManager.getTodaysChores();

        assertEquals(expected, actual);
    }

    private Chore createChore(String description, int priority, int effort, LocalDate next)
    {
        Chore chore = choreManager.createChore();

        ChoreEditor choreEditor = choreManager.getChoreEditor(chore);

        choreEditor.setDescription(description);
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
