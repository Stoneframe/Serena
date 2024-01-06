package stoneframe.chorelist.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

import java.util.Comparator;

public class Chore
{
    public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int MONTHLY = 2;
    public static final int YEARLY = 3;

    private DateTime next;

    private String description;
    private int priority;
    private int effort;

    private int intervalUnit;
    private int intervalLength;

    public Chore(
        String description,
        int priority,
        int effort,
        DateTime next,
        int intervalUnit,
        int intervalLength)
    {
        this.description = description;
        this.priority = priority;
        this.effort = effort;

        this.next = next;
        this.intervalUnit = intervalUnit;
        this.intervalLength = intervalLength;
    }

    public DateTime getNext()
    {
        return new DateTime(next);
    }

    public void setNext(DateTime next)
    {
        this.next = next;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public int getPriority()
    {
        return priority;
    }

    public void setPriority(int priority)
    {
        this.priority = priority;
    }

    public int getEffort()
    {
        return effort;
    }

    public void setEffort(int effort)
    {
        this.effort = effort;
    }

    public int getIntervalUnit()
    {
        return intervalUnit;
    }

    public void setIntervalUnit(int intervalUnit)
    {
        this.intervalUnit = intervalUnit;
    }

    public int getIntervalLength()
    {
        return intervalLength;
    }

    public void setIntervalLength(int intervalLength)
    {
        this.intervalLength = intervalLength;
    }

    public void reschedule(DateTime now)
    {
        if (next == null)
        {
            next = now;
        }

        while (!next.isAfter(now))
        {
            switch (intervalUnit)
            {
                case DAILY:
                    next = now.plusDays(intervalLength).withTimeAtStartOfDay();
                    break;
                case WEEKLY:
                    next = next.plusWeeks(intervalLength);
                    break;
                case MONTHLY:
                    next = next.plusMonths(intervalLength);
                    break;
                case YEARLY:
                    next = next.plusYears(intervalLength);
                    break;
                default:
                    return;
            }
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Chore))
        {
            return false;
        }

        Chore other = (Chore)obj;

        return this.description.equals(other.description);
    }

    @NonNull
    @Override
    public String toString()
    {
        return "- " + description;
    }

    public static class DutyComparator implements Comparator<Chore>
    {
        private final DateTime now;

        public DutyComparator(DateTime now)
        {
            this.now = now;
        }

        @Override
        public int compare(Chore o1, Chore o2)
        {
            int i;

            if (o1.next.isAfter(now) || o2.next.isAfter(now))
            {
                if ((i = o1.next.compareTo(o2.next)) != 0) return i;
            }
            else
            {
                if ((i = Integer.compare(o1.priority, o2.priority)) != 0) return i;
                if ((i = Integer.compare(o1.effort, o2.effort)) != 0) return i;
            }

            return o1.description.compareTo(o2.description);
        }
    }
}
