package stoneframe.chorelist.model.calories;

import org.joda.time.LocalDateTime;

public class CalorieConsumption
{
    private final String name;
    private final int calories;
    private final LocalDateTime time;

    public CalorieConsumption(String name, int calories, LocalDateTime now)
    {
        this.name = name;
        this.calories = calories;
        this.time = now;
    }

    public String getName()
    {
        return name;
    }

    public int getCalories()
    {
        return calories;
    }

    public LocalDateTime getTime()
    {
        return time;
    }
}
