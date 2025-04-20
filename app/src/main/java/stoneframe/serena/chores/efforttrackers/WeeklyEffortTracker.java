package stoneframe.serena.chores.efforttrackers;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import stoneframe.serena.chores.EffortTracker;

public class WeeklyEffortTracker implements EffortTracker
{
    private LocalDate previous;

    private int mon;
    private int tue;
    private int wed;
    private int thu;
    private int fri;
    private int sat;
    private int sun;

    private int remaining;

    public WeeklyEffortTracker(int mon, int tue, int wed, int thu, int fri, int sat, int sun)
    {
        this.mon = mon;
        this.tue = tue;
        this.wed = wed;
        this.thu = thu;
        this.fri = fri;
        this.sat = sat;
        this.sun = sun;
    }

    public int getMonday()
    {
        return mon;
    }

    public void setMonday(int mon)
    {
        this.mon = mon;
        if (previous.getDayOfWeek() == DateTimeConstants.MONDAY)
        {
            remaining = mon;
        }
    }

    public int getTuesday()
    {
        return tue;
    }

    public void setTuesday(int tue)
    {
        this.tue = tue;
        if (previous.getDayOfWeek() == DateTimeConstants.TUESDAY)
        {
            remaining = tue;
        }
    }

    public int getWednesday()
    {
        return wed;
    }

    public void setWednesday(int wed)
    {
        this.wed = wed;
        if (previous.getDayOfWeek() == DateTimeConstants.WEDNESDAY)
        {
            remaining = wed;
        }
    }

    public int getThursday()
    {
        return thu;
    }

    public void setThursday(int thu)
    {
        this.thu = thu;
        if (previous.getDayOfWeek() == DateTimeConstants.THURSDAY)
        {
            remaining = thu;
        }
    }

    public int getFriday()
    {
        return fri;
    }

    public void setFriday(int fri)
    {
        this.fri = fri;
        if (previous.getDayOfWeek() == DateTimeConstants.FRIDAY)
        {
            remaining = fri;
        }
    }

    public int getSaturday()
    {
        return sat;
    }

    public void setSaturday(int sat)
    {
        this.sat = sat;
        if (previous.getDayOfWeek() == DateTimeConstants.SATURDAY)
        {
            remaining = sat;
        }
    }

    public int getSunday()
    {
        return sun;
    }

    public void setSunday(int sun)
    {
        this.sun = sun;
        if (previous.getDayOfWeek() == DateTimeConstants.SUNDAY)
        {
            remaining = mon;
        }
    }

    @Override
    public int getTodaysEffort(LocalDate today)
    {
        if (!isSameDay(previous, today))
        {
            previous = today;
            switch (today.getDayOfWeek())
            {
                case DateTimeConstants.MONDAY:
                    remaining = mon;
                    break;
                case DateTimeConstants.TUESDAY:
                    remaining = tue;
                    break;
                case DateTimeConstants.WEDNESDAY:
                    remaining = wed;
                    break;
                case DateTimeConstants.THURSDAY:
                    remaining = thu;
                    break;
                case DateTimeConstants.FRIDAY:
                    remaining = fri;
                    break;
                case DateTimeConstants.SATURDAY:
                    remaining = sat;
                    break;
                case DateTimeConstants.SUNDAY:
                    remaining = sun;
                    break;
            }
        }

        return remaining;
    }

    @Override
    public void spend(int effort)
    {
        if (effort > remaining)
        {
            remaining = 0;
        }
        else
        {
            remaining -= effort;
        }
    }

    @Override
    public void reset(LocalDate today)
    {
        remaining = getEffortFor(today);
    }

    private int getEffortFor(LocalDate today)
    {
        switch (today.getDayOfWeek())
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
                throw new RuntimeException("Unexpected week day constant");
        }
    }

    private boolean isSameDay(LocalDate d1, LocalDate d2)
    {
        return d1 != null && d2 != null
            && d1.getYear() == d2.getYear()
            && d1.getMonthOfYear() == d2.getMonthOfYear()
            && d1.getDayOfMonth() == d2.getDayOfMonth();
    }
}
