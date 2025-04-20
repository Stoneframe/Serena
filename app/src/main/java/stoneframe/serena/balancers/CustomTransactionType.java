package stoneframe.serena.balancers;

public class CustomTransactionType extends TransactionType
{
    private boolean isFavorite;

    public CustomTransactionType(String name, int calories, boolean isFavorite)
    {
        super(name, calories);

        this.isFavorite = isFavorite;
    }

    @Override
    public boolean isQuick()
    {
        return false;
    }

    @Override
    public boolean isFavorite()
    {
        return isFavorite;
    }

    void setFavorite(boolean isFavorite)
    {
        this.isFavorite = isFavorite;
    }

    void setName(String name)
    {
        this.name = name;
    }
}
