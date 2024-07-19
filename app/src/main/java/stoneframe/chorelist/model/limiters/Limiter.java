package stoneframe.chorelist.model.limiters;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Limiter
{
    private static final double MINUTES_PER_DAY = 24 * 60;

    private final List<CustomExpenditureType> expenditureTypes = new LinkedList<>();
    private final List<Expenditure> expenditures = new LinkedList<>();

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

    public void setName(String name)
    {
        this.name = name;
    }

    public String getUnit()
    {
        return unit != null ? unit : "";
    }

    public void setUnit(String unit)
    {
        this.unit = unit;
    }

    public void setStartDate(LocalDate startDate)
    {
        this.startDate = startDate;
    }

    public int getIncrementPerDay()
    {
        return incrementPerDay;
    }

    public void setIncrementPerDay(LocalDateTime now, int incrementPerDay)
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
            .mapToInt(Expenditure::getAmount)
            .sum();

        return (int)(incrementPerDay * minutes.getMinutes() / MINUTES_PER_DAY)
            - previousExpenditure
            - recentExpenditures;
    }

    public void addExpenditureType(CustomExpenditureType expenditureType)
    {
        expenditureTypes.add(expenditureType);
    }

    public void removeExpenditureType(CustomExpenditureType expenditureType)
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

    public void addExpenditure(Expenditure expenditure)
    {
        clearOldExpenditures(expenditure.getTime());

        expenditures.add(expenditure);
    }

    public void removeExpenditure(Expenditure expenditure)
    {
        expenditures.remove(expenditure);
    }

    public List<Expenditure> getExpenditures()
    {
        return Collections.unmodifiableList(expenditures);
    }

    private void clearOldExpenditures(LocalDateTime now)
    {
        List<Expenditure> expendituresOlderThanOneDay = expenditures.stream()
            .filter(c -> c.getTime().isBefore(now.minusDays(1)))
            .collect(Collectors.toList());

        previousExpenditure += expendituresOlderThanOneDay.stream()
            .mapToInt(Expenditure::getAmount)
            .sum();

        expenditures.removeAll(expendituresOlderThanOneDay);
    }
}
