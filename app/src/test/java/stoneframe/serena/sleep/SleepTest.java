package stoneframe.serena.sleep;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;

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

    @Test
    public void updateSession_validTimes_replacesSessionInSamePosition()
    {
        sleep.addSession(now, now.plusHours(1));
        sleep.addSession(now.plusHours(2), now.plusHours(3));

        Sleep.SleepSession firstSession = sleep.getSessions(now).get(0);
        sleep.updateSession(firstSession, now.plusMinutes(15), now.plusHours(2));

        assertEquals(now.plusMinutes(15), sleep.getSessions(now).get(0).getStartTime());
        assertEquals(now.plusHours(2), sleep.getSessions(now).get(0).getStopTime());
        assertEquals(now.plusHours(2), sleep.getSessions(now).get(1).getStartTime());
    }

    @Test
    public void addSession_stopAfterStart_addsSession()
    {
        sleep.addSession(now, now.plusHours(1));

        assertEquals(1, sleep.getSessions(now).size());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSession_stopSameAsStart_throws()
    {
        sleep.addSession(now, now);
    }

    @Test(expected = IllegalArgumentException.class)
    public void addSession_stopBeforeStart_throws()
    {
        sleep.addSession(now, now.minusMinutes(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateSession_stopNotAfterStart_throws()
    {
        sleep.addSession(now, now.plusHours(1));

        Sleep.SleepSession session = sleep.getSessions(now).get(0);
        sleep.updateSession(session, now.plusHours(1), now.plusHours(1));
    }

    @Test
    public void removeSession_multipleSessions_removesSelectedSession()
    {
        sleep.addSession(now, now.plusHours(1));
        sleep.addSession(now.plusHours(2), now.plusHours(3));

        Sleep.SleepSession retainedSession = sleep.getSessions(now).get(0);
        Sleep.SleepSession removedSession = sleep.getSessions(now).get(1);
        sleep.removeSession(removedSession);

        assertEquals(1, sleep.getSessions(now).size());
        assertSame(retainedSession, sleep.getSessions(now).get(0));
        assertFalse(sleep.getSessions(now).contains(removedSession));
    }

    @Test
    public void getSessions_sessionEndedBeforeThreeDayWindow_removesOldSession()
    {
        sleep.addSession(now, now.plusHours(1));
        sleep.addSession(now.plusDays(4), now.plusDays(4).plusHours(1));

        assertEquals(1, sleep.getSessions(now.plusDays(4)).size());
        assertEquals(
            now.plusDays(4),
            sleep.getSessions(now.plusDays(4)).get(0).getStartTime());
    }

    @Test
    public void updateSession_changedDuration_recalculatesPercent()
    {
        sleep.addSession(now, now.plusHours(8));

        Sleep.SleepSession session = sleep.getSessions(now).get(0);
        assertEquals(100, sleep.getPercent(now.plusHours(24)));

        sleep.updateSession(session, now, now.plusHours(7));

        assertEquals(0, sleep.getPercent(now.plusHours(24)));
    }
}
