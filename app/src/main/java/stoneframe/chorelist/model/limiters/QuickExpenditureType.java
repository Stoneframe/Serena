package stoneframe.chorelist.model.limiters;

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
}
