package stoneframe.serena.sleep;

import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

public class Sleep
{
    public static final int AWAKE = 1;
    public static final int ASLEEP = 2;

    private static final double MINUTES_PER_POINT = 24 * 60 / 7;

    private final LocalDateTime startDateTime;

    private int state;

    private int minutesSlept;

    private LocalDateTime startSleep;

    public Sleep(LocalDateTime now)
    {
        startDateTime = now;
        state = AWAKE;
    }

    public int getState()
    {
        return state;
    }

    public int getPoints(LocalDateTime now)
    {
        int pointsLost = -(int)(getTotalMinutes(now) / MINUTES_PER_POINT);
        int pointsAcquired = minutesSlept / 60;

        int currentPoints = pointsLost + pointsAcquired;

        if (currentPoints < -5)
        {
            return -5;
        }

        return currentPoints;
    }

    public void toggle(LocalDateTime now)
    {
        if (state == AWAKE)
        {
            startSleep(now);
        }
        else
        {
            stopSleep(now);
        }
    }

    private int getTotalMinutes(LocalDateTime now)
    {
        return Minutes.minutesBetween(startDateTime, now).getMinutes();
    }

    private void startSleep(LocalDateTime now)
    {
        state = ASLEEP;

        startSleep = now;
    }

    private void stopSleep(LocalDateTime now)
    {
        state = AWAKE;

        minutesSlept += Minutes.minutesBetween(startSleep, now).getMinutes();
    }
}
