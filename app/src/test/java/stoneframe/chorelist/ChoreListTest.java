package stoneframe.chorelist;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import stoneframe.chorelist.model.Chore;
import stoneframe.chorelist.model.SimpleChoreSelector;
import stoneframe.chorelist.model.SimpleEffortTracker;

public class ChoreListTest
{
    private final int MAX_EFFORT = 10;

    private static final DateTime TODAY = TestUtils.MOCK_NOW;
    private static final DateTime YESTERDAY = TODAY.minusDays(1);
    private static final DateTime TOMORROW = TODAY.plusDays(1);

    private ChoreList choreList;

    @Before
    public void before()
    {
        choreList = new ChoreList(new SimpleEffortTracker(MAX_EFFORT), new SimpleChoreSelector());
    }

    @Test
    public void GetAllChores_NoChoresAdded_ReturnEmptyList()
    {
        assertAllChoresIsEmpty();
    }

    @Test
    public void AddChore_AddSingleChore_GetAllChoresContainsChore()
    {
        addChore("Chore1", TODAY);
        assertAllChoresContains("Chore1");
    }

    @Test
    public void RemoveChore_RemoveExistingChore_GetAllChoresDoesNotContainChore()
    {
        addChore("Chore1", TODAY);
        addChore("Chore2", TODAY);
        removeChore("Chore1");
        assertAllChoresContains("Chore2");
    }

    @Test
    public void GetTodaysChores_NoChoresAdded_ReturnEmptyList()
    {
        assertTodaysChoresIsEmpty(TODAY);
    }

    @Test
    public void GetTodaysChores_ChoreAddedWithTodaysDate_TodaysChoresContainsAddedChore()
    {
        addChore("ChoreForToday", TODAY);
        assertTodaysChoresContains(TODAY, "ChoreForToday");
    }

    @Test
    public void GetTodaysChores_ChoreAddedWithYesterdaysDate_TodaysChoresContainsAddedChore()
    {
        addChore("choreForYesterday", YESTERDAY);
        assertTodaysChoresContains(TODAY, "choreForYesterday");
    }

    @Test
    public void GetTodaysChores_ChoreAddedWithTomorrowsDate_TodaysChoresContainsAddedChore()
    {
        addChore("choreForTomorrow", TOMORROW);
        assertTodaysChoresIsEmpty(TODAY);
    }

    @Test
    public void ChoreDone_ChoreForEveryDay_RescheduleChoreForTomorrow()
    {
        addChore("everydayChore", TODAY, 1, Chore.DAYS);
        completeChore("everydayChore", TODAY);
        assertChore("everydayChore", c -> c.getNext().equals(TOMORROW));
    }

    @Test
    public void GetRemainingEffort_NoChoreDone_ReturnMaxEffort()
    {
        assertRemainingEffortIs(MAX_EFFORT, TODAY);
    }

    @Test
    public void GetRemainingEffort_OneChoreDone_ReturnLessRemainingEffort()
    {
        final int choreEffort = 5;

        addChore("Chore", TODAY, choreEffort);
        completeChore("Chore", TODAY);
        assertRemainingEffortIs(MAX_EFFORT - choreEffort, TODAY);
    }

    private void addChore(String description, DateTime next)
    {
        addChore(description, next, 1, Chore.DAYS);
    }

    private void addChore(String description, DateTime next, int effort)
    {
        addChore(description, next, 1, Chore.DAYS, effort);
    }

    private void addChore(String description, DateTime next, int intervalLength, int intervalUnit)
    {
        addChore(description, next, intervalLength, intervalUnit, 1);
    }

    private void addChore(
        String description,
        DateTime next,
        int intervalLength,
        int intervalUnit,
        int effort)
    {
        Chore chore = new Chore(description, 1, effort, next, intervalLength, intervalUnit);

        choreList.addChore(chore);
    }

    private void removeChore(String description)
    {
        Chore chore = getChore(description);

        choreList.removeChore(chore);
    }

    @NonNull
    private Chore getChore(String description)
    {
        Optional<Chore> choreOptional = choreList.getAllChores()
            .stream()
            .filter(c -> c.getDescription().equals(description))
            .findFirst();

        assertTrue(choreOptional.isPresent());

        return choreOptional.get();
    }

    private void completeChore(String description, DateTime now)
    {
        Chore chore = getChore(description);

        choreList.choreDone(chore, now);
    }

    private void assertAllChoresIsEmpty()
    {
        assertAllChoresContains();
    }

    private void assertAllChoresContains(String... descriptions)
    {
        List<String> expectedDescriptions = Arrays.asList(descriptions);

        List<String> actualDescriptions = choreList.getAllChores()
            .stream()
            .map(Chore::getDescription)
            .collect(Collectors.toList());

        assertEquals(expectedDescriptions.size(), actualDescriptions.size());
        assertTrue(expectedDescriptions.containsAll(actualDescriptions));
    }

    private void assertTodaysChoresIsEmpty(DateTime now)
    {
        assertTodaysChoresContains(now);
    }

    private void assertTodaysChoresContains(DateTime now, String... descriptions)
    {
        List<String> expectedDescriptions = Arrays.asList(descriptions);

        List<String> actualDescriptions = choreList.getTodaysChores(now)
            .stream()
            .map(Chore::getDescription)
            .collect(Collectors.toList());

        assertEquals(expectedDescriptions.size(), actualDescriptions.size());
        assertTrue(expectedDescriptions.containsAll(actualDescriptions));
    }

    private void assertRemainingEffortIs(int expectedRemainingEffort, DateTime now)
    {
        assertEquals(expectedRemainingEffort, choreList.getRemainingEffort(now));
    }

    private void assertChore(String description, Predicate<Chore> predicate)
    {
        Chore chore = getChore(description);

        assertTrue(predicate.test(chore));
    }
}
