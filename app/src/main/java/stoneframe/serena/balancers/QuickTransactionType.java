package stoneframe.serena.balancers;

public class QuickTransactionType extends TransactionType
{
    public QuickTransactionType()
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
