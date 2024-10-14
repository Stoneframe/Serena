package stoneframe.chorelist.model.chores;

import androidx.annotation.NonNull;

import org.joda.time.LocalDate;

import java.util.Comparator;

import stoneframe.chorelist.model.util.Revertible;

public class Chore extends Revertible<ChoreData>
{
    public static final int DAYS = 0;
    public static final int WEEKS = 1;
    public static final int MONTHS = 2;
    public static final int YEARS = 3;

    Chore(
        String description,
        int priority,
        int effort,
        LocalDate next,
        int intervalLength,
        int intervalUnit)
    {
        super(new ChoreData(
            true,
            next,
            null,
            description,
            priority,
            effort,
            intervalUnit,
            intervalLength));
    }

    public boolean isEnabled()
    {
        return data().isEnabled;
    }

    public LocalDate getNext()
    {
        return new LocalDate(data().next);
    }

    public String getDescription()
    {
        return data().description;
    }

    public int getPriority()
    {
        return data().priority;
    }

    public int getEffort()
    {
        return data().effort;
    }

    public int getIntervalUnit()
    {
        return data().intervalUnit;
    }

    public int getIntervalLength()
    {
        return data().intervalLength;
    }

    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof Chore))
        {
            return false;
        }

        Chore other = (Chore)obj;

        return data().description.equals(other.data().description);
    }

    @NonNull
    @Override
    public String toString()
    {
        return data().description;
    }

    void postpone(LocalDate today)
    {
        data().postpone = today.plusDays(1);
    }

    void setEnabled(boolean enabled)
    {
        data().isEnabled = enabled;
    }

    void setNext(LocalDate next)
    {
        data().next = next;
        data().postpone = null;
    }

    void setDescription(String description)
    {
        data().description = description;
    }

    void setPriority(int priority)
    {
        data().priority = priority;
    }

    void setEffort(int effort)
    {
        data().effort = effort;
    }

    void setIntervalUnit(int intervalUnit)
    {
        data().intervalUnit = intervalUnit;
    }

    void setIntervalLength(int intervalLength)
    {
        data().intervalLength = intervalLength;
    }

    boolean isTimeToDo(LocalDate today)
    {
        return !getNextOrPostpone().isAfter(today);
    }

    void reschedule(LocalDate today)
    {
        if (data().next == null)
        {
            data().next = today;
        }

        while (!data().next.isAfter(today))
        {
            switch (data().intervalUnit)
            {
                case DAYS:
                    data().next = today.plusDays(data().intervalLength);
                    break;
                case WEEKS:
                    data().next = data().next.plusWeeks(data().intervalLength);
                    break;
                case MONTHS:
                    data().next = data().next.plusMonths(data().intervalLength);
                    break;
                case YEARS:
                    data().next = data().next.plusYears(data().intervalLength);
                    break;
                default:
                    return;
            }
        }

        data().postpone = null;
    }

    private LocalDate getNextOrPostpone()
    {
        return data().postpone == null ? data().next : data().postpone;
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
                if ((i = Integer.compare(o1.data().priority, o2.data().priority)) != 0) return i;
                if ((i = Integer.compare(o1.data().effort, o2.data().effort)) != 0) return i;
            }
            if ((i = o1.getNextOrPostpone().compareTo(o2.getNextOrPostpone())) != 0) return i;

            return o1.data().description.compareTo(o2.data().description);
        }
    }
}
