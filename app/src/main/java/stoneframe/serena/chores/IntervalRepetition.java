package stoneframe.serena.chores;

import org.joda.time.LocalDate;

public class IntervalRepetition extends Repetition
{
    public static final int DAYS = 0;
    public static final int WEEKS = 1;
    public static final int MONTHS = 2;
    public static final int YEARS = 3;

    IntervalRepetition(ChoreData data)
    {
        super(Interval, data);
    }

    public LocalDate getNext()
    {
        return data.next;
    }

    public void setNext(LocalDate next)
    {
        data.next = next;
    }

    public int getIntervalUnit()
    {
        return data.intervalUnit;
    }

    public void setIntervalUnit(int intervalUnit)
    {
        data.intervalUnit = intervalUnit;
    }

    public int getIntervalLength()
    {
        return data.intervalLength;
    }

    public void setIntervalLength(int intervalLength)
    {
        data.intervalLength = intervalLength;
    }

    @Override
    public double getEffortPerWeek()
    {
        switch (data.intervalUnit)
        {
            case IntervalRepetition.DAYS:
                return (double)data.effort / data.intervalLength * 7;
            case IntervalRepetition.WEEKS:
                return (double)data.effort / data.intervalLength;
            case IntervalRepetition.MONTHS:
                return (double)data.effort / data.intervalLength / 30 * 7;
            case IntervalRepetition.YEARS:
                return (double)data.effort / data.intervalLength / 365 * 7;
            default:
                throw new IllegalStateException("Unknown interval unit " + data.intervalUnit);
        }
    }

    @Override
    void updateNext(LocalDate today)
    {
        if (data.next == null)
        {
            data.next = today;
        }
    }

    @Override
    void reschedule(LocalDate today)
    {
        while (!data.next.isAfter(today))
        {
            switch (data.intervalUnit)
            {
                case DAYS:
                    data.next = today.plusDays(data.intervalLength);
                    break;
                case WEEKS:
                    data.next = data.next.plusWeeks(data.intervalLength);
                    break;
                case MONTHS:
                    data.next = data.next.plusMonths(data.intervalLength);
                    break;
                case YEARS:
                    data.next = data.next.plusYears(data.intervalLength);
                    break;
                default:
                    return;
            }
        }
    }

    @Override
    double getFrequency()
    {
        switch (data.intervalUnit)
        {
            case IntervalRepetition.DAYS:
                return 7d / data.intervalLength;
            case IntervalRepetition.WEEKS:
                return 1d / data.intervalLength;
            case IntervalRepetition.MONTHS:
                return 7d / 30d / data.intervalLength;
            case IntervalRepetition.YEARS:
                return 7d / 365d / data.intervalLength;
            default:
                throw new IllegalStateException("Unknown interval unit " + data.intervalUnit);
        }
    }
}
