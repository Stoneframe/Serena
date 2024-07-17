package stoneframe.chorelist.model.calories;

public class QuickCalorieConsumptionType extends CalorieConsumptionType
{
    public QuickCalorieConsumptionType()
    {
        super("Quick", 0);
    }

    @Override
    public boolean isQuick()
    {
        return true;
    }
}
