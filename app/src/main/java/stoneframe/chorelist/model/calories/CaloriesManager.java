package stoneframe.chorelist.model.calories;

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

public class CaloriesManager
{
    private static final double MINUTES_PER_DAY = 24 * 60;

    private final List<CustomCalorieConsumptionType> consumptionTypes = new LinkedList<>();
    private final List<CalorieConsumption> consumptions = new LinkedList<>();

    private int previousConsumption;

    private LocalDate startDate;
    private int incrementPerDay;

    public CaloriesManager(LocalDate startDate, int incrementPerDay)
    {
        this.startDate = startDate;
        this.incrementPerDay = incrementPerDay;
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

        consumptions.clear();
        previousConsumption = 0;

        int newCurrentAvailable = getAvailable(now);

        previousConsumption = newCurrentAvailable - oldCurrentAvailable;
    }

    public int getAvailable(LocalDateTime now)
    {
        Minutes minutes = Minutes.minutesBetween(
            startDate.toLocalDateTime(LocalTime.MIDNIGHT),
            now);

        int recentConsumption = consumptions.stream()
            .mapToInt(CalorieConsumption::getCalories)
            .sum();

        return (int)(incrementPerDay * minutes.getMinutes() / MINUTES_PER_DAY)
            - previousConsumption
            - recentConsumption;
    }

    public void addConsumptionType(CustomCalorieConsumptionType consumptionType)
    {
        consumptionTypes.add(consumptionType);
    }

    public void removeConsumptionType(CustomCalorieConsumptionType consumptionType)
    {
        consumptionTypes.remove(consumptionType);
    }

    public List<CalorieConsumptionType> getConsumptionTypes()
    {
        return Stream.concat(
                Stream.of(new QuickCalorieConsumptionType()),
                consumptionTypes.stream().sorted(Comparator.comparing(CalorieConsumptionType::getName)))
            .collect(Collectors.toList());
    }

    public void addConsumption(CalorieConsumption consumption)
    {
        clearOldConsumptions(consumption.getTime());

        consumptions.add(consumption);
    }

    public void removeConsumption(CalorieConsumption consumption)
    {
        consumptions.remove(consumption);
    }

    public List<CalorieConsumption> getConsumptions()
    {
        return Collections.unmodifiableList(consumptions);
    }

    private void clearOldConsumptions(LocalDateTime now)
    {
        List<CalorieConsumption> consumptionsOlderThanOneDay = consumptions.stream()
            .filter(c -> c.getTime().isBefore(now.minusDays(1)))
            .collect(Collectors.toList());

        previousConsumption += consumptionsOlderThanOneDay.stream()
            .mapToInt(CalorieConsumption::getCalories)
            .sum();

        consumptions.removeAll(consumptionsOlderThanOneDay);
    }
}
