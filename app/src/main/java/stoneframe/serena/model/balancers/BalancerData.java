package stoneframe.serena.model.balancers;

import android.util.Pair;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.LinkedList;
import java.util.List;

class BalancerData
{
    final List<CustomTransactionType> transactionTypes = new LinkedList<>();
    final List<Pair<Transaction, LocalDateTime>> transactions = new LinkedList<>();

    String name;
    String unit;

    int previousTransactions;

    LocalDate startDate;
    int changePerDay;

    Integer maxValue;
    Integer minValue;

    boolean allowQuick;

    boolean isEnabled;

    BalancerData(
        String name,
        String unit,
        int previousTransactions,
        LocalDate startDate,
        int changePerDay,
        Integer maxValue,
        Integer minValue,
        boolean allowQuick,
        boolean isEnabled)
    {
        this.name = name;
        this.unit = unit;
        this.previousTransactions = previousTransactions;
        this.startDate = startDate;
        this.changePerDay = changePerDay;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.allowQuick = allowQuick;
        this.isEnabled = isEnabled;
    }
}
