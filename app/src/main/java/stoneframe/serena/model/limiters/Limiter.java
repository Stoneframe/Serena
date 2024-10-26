package stoneframe.serena.model.limiters;

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

public class Limiter extends Revertible<LimiterData>
{
    private static final double MINUTES_PER_DAY = 24 * 60;

    Limiter(String name, LocalDate startDate, int incrementPerDay, boolean allowQuick)
    {
        super(new LimiterData(
            name,
            null,
            0,
            startDate,
            incrementPerDay,
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

    public int getIncrementPerDay()
    {
        return data().incrementPerDay;
    }

    public boolean isQuickDisableable()
    {
        return !data().expenditureTypes.isEmpty();
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

        double remaining = Math.abs((double)available / getIncrementPerDay());

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

    public List<ExpenditureType> getExpenditureTypes()
    {
        if (!data().allowQuick)
        {
            return data().expenditureTypes.stream()
                .sorted(Comparator.comparing(ExpenditureType::getName))
                .collect(Collectors.toList());
        }

        return Stream.concat(
                Stream.of(new QuickExpenditureType()),
                data().expenditureTypes.stream().sorted(Comparator.comparing(ExpenditureType::getName)))
            .collect(Collectors.toList());
    }

    public List<Expenditure> getExpenditures()
    {
        return data().expenditures.stream().map(p -> p.first).collect(Collectors.toList());
    }

    void setName(String name)
    {
        data().name = name;
    }

    void setUnit(String unit)
    {
        data().unit = unit;
    }

    void setIncrementPerDay(LocalDateTime now, int incrementPerDay)
    {
        int oldCurrentAvailable = getAvailable(now);

        data().startDate = now.toLocalDate();
        data().incrementPerDay = incrementPerDay;

        data().expenditures.clear();
        data().previousExpenditure = 0;

        int newCurrentAvailable = getAvailable(now);

        data().previousExpenditure = newCurrentAvailable - oldCurrentAvailable;
    }

    void setMaxValue(Integer maxValue, LocalDateTime now)
    {
        data().maxValue = maxValue;

        updatePreviousExpenditures(now);
    }

    void setAllowQuick(boolean allowQuick)
    {
        data().allowQuick = allowQuick;
    }

    void addExpenditureType(CustomExpenditureType expenditureType)
    {
        data().expenditureTypes.add(expenditureType);
    }

    void removeExpenditureType(CustomExpenditureType expenditureType)
    {
        data().expenditureTypes.remove(expenditureType);
    }

    void addExpenditure(Expenditure expenditure, LocalDateTime now)
    {
        updatePreviousExpenditures(now);
        clearOldExpenditures(now);

        data().expenditures.add(new Pair<>(expenditure, now));
    }

    void removeExpenditure(Expenditure expenditure)
    {
        data().expenditures.removeIf(p -> p.first.equals(expenditure));
    }

    private void updatePreviousExpenditures(LocalDateTime now)
    {
        if (data().maxValue == null) return;

        int totalAvailable = getTotalAvailable(now);

        if (totalAvailable > data().maxValue)
        {
            data().previousExpenditure += totalAvailable - data().maxValue;
        }
    }

    private void clearOldExpenditures(LocalDateTime now)
    {
        List<Pair<Expenditure, LocalDateTime>> expendituresOlderThanOneDay = data().expenditures.stream()
            .filter(p -> p.second.isBefore(now.minusDays(1)))
            .collect(Collectors.toList());

        data().previousExpenditure += expendituresOlderThanOneDay.stream()
            .mapToInt(p -> p.first.getAmount())
            .sum();

        data().expenditures.removeAll(expendituresOlderThanOneDay);
    }

    private int getTotalAvailable(LocalDateTime now)
    {
        Minutes minutes = Minutes.minutesBetween(
            data().startDate.toLocalDateTime(LocalTime.MIDNIGHT),
            now);

        int recentExpenditures = data().expenditures.stream()
            .map(p -> p.first)
            .mapToInt(Expenditure::getAmount)
            .sum();

        return (int)(data().incrementPerDay * minutes.getMinutes() / MINUTES_PER_DAY)
            - data().previousExpenditure
            - recentExpenditures;
    }
}
