package stoneframe.serena.model.balancers;

import android.util.Pair;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import stoneframe.serena.model.util.Revertible;

public class Balancer extends Revertible<BalancerData>
{
    private static final double MINUTES_PER_DAY = 24 * 60;

    Balancer(String name, LocalDate startDate, int changePerDay, boolean allowQuick)
    {
        super(new BalancerData(
            name,
            null,
            0,
            startDate,
            changePerDay,
            null,
            allowQuick));
    }

    public String getName()
    {
        return data().name;
    }

    public String getUnit()
    {
        return data().unit != null ? data().unit : "";
    }

    public int getChangePerDay()
    {
        return data().changePerDay;
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

    public int getMaxValue()
    {
        return data().maxValue != null ? data().maxValue : Integer.MAX_VALUE;
    }

    public LocalDateTime getReplenishTime(LocalDateTime now)
    {
        int available = getAvailable(now);

        if (available >= 0)
        {
            return now;
        }

        double remaining = Math.abs((double)available / getChangePerDay());

        int days = (int)remaining;

        double hours = 24 * (remaining - days);
        double minutes = 60 * (hours - (int)hours);

        return now.plusDays(days).plusHours((int)hours).plusMinutes((int)minutes);
    }

    public int getAvailable(LocalDateTime now)
    {
        int totalAvailable = getTotalAvailable(now);

        return data().maxValue != null ? Math.min(totalAvailable, data().maxValue) : totalAvailable;
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

    public List<Transaction> getTransactions()
    {
        return data().transactions.stream().map(p -> p.first).collect(Collectors.toList());
    }

    void setName(String name)
    {
        data().name = name;
    }

    void setUnit(String unit)
    {
        data().unit = unit;
    }

    void setChangePerDay(LocalDateTime now, int changePerDay)
    {
        int oldCurrentAvailable = getAvailable(now);

        data().startDate = now.toLocalDate();
        data().changePerDay = changePerDay;

        data().transactions.clear();
        data().previousTransactions = 0;

        int newCurrentAvailable = getAvailable(now);

        data().previousTransactions = newCurrentAvailable - oldCurrentAvailable;
    }

    void setMaxValue(Integer maxValue, LocalDateTime now)
    {
        data().maxValue = maxValue;

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

    void addTransaction(Transaction transaction, LocalDateTime now)
    {
        updatePreviousTransactions(now);
        clearOldTransactions(now);

        data().transactions.add(new Pair<>(transaction, now));
    }

    void removeTransaction(Transaction transaction)
    {
        data().transactions.removeIf(p -> p.first.equals(transaction));
    }

    private void updatePreviousTransactions(LocalDateTime now)
    {
        if (data().maxValue == null) return;

        int totalAvailable = getTotalAvailable(now);

        if (totalAvailable > data().maxValue)
        {
            data().previousTransactions += totalAvailable - data().maxValue;
        }
    }

    private void clearOldTransactions(LocalDateTime now)
    {
        List<Pair<Transaction, LocalDateTime>> transactionsOlderThanOneDay = data().transactions.stream()
            .filter(p -> p.second.isBefore(now.minusDays(1)))
            .collect(Collectors.toList());

        data().previousTransactions += transactionsOlderThanOneDay.stream()
            .mapToInt(p -> p.first.getAmount())
            .sum();

        data().transactions.removeAll(transactionsOlderThanOneDay);
    }

    private int getTotalAvailable(LocalDateTime now)
    {
        Minutes minutes = Minutes.minutesBetween(
            data().startDate.toLocalDateTime(LocalTime.MIDNIGHT),
            now);

        int recentTransactions = data().transactions.stream()
            .map(p -> p.first)
            .mapToInt(Transaction::getAmount)
            .sum();

        return (int)(data().changePerDay * minutes.getMinutes() / MINUTES_PER_DAY)
            + data().previousTransactions
            + recentTransactions;
    }
}
