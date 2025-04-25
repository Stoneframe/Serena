package stoneframe.serena.sleep;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDateTime;
import org.junit.Test;

public class SleepTest
{
    @Test
    public void getPoints_sleepInitialized_pointsAreZero()
    {
        Sleep sleep = new Sleep();

        int points = sleep.getPoints(LocalDateTime.now());

        assertEquals(0, points);
    }

    @Test
    public void getPoints_oneDayAfterInitialization_pointsIsMinusEight()
    {
        Sleep sleep = new Sleep();

        int points = sleep.getPoints(LocalDateTime.now().plusDays(1));

        assertEquals(-8, points);
    }

    @Test
    public void getPoints_sleepEightHours_pointsIsZeroOneDayLater()
    {
        Sleep sleep = new Sleep();

        sleep.toggle(LocalDateTime.now().plusHours(0));
        sleep.toggle(LocalDateTime.now().plusHours(8));

        int points = sleep.getPoints(LocalDateTime.now().plusDays(1));

        assertEquals(0, points);
    }

    @Test
    public void getPoints_sleepFourHours_pointsIsMinusFourOneDayLater()
    {
        Sleep sleep = new Sleep();

        sleep.toggle(LocalDateTime.now().plusHours(0));
        sleep.toggle(LocalDateTime.now().plusHours(4));

        int points = sleep.getPoints(LocalDateTime.now().plusDays(1));

        assertEquals(-4, points);
    }


    @Test
    public void getPoints_sleepFourHoursTwice_pointsIsZeroOneDayLater()
    {
        Sleep sleep = new Sleep();

        sleep.toggle(LocalDateTime.now().plusHours(0));
        sleep.toggle(LocalDateTime.now().plusHours(4));

        sleep.toggle(LocalDateTime.now().plusHours(6));
        sleep.toggle(LocalDateTime.now().plusHours(10));

        int points = sleep.getPoints(LocalDateTime.now().plusDays(1));

        assertEquals(0, points);
    }

    @Test
    public void getState_sleepInitialized_stateIsAwake()
    {
        Sleep sleep = new Sleep();

        assertEquals(Sleep.AWAKE, sleep.getState());
    }

    @Test
    public void getState_firstToggle_stateIsAsleep()
    {
        Sleep sleep = new Sleep();

        sleep.toggle(LocalDateTime.now());

        assertEquals(Sleep.ASLEEP, sleep.getState());
    }

    @Test
    public void getState_secondToggle_stateIsAwake()
    {
        Sleep sleep = new Sleep();

        sleep.toggle(LocalDateTime.now());
        sleep.toggle(LocalDateTime.now());

        assertEquals(Sleep.AWAKE, sleep.getState());
    }

    @Test
    public void getState_thirdToggle_stateIsAsleep()
    {
        Sleep sleep = new Sleep();

        sleep.toggle(LocalDateTime.now());
        sleep.toggle(LocalDateTime.now());
        sleep.toggle(LocalDateTime.now());

        assertEquals(Sleep.ASLEEP, sleep.getState());
    }
}
