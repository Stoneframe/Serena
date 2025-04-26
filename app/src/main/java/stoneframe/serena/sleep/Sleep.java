package stoneframe.serena.sleep;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

public class Sleep
{
    public static final int AWAKE = 1;
    public static final int ASLEEP = 2;

    private static final double MINUTES_PER_POINT = 24d * 60d / 7d;
    private static final double MINUTES_PER_HOUR = 60;

    private static final int MAXIMUM_VALUE = 3;
    private static final int MINIMUM_VALUE = -10;

    private LocalDateTime startDateTime;

    private int state;

    private int minutesSlept;

    private LocalDateTime startSleep;

    Sleep(LocalDateTime now)
    {
        startDateTime = now;
        state = AWAKE;
    }

    int getState()
    {
        return state;
    }

    boolean isOnTrack(LocalDateTime now)
    {
        return calculateCurrentPoints(now.toLocalDate()
            .plusDays(1)
            .toLocalDateTime(LocalTime.MIDNIGHT)) > -9;
    }

    int getPoints(LocalDateTime now)
    {
        int currentPoints = calculateCurrentPoints(now);

        if (currentPoints < MINIMUM_VALUE)
        {
            updateStartTimeToMatchTargetValue(now, MINIMUM_VALUE);

            currentPoints = calculateCurrentPoints(now);
        }

        if (currentPoints > MAXIMUM_VALUE)
        {
            updateStartTimeToMatchTargetValue(now, MAXIMUM_VALUE);

            currentPoints = calculateCurrentPoints(now);
        }

        return currentPoints;
    }

    void toggle(LocalDateTime now)
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

    private int calculateCurrentPoints(LocalDateTime now)
    {
        double pointsLost = -getTotalMinutes(now) / MINUTES_PER_POINT;
        double pointsAcquired = minutesSlept / MINUTES_PER_HOUR;

        return (int)Math.round(pointsLost + pointsAcquired);
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

    private void updateStartTimeToMatchTargetValue(LocalDateTime now, int targetValue)
    {
        double totalMinutes = -(targetValue - minutesSlept / MINUTES_PER_HOUR) * MINUTES_PER_POINT;

        startDateTime = now.minusMinutes((int)totalMinutes);
    }
}
