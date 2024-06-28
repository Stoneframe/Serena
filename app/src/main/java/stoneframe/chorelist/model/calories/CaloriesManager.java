package stoneframe.chorelist.model.calories;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.Minutes;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class CaloriesManager
{
    private static final double MINUTES_PER_DAY = 24 * 60;

    private final List<CalorieConsumptionType> consumptionTypes = new LinkedList<>();
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

    public void setIncrementPerDay(LocalDate today, int incrementPerDay)
    {
        this.startDate = today;
        this.incrementPerDay = incrementPerDay;
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

    public void addConsumption(CalorieConsumption consumption)
    {
        List<CalorieConsumption> oldConsumptions = consumptions.stream()
            .filter(c -> c.getTime().isBefore(consumption.getTime().minusDays(1)))
            .collect(Collectors.toList());

        previousConsumption += oldConsumptions.stream()
            .mapToInt(CalorieConsumption::getCalories)
            .sum();

        consumptions.removeAll(oldConsumptions);

        consumptions.add(consumption);
    }
}
