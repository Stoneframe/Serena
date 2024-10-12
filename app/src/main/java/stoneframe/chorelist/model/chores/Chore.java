package stoneframe.chorelist.model.chores;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

import java.util.Comparator;

public class Chore
{
    public static final int DAYS = 0;
    public static final int WEEKS = 1;
    public static final int MONTHS = 2;
    public static final int YEARS = 3;

    private boolean isEnabled;

    private LocalDate next;
    private LocalDate postpone;

    private String description;

    private int priority;
    private int effort;

    private int intervalUnit;
    private int intervalLength;

    Chore(
        String description,
        int priority,
        int effort,
        LocalDate next,
        int intervalLength,
        int intervalUnit)
    {
        this.description = description;
        this.priority = priority;
        this.effort = effort;

        this.next = next;
        this.postpone = null;
        this.intervalUnit = intervalUnit;
        this.intervalLength = intervalLength;

        isEnabled = true;
    }

    public boolean isEnabled()
    {
        return isEnabled;
    }

    public LocalDate getNext()
    {
        return new LocalDate(next);
    }

    public String getDescription()
    {
        return description;
    }

    public int getPriority()
    {
        return priority;
    }

    public int getEffort()
    {
        return effort;
    }

    public int getIntervalUnit()
    {
        return intervalUnit;
    }

    public int getIntervalLength()
    {
        return intervalLength;
    }

    public void reschedule(LocalDate today)
    {
        if (next == null)
        {
            next = today;
        }

        while (!next.isAfter(today))
        {
            switch (intervalUnit)
            {
                case DAYS:
                    next = today.plusDays(intervalLength);
                    break;
                case WEEKS:
                    next = next.plusWeeks(intervalLength);
                    break;
                case MONTHS:
                    next = next.plusMonths(intervalLength);
                    break;
                case YEARS:
                    next = next.plusYears(intervalLength);
                    break;
                default:
                    return;
            }
        }

        postpone = null;
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
        return description;
    }

    void postpone(LocalDate today)
    {
        postpone = today.plusDays(1);
    }

    void setEnabled(boolean enabled)
    {
        isEnabled = enabled;
    }

    void setNext(LocalDate next)
    {
        this.next = next;
        this.postpone = null;
    }

    void setDescription(String description)
    {
        this.description = description;
    }

    void setPriority(int priority)
    {
        this.priority = priority;
    }

    void setEffort(int effort)
    {
        this.effort = effort;
    }

    void setIntervalUnit(int intervalUnit)
    {
        this.intervalUnit = intervalUnit;
    }

    void setIntervalLength(int intervalLength)
    {
        this.intervalLength = intervalLength;
    }

    boolean isTimeToDo(LocalDate today)
    {
        return !getNextOrPostpone().isAfter(today);
    }

    private LocalDate getNextOrPostpone()
    {
        return postpone == null ? next : postpone;
    }

    public static class ChoreComparator implements Comparator<Chore>
    {
        private final LocalDate today;

        public ChoreComparator(LocalDate today)
        {
            this.today = today;
        }

        @Override
        public int compare(Chore o1, Chore o2)
        {
            int i;

            if (!o1.getNextOrPostpone().isAfter(today) && !o2.getNextOrPostpone().isAfter(today))
            {
                if ((i = Integer.compare(o1.priority, o2.priority)) != 0) return i;
                if ((i = Integer.compare(o1.effort, o2.effort)) != 0) return i;
            }
            if ((i = o1.getNextOrPostpone().compareTo(o2.getNextOrPostpone())) != 0) return i;

            return o1.description.compareTo(o2.description);
        }
    }
}
