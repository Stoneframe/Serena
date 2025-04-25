package stoneframe.serena.sleep;

import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

public class Sleep
{
    public static final int AWAKE = 1;
    public static final int ASLEEP = 2;

    private static final int MINUTES_PER_POINT = 180;

    private final LocalDateTime startDateTime;

    private int state;

    private LocalDateTime startSleep;

    private int acquiredPoints;

    public Sleep()
    {
        startDateTime = LocalDateTime.now();
        state = AWAKE;
    }

    public int getState()
    {
        return state;
    }

    public int getPoints(LocalDateTime now)
    {
        Minutes minutes = Minutes.minutesBetween(startDateTime, now);

        return -minutes.getMinutes() / MINUTES_PER_POINT + acquiredPoints;
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

    private void startSleep(LocalDateTime now)
    {
        state = ASLEEP;

        startSleep = now;
    }

    private void stopSleep(LocalDateTime now)
    {
        state = AWAKE;

        Minutes minutes = Minutes.minutesBetween(startSleep, now);

        acquiredPoints += minutes.getMinutes() / 60;
    }
}
