package stoneframe.serena.sleep;

import androidx.annotation.Nullable;

import org.joda.time.LocalDateTime;
import org.joda.time.Minutes;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Sleep
{
    public static final int AWAKE = 1;
    public static final int ASLEEP = 2;

    private static final double DEFAULT_MIN_HOURS_SLEEP_PER_DAY = 7d;
    private static final double DEFAULT_MAX_HOURS_SLEEP_PER_DAY = 8d;

    private final List<SleepSession> sleepSessions = new LinkedList<>();

    private boolean isEnabled;
    private LocalDateTime startDateTime;
    private int state;
    private double minHoursSleepPerDay;
    private double maxHoursSleepPerDay;

    private LocalDateTime startSleep;

    Sleep(LocalDateTime now)
    {
        isEnabled = false;
        startDateTime = now;
        state = AWAKE;
        minHoursSleepPerDay = DEFAULT_MIN_HOURS_SLEEP_PER_DAY;
        maxHoursSleepPerDay = DEFAULT_MAX_HOURS_SLEEP_PER_DAY;
    }

    boolean isEnabled()
    {
        return isEnabled;
    }

    void setEnabled(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }

    int getState()
    {
        return state;
    }

    double getMinHoursSleepPerDay()
    {
        normalizeSleepRange();

        return minHoursSleepPerDay;
    }

    double getMaxHoursSleepPerDay()
    {
        normalizeSleepRange();

        return maxHoursSleepPerDay;
    }

    void setSleepRange(double minHoursSleepPerDay, double maxHoursSleepPerDay)
    {
        validateSleepRange(minHoursSleepPerDay, maxHoursSleepPerDay);

        this.minHoursSleepPerDay = minHoursSleepPerDay;
        this.maxHoursSleepPerDay = maxHoursSleepPerDay;
    }

    int getPercent(LocalDateTime now)
    {
        normalizeSleepRange();
        pruneSessions(now);

        int elapsedMinutes = Minutes.minutesBetween(startDateTime, now).getMinutes();

        if (elapsedMinutes <= 0)
        {
            return 0;
        }

        int minutesSlept = getMinutesSleptLastThreeDays();
        double minExpectedMinutes = getExpectedMinutesToSleep(now, minHoursSleepPerDay);
        double maxExpectedMinutes = getExpectedMinutesToSleep(now, maxHoursSleepPerDay);

        if (minutesSlept >= maxExpectedMinutes) return 100;
        if (minutesSlept <= minExpectedMinutes) return 0;

        return (int)((minutesSlept - minExpectedMinutes) / (maxExpectedMinutes - minExpectedMinutes) * 100);
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

    void addSession(LocalDateTime start, LocalDateTime end)
    {
        validateSessionTimes(start, end);

        sleepSessions.add(new SleepSession(start, end));
    }

    @Nullable
    SleepSession getPreviousSession(LocalDateTime now)
    {
        pruneSessions(now);

        if (sleepSessions.isEmpty())
        {
            return null;
        }

        return sleepSessions.get(sleepSessions.size() - 1);
    }

    List<SleepSession> getSessions(LocalDateTime now)
    {
        pruneSessions(now);

        return new ArrayList<>(sleepSessions);
    }

    void updateSession(SleepSession session, LocalDateTime start, LocalDateTime stop)
    {
        validateSessionTimes(start, stop);

        int index = sleepSessions.indexOf(session);

        if (index < 0)
        {
            throw new IllegalArgumentException("Sleep session was not found.");
        }

        sleepSessions.set(index, new SleepSession(start, stop));
    }

    void removeSession(SleepSession session)
    {
        sleepSessions.remove(session);
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

        startSleep = null;
    }

    private double getExpectedMinutesToSleep(LocalDateTime now, double hoursSleepPerDay)
    {
        double fractionOfDayToSleep = hoursSleepPerDay / 24d;

        return Minutes
            .minutesBetween(startDateTime, now)
            .getMinutes() * fractionOfDayToSleep;
    }

    private void normalizeSleepRange()
    {
        if (!isValidSleepRange(minHoursSleepPerDay, maxHoursSleepPerDay))
        {
            minHoursSleepPerDay = DEFAULT_MIN_HOURS_SLEEP_PER_DAY;
            maxHoursSleepPerDay = DEFAULT_MAX_HOURS_SLEEP_PER_DAY;
        }
    }

    private static void validateSleepRange(double minHoursSleepPerDay, double maxHoursSleepPerDay)
    {
        if (!isFinite(minHoursSleepPerDay) || !isFinite(maxHoursSleepPerDay))
        {
            throw new IllegalArgumentException("Sleep hours must be valid numbers.");
        }

        if (minHoursSleepPerDay < 0d)
        {
            throw new IllegalArgumentException("Minimum sleep hours must be zero or greater.");
        }

        if (maxHoursSleepPerDay <= minHoursSleepPerDay)
        {
            throw new IllegalArgumentException("Maximum sleep hours must be greater than minimum sleep hours.");
        }
    }

    private static boolean isValidSleepRange(double minHoursSleepPerDay, double maxHoursSleepPerDay)
    {
        return isFinite(minHoursSleepPerDay)
            && isFinite(maxHoursSleepPerDay)
            && minHoursSleepPerDay >= 0d
            && maxHoursSleepPerDay > minHoursSleepPerDay;
    }

    private static boolean isFinite(double value)
    {
        return !Double.isNaN(value) && !Double.isInfinite(value);
    }

    private int getMinutesSleptLastThreeDays()
    {
        return sleepSessions.stream()
            .mapToInt(s -> s.getMinutes(startDateTime))
            .sum();
    }

    private void pruneSessions(LocalDateTime now)
    {
        if (startDateTime.isBefore(now.minusDays(3)))
        {
            startDateTime = now.minusDays(3);
        }

        sleepSessions.removeIf(s -> s.isPassed(startDateTime));
    }

    private static void validateSessionTimes(LocalDateTime start, LocalDateTime stop)
    {
        if (start == null || stop == null || !stop.isAfter(start))
        {
            throw new IllegalArgumentException("Stop time must be after start time.");
        }
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
