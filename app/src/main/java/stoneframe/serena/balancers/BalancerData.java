package stoneframe.serena.balancers;

import org.joda.time.LocalDate;

import java.util.LinkedList;
import java.util.List;

class BalancerData
{
    final List<CustomTransactionType> transactionTypes = new LinkedList<>();

    String name;
    String unit;

    int previousTransactions;

    LocalDate startDate;
    int changePerInterval;
    int intervalType;

    Integer maxValue;
    Integer minValue;

    boolean clearInputOnAdd;

    boolean allowQuick;

    boolean isEnabled;

    BalancerData(
        String name,
        String unit,
        int previousTransactions,
        LocalDate startDate,
        int changePerInterval,
        int intervalType,
        Integer maxValue,
        Integer minValue,
        boolean clearInputOnAdd,
        boolean allowQuick,
        boolean isEnabled)
    {
        this.name = name;
        this.unit = unit;
        this.previousTransactions = previousTransactions;
        this.startDate = startDate;
        this.changePerInterval = changePerInterval;
        this.intervalType = intervalType;
        this.maxValue = maxValue;
        this.minValue = minValue;
        this.clearInputOnAdd = clearInputOnAdd;
        this.allowQuick = allowQuick;
        this.isEnabled = isEnabled;
    }
}
