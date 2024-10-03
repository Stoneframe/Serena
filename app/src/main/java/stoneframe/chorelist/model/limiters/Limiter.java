package stoneframe.chorelist.model.limiters;

import android.util.Pair;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Limiter
{
    private static final double MINUTES_PER_DAY = 24 * 60;

    private final List<CustomExpenditureType> expenditureTypes = new LinkedList<>();
    private final List<Pair<Expenditure, LocalDateTime>> expenditures = new LinkedList<>();

    private String name;
    private String unit;

    private int previousExpenditure;

    private LocalDate startDate;
    private int incrementPerDay;

    private boolean hasMaxValue;
    private int maxValue;

    private boolean allowQuick;

    public Limiter(String name, LocalDate startDate, int incrementPerDay, boolean allowQuick)
    {
        this.name = name;
        this.startDate = startDate;
        this.incrementPerDay = incrementPerDay;
        this.allowQuick = allowQuick;
    }

    public String getName()
    {
        return name;
    }

    void setName(String name)
    {
        this.name = name;
    }

    public String getUnit()
    {
        return unit != null ? unit : "";
    }

    void setUnit(String unit)
    {
        this.unit = unit;
    }

    public int getIncrementPerDay()
    {
        return incrementPerDay;
    }

    public boolean isQuickDisableable()
    {
        return !expenditureTypes.isEmpty();
    }

    public boolean isQuickAllowed()
    {
        return allowQuick;
    }

    public boolean hasMaxValue()
    {
        return hasMaxValue;
    }

    void setHasMaxValue(boolean hasMaxValue)
    {
        this.hasMaxValue = hasMaxValue;

        if (!hasMaxValue)
        {
            maxValue = Integer.MAX_VALUE;
        }
    }

    public int getMaxValue()
    {
        return maxValue;
    }

    void setMaxValue(int maxValue, LocalDateTime now)
    {
        this.maxValue = maxValue;

        updatePreviousExpenditures(now);
    }

    public void setAllowQuick(boolean allowQuick)
    {
        this.allowQuick = allowQuick;
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

        return hasMaxValue ? Math.min(totalAvailable, maxValue) : totalAvailable;
    }

    public List<ExpenditureType> getExpenditureTypes()
    {
        if (!allowQuick)
        {
            return expenditureTypes.stream()
                .sorted(Comparator.comparing(ExpenditureType::getName))
                .collect(Collectors.toList());
        }

        return Stream.concat(
                Stream.of(new QuickExpenditureType()),
                expenditureTypes.stream().sorted(Comparator.comparing(ExpenditureType::getName)))
            .collect(Collectors.toList());
    }

    public List<Expenditure> getExpenditures()
    {
        return expenditures.stream().map(p -> p.first).collect(Collectors.toList());
    }

    void setIncrementPerDay(LocalDateTime now, int incrementPerDay)
    {
        int oldCurrentAvailable = getAvailable(now);

        this.startDate = now.toLocalDate();
        this.incrementPerDay = incrementPerDay;

        expenditures.clear();
        previousExpenditure = 0;

        int newCurrentAvailable = getAvailable(now);

        previousExpenditure = newCurrentAvailable - oldCurrentAvailable;
    }

    void addExpenditureType(CustomExpenditureType expenditureType)
    {
        expenditureTypes.add(expenditureType);
    }

    void removeExpenditureType(CustomExpenditureType expenditureType)
    {
        expenditureTypes.remove(expenditureType);
    }

    void addExpenditure(Expenditure expenditure, LocalDateTime now)
    {
        updatePreviousExpenditures(now);
        clearOldExpenditures(now);

        expenditures.add(new Pair<>(expenditure, now));
    }

    void removeExpenditure(Expenditure expenditure)
    {
        expenditures.removeIf(p -> p.first.equals(expenditure));
    }

    private void updatePreviousExpenditures(LocalDateTime now)
    {
        if (!hasMaxValue) return;

        int totalAvailable = getTotalAvailable(now);

        if (totalAvailable > maxValue)
        {
            previousExpenditure += totalAvailable - maxValue;
        }
    }

    private void clearOldExpenditures(LocalDateTime now)
    {
        List<Pair<Expenditure, LocalDateTime>> expendituresOlderThanOneDay = expenditures.stream()
            .filter(p -> p.second.isBefore(now.minusDays(1)))
            .collect(Collectors.toList());

        previousExpenditure += expendituresOlderThanOneDay.stream()
            .mapToInt(p -> p.first.getAmount())
            .sum();

        expenditures.removeAll(expendituresOlderThanOneDay);
    }

    private int getTotalAvailable(LocalDateTime now)
    {
        Minutes minutes = Minutes.minutesBetween(
            startDate.toLocalDateTime(LocalTime.MIDNIGHT),
            now);

        int recentExpenditures = expenditures.stream()
            .map(p -> p.first)
            .mapToInt(Expenditure::getAmount)
            .sum();

        return (int)(incrementPerDay * minutes.getMinutes() / MINUTES_PER_DAY)
            - previousExpenditure
            - recentExpenditures;
    }
}
