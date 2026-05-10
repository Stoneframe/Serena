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
    public void getPercent_doNotSleep_percentIsZeroEndOfDay()
    {
        assertEquals(0, sleep.getPercent(now.plusHours(24)));
    }

    @Test
    public void getPercent_sleepSevenHours_percentIsSeventyWhenWakingUp()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(7));

        assertEquals(100, sleep.getPercent(now.plusHours(7)));
    }

    @Test
    public void getPercent_sleepEightHours_percentIsEightyWhenWakingUp()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(8));

        assertEquals(100, sleep.getPercent(now.plusHours(8)));
    }

    @Test
    public void getPercent_sleepSevenHours_percentIsZeroAtEndOfDay()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(7));

        assertEquals(0, sleep.getPercent(now.plusHours(24)));
    }

    @Test
    public void getPercent_sleepEightHours_percentIs100AtEndOfDay()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(8));

        assertEquals(100, sleep.getPercent(now.plusHours(24)));
    }

    @Test
    public void getPercent_sleepEightHoursForTwoDays_percentIs100WhenWakingUp()
    {
        sleep.toggle(now.plusDays(0).plusHours(0));
        sleep.toggle(now.plusDays(0).plusHours(8));

        sleep.toggle(now.plusDays(1).plusHours(0));
        sleep.toggle(now.plusDays(1).plusHours(8));

        assertEquals(100, sleep.getPercent(now.plusDays(1).plusHours(8)));
    }

    @Test
    public void getPercent_sleepSevenHoursForThreeDays_percentIsZeroAtEndOfDay()
    {
        sleep.toggle(now.plusDays(0).plusHours(0));
        sleep.toggle(now.plusDays(0).plusHours(7));

        sleep.toggle(now.plusDays(1).plusHours(0));
        sleep.toggle(now.plusDays(1).plusHours(7));

        sleep.toggle(now.plusDays(2).plusHours(0));
        sleep.toggle(now.plusDays(2).plusHours(7));

        assertEquals(0, sleep.getPercent(now.plusDays(3)));
    }

    @Test
    public void getPercent_sleepEightHoursForThreeDays_percentIs100AtEndOfDay()
    {
        sleep.toggle(now.plusDays(0).plusHours(0));
        sleep.toggle(now.plusDays(0).plusHours(8));

        sleep.toggle(now.plusDays(1).plusHours(0));
        sleep.toggle(now.plusDays(1).plusHours(8));

        sleep.toggle(now.plusDays(2).plusHours(0));
        sleep.toggle(now.plusDays(2).plusHours(8));

        assertEquals(100, sleep.getPercent(now.plusDays(3)));
    }

    @Test
    public void getPercent_sleepEightHoursForTwoDaysThenSevenForOneDay_percentIs66AtEndOfDay()
    {
        sleep.toggle(now.plusDays(0).plusHours(0));
        sleep.toggle(now.plusDays(0).plusHours(8));

        sleep.toggle(now.plusDays(1).plusHours(0));
        sleep.toggle(now.plusDays(1).plusHours(8));

        sleep.toggle(now.plusDays(2).plusHours(0));
        sleep.toggle(now.plusDays(2).plusHours(7));

        assertEquals(66, sleep.getPercent(now.plusDays(3)));
    }

    @Test
    public void getPercent_sleepEightHoursForTwoDaysThenSevenForTwoDay_percentIs33AtEndOfDay()
    {
        sleep.toggle(now.plusDays(0).plusHours(0));
        sleep.toggle(now.plusDays(0).plusHours(8));

        sleep.toggle(now.plusDays(1).plusHours(0));
        sleep.toggle(now.plusDays(1).plusHours(8));

        sleep.toggle(now.plusDays(2).plusHours(0));
        sleep.toggle(now.plusDays(2).plusHours(7));

        sleep.toggle(now.plusDays(3).plusHours(0));
        sleep.toggle(now.plusDays(3).plusHours(7));

        assertEquals(33, sleep.getPercent(now.plusDays(4)));
    }

    @Test
    public void getPercent_sleepEightHoursForTwoDaysThenSevenForThreeDay_percentIsZeroAtEndOfDay()
    {
        sleep.toggle(now.plusDays(0).plusHours(0));
        sleep.toggle(now.plusDays(0).plusHours(8));

        sleep.toggle(now.plusDays(1).plusHours(0));
        sleep.toggle(now.plusDays(1).plusHours(8));

        sleep.toggle(now.plusDays(2).plusHours(0));
        sleep.toggle(now.plusDays(2).plusHours(7));

        sleep.toggle(now.plusDays(3).plusHours(0));
        sleep.toggle(now.plusDays(3).plusHours(7));

        sleep.toggle(now.plusDays(4).plusHours(0));
        sleep.toggle(now.plusDays(4).plusHours(7));

        assertEquals(0, sleep.getPercent(now.plusDays(5)));
    }

    @Test
    public void getPercent_customSleepRange_scoreUsesConfiguredRange()
    {
        sleep.setSleepRange(6d, 10d);

        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(8));

        assertEquals(50, sleep.getPercent(now.plusHours(24)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSleepRange_minBelowZero_throws()
    {
        sleep.setSleepRange(-1d, 8d);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setSleepRange_maxSameAsMin_throws()
    {
        sleep.setSleepRange(7d, 7d);
    }

    @Test
    public void getPercent_sleepWholeDay_percentIs100EndOfNextDay()
    {
        sleep.toggle(now.plusHours(0));
        sleep.toggle(now.plusHours(24));

        assertEquals(100, sleep.getPercent(now.plusHours(48)));
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
