package stoneframe.serena.chores.efforttrackers;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
import org.junit.Test;

public class WeeklyEffortTrackerTest
{
    private static final LocalDate MONDAY = new LocalDate(2024, 1, 8);
    private static final LocalDate SUNDAY = new LocalDate(2024, 1, 7);

    @Test
    public void setSunday_whenSundayIsActive_updatesSundayRemainingEffort()
    {
        // ARRANGE
        WeeklyEffortTracker tracker = createTracker();
        tracker.getTodaysEffort(SUNDAY);

        // ACT
        tracker.setSunday(40);

        // ASSERT
        assertEquals(40, tracker.getTodaysEffort(SUNDAY));
    }

    @Test
    public void setSunday_whenAnotherDayIsActive_preservesActiveDayRemainingEffort()
    {
        // ARRANGE
        WeeklyEffortTracker tracker = createTracker();
        tracker.getTodaysEffort(MONDAY);
        tracker.spend(3);

        // ACT
        tracker.setSunday(40);

        // ASSERT
        assertEquals(7, tracker.getTodaysEffort(MONDAY));
    }

    private WeeklyEffortTracker createTracker()
    {
        return new WeeklyEffortTracker(10, 10, 10, 10, 10, 10, 30);
    }
}
