package stoneframe.chorelist;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.ChoreManager;
import stoneframe.chorelist.model.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.efforttrackers.SimpleEffortTracker;

public class ChoreManagerTest
{
    @Test
    public void two_chores_scheduled_for_before_now()
    {
        Chore chore1 = new Chore("Chore1", 5, 5, TestUtils.MOCK_NOW.minusDays(2), 1, Chore.DAYS);
        Chore chore2 = new Chore("Chore2", 3, 8, TestUtils.MOCK_NOW.minusDays(1), 1, Chore.DAYS);

        ChoreManager choreManager = new ChoreManager(
            new SimpleEffortTracker(15),
            new SimpleChoreSelector());

        choreManager.addChore(chore1);
        choreManager.addChore(chore2);

        List<Chore> expected = new LinkedList<>();
        expected.add(chore2);
        expected.add(chore1);

        List<Chore> actual = choreManager.getChores(TestUtils.MOCK_NOW);

        assertEquals(expected, actual);
    }
}
