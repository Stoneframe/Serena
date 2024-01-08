package stoneframe.chorelist.model.efforttrackers;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import stoneframe.chorelist.model.EffortTracker;

public class WeeklyEffortTracker implements EffortTracker
{
    private DateTime previous;

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
    public int getTodaysEffort(DateTime now)
    {
        if (!isSameDay(previous, now))
        {
            previous = now;
            switch (now.getDayOfWeek())
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

    private boolean isSameDay(DateTime d1, DateTime d2)
    {
        return d1 != null && d2 != null
            && d1.getYear() == d2.getYear()
            && d1.getMonthOfYear() == d2.getMonthOfYear()
            && d1.getDayOfMonth() == d2.getDayOfMonth();
    }
}
