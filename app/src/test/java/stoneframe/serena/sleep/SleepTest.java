package stoneframe.serena.sleep;

import static org.junit.Assert.assertEquals;

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
    public void getPoints_oneDayAfterInitialization_pointsIsMinusFive()
    {
        int points = sleep.getPoints(now.plusDays(1));

        assertEquals(-5, points);
    }

    @Test
    public void getPoints_sleepEightHours_pointsIsOneOneDayLater()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(8));

        int points = sleep.getPoints(now.plusDays(1));

        assertEquals(1, points);
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

        assertEquals(1, points);
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

        assertEquals(-4, points);
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
}
