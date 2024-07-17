package stoneframe.chorelist.model.calories;

public class CustomCalorieConsumptionType extends CalorieConsumptionType
{
    public CustomCalorieConsumptionType(String name, int calories)
    {
        super(name, calories);
    }

    @Override
    public boolean isQuick()
    {
        return false;
    }

    public void setName(String name)
    {
        this.name = name;
    }
}
