package stoneframe.serena.model.balancers;

import android.util.Pair;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.LinkedList;
import java.util.List;

class BalancerData
{
    final List<CustomExpenditureType> expenditureTypes = new LinkedList<>();
    final List<Pair<Expenditure, LocalDateTime>> expenditures = new LinkedList<>();

    String name;
    String unit;

    int previousExpenditure;

    LocalDate startDate;
    int incrementPerDay;

    Integer maxValue;

    boolean allowQuick;

    BalancerData(
        String name,
        String unit,
        int previousExpenditure,
        LocalDate startDate,
        int incrementPerDay,
        Integer maxValue,
        boolean allowQuick)
    {
        this.name = name;
        this.unit = unit;
        this.previousExpenditure = previousExpenditure;
        this.startDate = startDate;
        this.incrementPerDay = incrementPerDay;
        this.maxValue = maxValue;
        this.allowQuick = allowQuick;
    }
}
