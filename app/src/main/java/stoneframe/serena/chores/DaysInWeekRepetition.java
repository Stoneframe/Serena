package stoneframe.serena.chores;

import androidx.annotation.NonNull;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.util.Arrays;

public class DaysInWeekRepetition extends Repetition
{
    DaysInWeekRepetition(ChoreData data)
    {
        super(DaysInWeek, data);
    }

    @Override
    public LocalDate getNext()
    {
        return data.next;
    }

    @Override
    public double getEffortPerWeek()
    {
        return data.effort * getNbrOfSelectedDays();
    }

    public boolean getMonday()
    {
        return data.monday;
    }

    public void setMonday(boolean monday)
    {
        data.monday = monday;
    }

    public boolean getTuesday()
    {
        return data.tuesday;
    }

    public void setTuesday(boolean tuesday)
    {
        data.tuesday = tuesday;
    }

    public boolean getWednesday()
    {
        return data.wednesday;
    }

    public void setWednesday(boolean wednesday)
    {
        data.wednesday = wednesday;
    }

    public boolean getThursday()
    {
        return data.thursday;
    }

    public void setThursday(boolean thursday)
    {
        data.thursday = thursday;
    }

    public boolean getFriday()
    {
        return data.friday;
    }

    public void setFriday(boolean friday)
    {
        data.friday = friday;
    }

    public boolean getSaturday()
    {
        return data.saturday;
    }

    public void setSaturday(boolean saturday)
    {
        data.saturday = saturday;
    }

    public boolean getSunday()
    {
        return data.sunday;
    }

    public void setSunday(boolean sunday)
    {
        data.sunday = sunday;
    }

    @Override
    void updateNext(LocalDate today)
    {
        if (getNbrOfSelectedDays() == 0)
        {
            data.next = LocalDate.now();
        }

        data.next = getNextSelectedDayAndIncludeToday(today);
        data.postpone = null;
    }

    @Override
    void reschedule(LocalDate today)
    {
        if (getNbrOfSelectedDays() == 0)
        {
            data.next = LocalDate.now();
        }

        data.next = getNextSelectedDayAndExcludeToday(today);
    }

    @Override
    double getFrequency()
    {
        return (double)getNbrOfSelectedDays() / 7;
    }

    private long getNbrOfSelectedDays()
    {
        return Arrays.stream(
                new Boolean[]{
                    getMonday(),
                    getTuesday(),
                    getWednesday(),
                    getThursday(),
                    getFriday(),
                    getSaturday(),
                    getSunday()
                })
            .filter(d -> d)
            .count();
    }

    private @NonNull LocalDate getNextSelectedDayAndIncludeToday(LocalDate today)
    {
        return getNextSelectedDay(today.plusDays(0));
    }

    private @NonNull LocalDate getNextSelectedDayAndExcludeToday(LocalDate today)
    {
        return getNextSelectedDay(today.plusDays(1));
    }

    private @NonNull LocalDate getNextSelectedDay(LocalDate date)
    {
        while (!isDayChecked(date.getDayOfWeek()))
        {
            date = date.plusDays(1);
        }

        return date;
    }

    private boolean isDayChecked(int dayOfWeek)
    {
        switch (dayOfWeek)
        {
            case DateTimeConstants.MONDAY:
                return getMonday();
            case DateTimeConstants.TUESDAY:
                return getTuesday();
            case DateTimeConstants.WEDNESDAY:
                return getWednesday();
            case DateTimeConstants.THURSDAY:
                return getThursday();
            case DateTimeConstants.FRIDAY:
                return getFriday();
            case DateTimeConstants.SATURDAY:
                return getSaturday();
            case DateTimeConstants.SUNDAY:
                return getSunday();
            default:
                return false;
        }
    }
}
