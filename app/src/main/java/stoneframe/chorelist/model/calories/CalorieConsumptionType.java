package stoneframe.chorelist.model.calories;

import androidx.annotation.NonNull;

import org.joda.time.LocalDateTime;

public abstract class CalorieConsumptionType
{
    protected String name;
    protected int calories;

    protected CalorieConsumptionType(String name, int calories)
    {
        this.name = name;
        this.calories = calories;
    }

    public String getName()
    {
        return name;
    }

    public int getCalories()
    {
        return calories;
    }

    public void setCalories(int calories)
    {
        this.calories = calories;
    }

    public CalorieConsumption getConsumption(LocalDateTime now)
    {
        return new CalorieConsumption(name, calories, now);
    }

    public abstract boolean isQuick();

    @NonNull
    @Override
    public String toString()
    {
        return name;
    }
}
