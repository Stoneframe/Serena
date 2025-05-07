package stoneframe.serena.balancers;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import stoneframe.serena.util.Revertible;

public class Balancer extends Revertible<BalancerData>
{
    public static final int LIMITER = -1;
    public static final int COUNTER = 0;
    public static final int ENHANCER = 1;

    public static final int DAILY = 0;
    public static final int WEEKLY = 1;
    public static final int MONTHLY = 2;
    public static final int YEARLY = 3;

    private static final double MINUTES_PER_DAY = 24 * 60;

    Balancer(String name, LocalDate startDate, int changePerInterval, boolean allowQuick)
    {
        super(new BalancerData(
            name,
            null,
            0,
            startDate,
            changePerInterval,
            Balancer.DAILY,
            null,
            null,
            allowQuick,
            true));
    }

    public int getType()
    {
        return Integer.compare(0, getChangePerInterval());
    }

    public String getName()
    {
        return data().name;
    }

    void setName(String name)
    {
        data().name = name;
    }

    public String getUnit()
    {
        return data().unit != null ? data().unit : "";
    }

    void setUnit(String unit)
    {
        data().unit = unit;
    }

    public int getChangePerInterval()
    {
        return data().changePerInterval;
    }

    public int getIntervalType()
    {
        return data().intervalType;
    }

    public boolean isQuickDisableable()
    {
        return !data().transactionTypes.isEmpty();
    }

    public boolean isQuickAllowed()
    {
        return data().allowQuick;
    }

    public boolean hasMaxValue()
    {
        return data().maxValue != null;
    }

    public boolean hasMinValue()
    {
        return data().minValue != null;
    }

    public int getMaxValue()
    {
        return data().maxValue != null ? data().maxValue : Integer.MAX_VALUE;
    }

    public int getMinValue()
    {
        return data().minValue != null ? data().minValue : Integer.MIN_VALUE;
    }

    public boolean isEnabled()
    {
        return data().isEnabled;
    }

    void setEnabled(boolean isEnabled)
    {
        this.data().isEnabled = isEnabled;
    }

    public boolean isAhead(LocalDateTime now)
    {
        return getAvailable(now) > 0;
    }

    public LocalDateTime getTimeToBoundary(LocalDateTime now)
    {
        return getTimeToValue(now, getBoundary());
    }

    public int getAvailable(LocalDateTime now)
    {
        int totalAvailable = getTotalAvailableNotAccountingForMinAndMaxValues(now);

        if (totalAvailable > getMaxValue()) return getMaxValue();
        if (totalAvailable < getMinValue()) return getMinValue();

        return totalAvailable;
    }

    public List<TransactionType> getTransactionTypes()
    {
        if (!data().allowQuick)
        {
            return data().transactionTypes.stream()
                .sorted(Comparator.comparing(TransactionType::getName))
                .collect(Collectors.toList());
        }

        return Stream.concat(
                Stream.of(new QuickTransactionType()),
                data().transactionTypes.stream().sorted(Comparator.comparing(TransactionType::getName)))
            .collect(Collectors.toList());
    }

    void setChangePerInterval(LocalDateTime now, int changePerInterval)
    {
        setPropertyAffectingAvailable(now, () -> data().changePerInterval = changePerInterval);
    }

    void setIntervalType(LocalDateTime now, int intervalType)
    {
        setPropertyAffectingAvailable(now, () -> data().intervalType = intervalType);
    }

    void setMaxValue(LocalDateTime now, Integer maxValue)
    {
        setPropertyAffectingAvailable(now, () -> data().maxValue = maxValue);

        updatePreviousTransactions(now);
    }

    void setMinValue(LocalDateTime now, Integer minValue)
    {
        setPropertyAffectingAvailable(now, () -> data().minValue = minValue);

        updatePreviousTransactions(now);
    }

    void setAllowQuick(boolean allowQuick)
    {
        data().allowQuick = allowQuick;
    }

    void addTransactionType(CustomTransactionType transactionType)
    {
        data().transactionTypes.add(transactionType);
    }

    void removeTransactionType(CustomTransactionType transactionType)
    {
        data().transactionTypes.remove(transactionType);
    }

    void addTransaction(int transactionAmount, LocalDateTime now)
    {
        updatePreviousTransactions(now);

        data().previousTransactions += transactionAmount;

        updatePreviousTransactions(now);
    }

    void reset(LocalDateTime now)
    {
        data().startDate = now.toLocalDate();
        data().previousTransactions = 0;

        int currentAvailable = getTotalAvailableNotAccountingForMinAndMaxValues(now);

        data().previousTransactions -= currentAvailable;
    }

    private void setPropertyAffectingAvailable(LocalDateTime now, Runnable propertyUpdate)
    {
        int oldCurrentAvailable = getAvailable(now);

        data().startDate = now.toLocalDate();

        propertyUpdate.run();

        data().previousTransactions = 0;

        int newCurrentAvailable = getAvailable(now);

        data().previousTransactions = oldCurrentAvailable - newCurrentAvailable;
    }

    private void updatePreviousTransactions(LocalDateTime now)
    {
        int totalAvailable = getTotalAvailableNotAccountingForMinAndMaxValues(now);

        if (totalAvailable > getMaxValue())
        {
            data().previousTransactions -= totalAvailable - data().maxValue;
        }
        else if (totalAvailable < getMinValue())
        {
            data().previousTransactions -= totalAvailable - data().minValue;
        }
    }

    private int getTotalAvailableNotAccountingForMinAndMaxValues(LocalDateTime now)
    {
        Minutes minutes = Minutes.minutesBetween(
            data().startDate.toLocalDateTime(LocalTime.MIDNIGHT),
            now);

        return (int)(data().changePerInterval * minutes.getMinutes() / getMinutesOfInterval())
            + data().previousTransactions;
    }

    private double getMinutesOfInterval()
    {
        switch (data().intervalType)
        {
            case DAILY:
                return MINUTES_PER_DAY;
            case WEEKLY:
                return MINUTES_PER_DAY * 7;
            case MONTHLY:
                return MINUTES_PER_DAY * 30;
            case YEARLY:
                return MINUTES_PER_DAY * 365;
            default:
                throw new IndexOutOfBoundsException("Unknown interval " + data().intervalType);
        }
    }

    private LocalDateTime getTimeToValue(LocalDateTime now, int value)
    {
        int available = getAvailable(now);

        if (getType() == LIMITER && available > value || getType() == ENHANCER && available < value)
        {
            return now;
        }

        double minutes = (value - data().previousTransactions) * getMinutesOfInterval() / getChangePerInterval();

        return data().startDate.toLocalDateTime(LocalTime.MIDNIGHT).plusMinutes((int)minutes);
    }

    private int getBoundary()
    {
        if (getType() == LIMITER)
        {
            return 1;
        }

        if (getType() == ENHANCER)
        {
            return 0;
        }

        throw new IllegalStateException();
    }
}
