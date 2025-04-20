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

/** @noinspection UnnecessaryLocalVariable*/
public class ChoreTest
{
    private TestContext context;

    private ChoreManager choreManager;

    @Before
    public void before()
    {
        context = new TestContext();

        choreManager = context.getChoreManager();
    }

    @Test
    public void complete_intervalIsEvery3DaysAndInitialNextIsToday_nextIsThreeDaysAfterToday()
    {
        final LocalDate fridayWeek1 = new LocalDate(2024, 1, 5);
        final LocalDate mondayWeek2 = new LocalDate(2024, 1, 8);

        // ARRANGE
        LocalDate initialNext = fridayWeek1;

        int intervalLength = 3;
        int intervalUnit = IntervalRepetition.DAYS;

        Chore chore = createChore(initialNext, intervalLength, intervalUnit);

        context.setCurrentTime(fridayWeek1);

        // ACT
        choreManager.complete(chore);

        // ASSERT
        LocalDate expected = mondayWeek2;
        LocalDate actual = chore.getNext();

        assertEquals(expected, actual);
    }

    @Test
    public void complete_intervalIsEvery5DaysAndInitialNextIsTwoDaysAgo_nextIsFiveDaysAfterToday()
    {
        final LocalDate wednesdayWeek1 = new LocalDate(2024, 1, 3);
        final LocalDate fridayWeek1 = new LocalDate(2024, 1, 5);
        final LocalDate wednesdayWeek2 = new LocalDate(2024, 1, 10);

        // ARRANGE
        LocalDate initialNext = wednesdayWeek1;
        int intervalLength = 5;
        int intervalUnit = IntervalRepetition.DAYS;

        Chore chore = createChore(initialNext, intervalLength, intervalUnit);

        context.setCurrentTime(fridayWeek1);

        // ACT
        choreManager.complete(chore);

        // ASSERT
        assertEquals(wednesdayWeek2, chore.getNext());
    }

    @Test
    public void complete_intervalIsEvery5DaysAndInitialNextIsOneWeekAgo_nextIsFiveDaysAfterToday()
    {
        final LocalDate wednesdayWeek1 = new LocalDate(2024, 1, 3);
        final LocalDate wednesdayWeek2 = new LocalDate(2024, 1, 10);
        final LocalDate mondayWeek3 = new LocalDate(2024, 1, 15);

        // ARRANGE
        LocalDate initialNext = wednesdayWeek1;

        int intervalLength = 5;
        int intervalUnit = IntervalRepetition.DAYS;

        Chore chore = createChore(initialNext, intervalLength, intervalUnit);

        context.setCurrentTime(wednesdayWeek2);

        // ACT
        choreManager.complete(chore);

        // ASSERT
        assertEquals(mondayWeek3, chore.getNext());
    }

    @Test
    public void complete_intervalEveryWeekOnWednesday_nextIsInOneWeekOnWednesday()
    {
        final LocalDate fridayWeek1 = new LocalDate(2024, 1, 5);
        final LocalDate wednesdayWeek1 = new LocalDate(2024, 1, 3);
        final LocalDate wednesdayWeek2 = new LocalDate(2024, 1, 10);

        // ARRANGE
        LocalDate initialNext = wednesdayWeek1;

        int intervalLength = 1;
        int intervalUnit = IntervalRepetition.WEEKS;

        Chore chore = createChore(initialNext, intervalLength, intervalUnit);

        context.setCurrentTime(fridayWeek1);

        // ACT
        choreManager.complete(chore);

        // ASSERT
        assertEquals(wednesdayWeek2, chore.getNext());
    }

    @Test
    public void complete_intervalEveryFourWeeksOnWednesday_nextIsAfterFourWeekOnWednesday()
    {
        final LocalDate fridayWeek1 = new LocalDate(2024, 1, 5);
        final LocalDate wednesdayWeek1 = new LocalDate(2024, 1, 3);
        final LocalDate wednesdayWeek5 = new LocalDate(2024, 1, 31);

        // ARRANGE
        LocalDate initialNext = wednesdayWeek1;

        int intervalLength = 4;
        int intervalUnit = IntervalRepetition.WEEKS;

        Chore chore = createChore(initialNext, intervalLength, intervalUnit);

        context.setCurrentTime(fridayWeek1);

        // ACT
        choreManager.complete(chore);

        // ASSERT
        assertEquals(wednesdayWeek5, chore.getNext());
    }

