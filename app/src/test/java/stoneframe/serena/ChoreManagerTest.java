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
    private static final LocalDate TODAY = new LocalDate(2024, 1, 1);

    private TestContext context;

    private ChoreManager choreManager;

    @Before
    public void before()
    {
        context = new TestContext();

        choreManager = context.getChoreManager();
    }

    @Test
    public void getTodaysChores_twoChoresWithNextBeforeToday_listContainsBothChores()
    {
        // ARRANGE
        Chore chore1 = createChore("Chore1", 5, 2, TODAY.minusDays(2));
        Chore chore2 = createChore("Chore2", 3, 5, TODAY.minusDays(1));

        // ACT
        context.setCurrentTime(TODAY);

        List<Chore> todaysChores = choreManager.getTodaysChores();

        // ASSERT
        assertEquals(Arrays.asList(chore2, chore1), todaysChores);
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
