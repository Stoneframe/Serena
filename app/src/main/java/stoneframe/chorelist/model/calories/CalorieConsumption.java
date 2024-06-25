package stoneframe.chorelist.model.calories;

public class CalorieConsumption
{
    private final String name;
    private final int calories;

    public CalorieConsumption(String name, int calories)
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
}