    @Test
    public void complete_intervalEveryTwoMonthsOnDay3_nextIsAfterTwoMonthsOnDay3()
    {
        final LocalDate january3 = new LocalDate(2024, 1, 3);
        final LocalDate january5 = new LocalDate(2024, 1, 5);
        final LocalDate march3 = new LocalDate(2024, 3, 3);

        // ARRANGE
        LocalDate initialNext = january3;

        int intervalLength = 2;
        int intervalUnit = IntervalRepetition.MONTHS;

        Chore chore = createChore(initialNext, intervalLength, intervalUnit);

        context.setCurrentTime(january5);

        // ACT
        choreManager.complete(chore);

        // ASSERT
        assertEquals(march3, chore.getNext());
    }

    @Test
    public void complete_intervalEveryThreeMonthsOnDay13_nextIsAfterThreeMonthsOnDay13()
    {
        final LocalDate january13 = new LocalDate(2024, 1, 13);
        final LocalDate january16 = new LocalDate(2024, 1, 16);
        final LocalDate april13 = new LocalDate(2024, 4, 13);

        // ARRANGE
        LocalDate initialNext = january13;

        int intervalLength = 3;
        int intervalUnit = IntervalRepetition.MONTHS;

        Chore chore = createChore(initialNext, intervalLength, intervalUnit);

        context.setCurrentTime(january16);

        // ACT
        choreManager.complete(chore);

        // ASSERT
        assertEquals(april13, chore.getNext());
    }

    @Test
    public void complete_intervalEvery2YearsOnJanuary13_nextIsAfter2YearsOnJanuary13()
    {
        final LocalDate january13Year1 = new LocalDate(2024, 1, 13);
        final LocalDate january16Year1 = new LocalDate(2024, 1, 16);
        final LocalDate january13Year3 = new LocalDate(2026, 1, 13);

        // ARRANGE
        LocalDate initialNext = january13Year1;

        int intervalLength = 2;
        int intervalUnit = IntervalRepetition.YEARS;

        Chore chore = createChore(initialNext, intervalLength, intervalUnit);

        context.setCurrentTime(january16Year1);

        // ACT
        choreManager.complete(chore);

        // ASSERT
        assertEquals(january13Year3, chore.getNext());
    }

    @Test
    public void complete_intervalEvery5YearsOnJanuary13_nextIsAfter5YearsOnJanuary13()
    {
        final LocalDate january13Year1 = new LocalDate(2024, 1, 13);
        final LocalDate january16Year1 = new LocalDate(2024, 1, 16);
        final LocalDate january13Year5 = new LocalDate(2029, 1, 13);

        // ARRANGE
        LocalDate initialNext = january13Year1;

        int intervalLength = 5;
        int intervalUnit = IntervalRepetition.YEARS;

        Chore chore = createChore(initialNext, intervalLength, intervalUnit);

        context.setCurrentTime(january16Year1);

        // ART
        choreManager.complete(chore);

        // ASSERT
        assertEquals(january13Year5, chore.getNext());
    }

    private Chore createChore(LocalDate next, int intervalLength, int intervalUnit)
    {
        Chore chore = choreManager.createChore();

        ChoreEditor choreEditor = choreManager.getChoreEditor(chore);

        choreEditor.setDescription("Chore");
        choreEditor.setPriority(0);
        choreEditor.setEffort(0);

        IntervalRepetition repetition = (IntervalRepetition)choreEditor.getRepetition();

        repetition.setNext(next);
        repetition.setIntervalLength(intervalLength);
        repetition.setIntervalUnit(intervalUnit);

        choreEditor.save();

        return chore;
    }
}
