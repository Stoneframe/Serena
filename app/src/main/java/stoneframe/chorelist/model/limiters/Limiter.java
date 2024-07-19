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

    public Limiter(String name, LocalDate startDate, int incrementPerDay)
    {
        this.name = name;
        this.startDate = startDate;
        this.incrementPerDay = incrementPerDay;
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

    public int getAvailable(LocalDateTime now)
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

    void addExpenditureType(CustomExpenditureType expenditureType)
    {
        expenditureTypes.add(expenditureType);
    }

    void removeExpenditureType(CustomExpenditureType expenditureType)
    {
        expenditureTypes.remove(expenditureType);
    }

    public List<ExpenditureType> getExpenditureTypes()
    {
        return Stream.concat(
                Stream.of(new QuickExpenditureType()),
                expenditureTypes.stream().sorted(Comparator.comparing(ExpenditureType::getName)))
            .collect(Collectors.toList());
    }

    void addExpenditure(Expenditure expenditure, LocalDateTime now)
    {
        clearOldExpenditures(now);

        expenditures.add(new Pair<>(expenditure, now));
    }

    void removeExpenditure(Expenditure expenditure)
    {
        expenditures.removeIf(p -> p.first.equals(expenditure));
    }

    public List<Expenditure> getExpenditures()
    {
        return expenditures.stream().map(p -> p.first).collect(Collectors.toList());
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
}
