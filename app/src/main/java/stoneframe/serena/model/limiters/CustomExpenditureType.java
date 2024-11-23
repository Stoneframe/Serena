package stoneframe.serena.model.limiters;

public class CustomExpenditureType extends ExpenditureType
{
    private boolean isFavorite;

    public CustomExpenditureType(String name, int calories, boolean isFavorite)
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
