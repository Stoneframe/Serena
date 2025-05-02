package stoneframe.serena.sleep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

public class SleepTest
{
    private LocalDateTime now;

    private Sleep sleep;

    @Before
    public void setUp()
    {
        now = LocalDateTime.now();

        sleep = new Sleep(now);
    }

    @Test
    public void getPoints_sleepInitialized_pointsAreZero()
    {
        int points = sleep.getPoints(now);

        assertEquals(0, points);
    }

    @Test
    public void getPoints_oneDayAfterInitialization_pointsIsMinusSeven()
    {
        int points = sleep.getPoints(now.plusDays(1));

        assertEquals(-7, points);
    }

    @Test
    public void getPoints_twoDaysAfterInitialization_pointsIsMinusTen()
    {
        int points = sleep.getPoints(now.plusDays(2));

        assertEquals(-10, points);
    }

    @Test
    public void getPoints_sleepEightHours_pointsIsOneOneDayLater()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(8));

        int points = sleep.getPoints(now.plusDays(1));

        assertEquals(-2, points);
    }

    @Test
    public void getPoints_sleepFourHours_pointsIsMinusThreeOneDayLater()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(4));

        int points = sleep.getPoints(now.plusDays(1));

        assertEquals(-3, points);
    }

    @Test
    public void getPoints_sleepFourHoursTwice_pointsIsOneOneDayLater()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(4));

        sleep.toggle(now.plusHours(6));
        sleep.toggle(now.plusHours(10));

        int points = sleep.getPoints(now.plusDays(1));

        assertEquals(-1, points);
    }

    @Test
    public void getPoints_sleepEightHoursInFiveMinuteIntervals_pointsIsOneOneDayLater()
    {
        for (int i = 0; i < 960; i += 10)
        {
            sleep.toggle(now.plusMinutes(i));
            sleep.toggle(now.plusMinutes(i + 5));
        }

        int points = sleep.getPoints(now.plusDays(1));

        assertEquals(1, points);
    }

    @Test
    public void getPoints_sleepEightHoursOneDayAfterInitialization_pointsIsMinusFourTwoDaysLater()
    {
        sleep.toggle(now.plusDays(1).plusHours(0));
        sleep.toggle(now.plusDays(1).plusHours(8));

        int points = sleep.getPoints(now.plusDays(2));

        assertEquals(-6, points);
    }

    @Test
    public void getPoints_sleepTwentyFourHours_pointsIsThree()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(24));

        int points = sleep.getPoints(now.plusDays(1));

        assertEquals(3, points);
    }

    @Test
    public void getPoints_sleepOneDayThenWaitOneDay_pointsIsMinus4()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(24));

        assertEquals(-4, sleep.getPoints(now.plusHours(48)));
    }

    @Test
    public void getPercent_sleepToHundredThenWaitADay_()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(24));

        assertEquals(46, sleep.getPercent(now.plusHours(48)));
    }

    @Test
    public void getState_sleepInitialized_stateIsAwake()
    {
        assertEquals(Sleep.AWAKE, sleep.getState());
    }

    @Test
    public void getState_firstToggle_stateIsAsleep()
    {
        sleep.toggle(now);

        assertEquals(Sleep.ASLEEP, sleep.getState());
    }

    @Test
    public void getState_secondToggle_stateIsAwake()
    {
        sleep.toggle(now);
        sleep.toggle(now);

        assertEquals(Sleep.AWAKE, sleep.getState());
    }

    @Test
    public void getState_thirdToggle_stateIsAsleep()
    {
        sleep.toggle(now);
        sleep.toggle(now);
        sleep.toggle(now);

        assertEquals(Sleep.ASLEEP, sleep.getState());
    }

    @Test
    public void isOnTrack_sleepInitialized_isOnTrackIsTrue()
    {
        boolean isOnTrack = sleep.isOnTrack(now);

        assertTrue(isOnTrack);
    }

    @Test
    public void isOnTrack_secondDayWithoutSleep_isOnTrackIsFalse()
    {
        boolean isOnTrack = sleep.isOnTrack(now.plusDays(2));

        assertFalse(isOnTrack);
    }
}
