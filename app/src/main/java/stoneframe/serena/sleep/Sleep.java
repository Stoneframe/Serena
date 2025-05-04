package stoneframe.serena.sleep;

import androidx.annotation.Nullable;

import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import java.util.LinkedList;
import java.util.List;

public class Sleep
{
    public static final int AWAKE = 1;
    public static final int ASLEEP = 2;

    private static final int MAX_VALUE = 100;
    private static final int MIN_VALUE = 87;

    private final List<SleepSession> sleepSessions = new LinkedList<>();

    private LocalDateTime startDateTime;
    private int state;

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

    int getPercent(LocalDateTime now)
    {
        if (startDateTime.isBefore(now.minusDays(3)))
        {
            startDateTime = now.minusDays(3);
        }

        double expectedMinutesSlept = Minutes.minutesBetween(startDateTime, now)
            .getMinutes() * (8d / 24d);
        int minuteSlept = getMinutesSleptLastThreeDays();

        double value = minuteSlept / expectedMinutesSlept * 100;

        if (value > MAX_VALUE) return 100;
        if (value < MIN_VALUE) return 0;

        return (int)((value - MIN_VALUE) / (MAX_VALUE - MIN_VALUE) * 100);
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

    @Nullable
    SleepSession getPreviousSession()
    {
        if (sleepSessions.isEmpty())
        {
            return null;
        }

        return sleepSessions.get(sleepSessions.size() - 1);
    }

    private void startSleep(LocalDateTime now)
    {
        state = ASLEEP;

        startSleep = now;
    }

    private void stopSleep(LocalDateTime now)
    {
        state = AWAKE;

        SleepSession session = new SleepSession(startSleep, now);

        sleepSessions.add(session);
    }

    private int getMinutesSleptLastThreeDays()
    {
        sleepSessions.removeIf(s -> s.isPassed(startDateTime));

        return sleepSessions.stream()
            .mapToInt(s -> s.getMinutes(startDateTime))
            .sum();
    }

    public static class SleepSession
    {
        private final LocalDateTime startTime;
        private final LocalDateTime stopTime;

        public SleepSession(LocalDateTime startTime, LocalDateTime stopTime)
        {
            this.startTime = startTime;
            this.stopTime = stopTime;
        }

        public LocalDateTime getStartTime()
        {
            return startTime;
        }

        public LocalDateTime getStopTime()
        {
            return stopTime;
        }

        public Minutes getSleepTime()
        {
            return Minutes.minutesBetween(startTime, stopTime);
        }

        boolean isPassed(LocalDateTime now)
        {
            return now.isAfter(stopTime);
        }

        int getMinutes(LocalDateTime now)
        {
            Minutes minutes = now.isAfter(startTime)
                ? Minutes.minutesBetween(now, stopTime)
                : Minutes.minutesBetween(startTime, stopTime);

            return minutes.getMinutes();
        }
    }
}
