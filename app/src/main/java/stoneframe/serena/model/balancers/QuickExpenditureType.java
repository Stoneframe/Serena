package stoneframe.serena.model.balancers;

public class QuickExpenditureType extends ExpenditureType
{
    public QuickExpenditureType()
    {
        super("Quick", 0);
    }

    @Override
    public boolean isQuick()
    {
        return true;
    }

    @Override
    public boolean isFavorite()
    {
        return false;
    }
}
