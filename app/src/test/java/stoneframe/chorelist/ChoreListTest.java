package stoneframe.chorelist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.SimpleChoreSelector;
import stoneframe.chorelist.model.SimpleEffortTracker;

public class ChoreListTest
{
    private final int MAX_EFFORT = 10;

    private ChoreList choreList;

    @Before
    public void before()
    {
        choreList = new ChoreList(new SimpleEffortTracker(MAX_EFFORT), new SimpleChoreSelector());
    }

    @Test
    public void GetAllChores_NoChoresAdded_ReturnEmptyList()
    {
        List<Chore> allChores = choreList.getAllChores();

        assertTrue(allChores.isEmpty());
    }

    @Test
    public void AddChore_AddSingleChore_GetAllChoresContainsChore()
    {
        // ARRANGE
        Chore chore = new Chore("", 1, 1, DateTime.now(), Chore.DAILY, 1);

        // ACT
        choreList.addChore(chore);

        // ASSERT
        assertTrue(choreList.getAllChores().contains(chore));
    }

    @Test
    public void RemoveChore_RemoveExistingChore_GetAllChoresDoesNotContainChore()
    {
        // ARRANGE
        Chore chore = new Chore("", 1, 1, DateTime.now(), Chore.DAILY, 1);

        choreList.addChore(chore);

        // ACT
        choreList.removeChore(chore);

        // ASSERT
        assertFalse(choreList.getAllChores().contains(chore));
    }

    @Test
    public void GetTodaysChores_NoChoresAdded_ReturnEmptyList()
    {
        // ACT
        List<Chore> todaysChores = choreList.getTodaysChores(TestUtils.MOCK_NOW);

        // ASSERT
        assertTrue(todaysChores.isEmpty());
    }

    @Test
    public void GetTodaysChores_ChoreAddedWithTodaysDate_TodaysChoresContainsAddedChore()
    {
        // ARRANGE
        DateTime today = TestUtils.MOCK_NOW;

        Chore choreForToday = new Chore("", 1, 1, today, Chore.DAILY, 1);

        choreList.addChore(choreForToday);

        // ACT
        List<Chore> todaysChores = choreList.getTodaysChores(today);

        // ASSERT
        assertTrue(todaysChores.contains(choreForToday));
    }

    @Test
    public void GetTodaysChores_ChoreAddedWithYesterdaysDate_TodaysChoresContainsAddedChore()
    {
        // ARRANGE
        DateTime today = TestUtils.MOCK_NOW;
        DateTime yesterday = today.minusDays(1);

        Chore choreForYesterday = new Chore("", 1, 1, yesterday, Chore.DAILY, 1);

        choreList.addChore(choreForYesterday);

        // ACT
        List<Chore> todaysChores = choreList.getTodaysChores(today);

        // ASSERT
        assertTrue(todaysChores.contains(choreForYesterday));
    }

    @Test
    public void GetTodaysChores_ChoreAddedWithTomorrowsDate_TodaysChoresContainsAddedChore()
    {
        // ARRANGE
        DateTime today = TestUtils.MOCK_NOW;
        DateTime tomorrow = today.plusDays(1);

        Chore choreForTomorrow = new Chore("", 1, 1, tomorrow, Chore.DAILY, 1);

        choreList.addChore(choreForTomorrow);

        // ACT
        List<Chore> todaysChores = choreList.getTodaysChores(today);

        // ASSERT
        assertFalse(todaysChores.contains(choreForTomorrow));
    }

    @Test
    public void ChoreDone_ChoreForEveryDay_RescheduleChoreForTomorrow()
    {
        // ARRANGE
        DateTime today = TestUtils.MOCK_NOW;

        Chore everydayChore = new Chore("", 1, 1, today, Chore.DAILY, 1);

        choreList.addChore(everydayChore);

        // ACT
        choreList.choreDone(everydayChore, today);

        // ASSERT
        assertEquals(today.plusDays(1), choreList.getAllChores().get(0).getNext());
    }

    @Test
    public void GetRemainingEffort_NoChoreDone_ReturnMaxEffort()
    {
        // ACT
        int remainingEffort = choreList.getRemainingEffort(TestUtils.MOCK_NOW);

        // ASSERT
        assertEquals(MAX_EFFORT, remainingEffort);
    }
}
