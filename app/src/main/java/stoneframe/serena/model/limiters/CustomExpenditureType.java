package stoneframe.serena.model.limiters;

public class CustomExpenditureType extends ExpenditureType
{
    public CustomExpenditureType(String name, int calories)
    {
        super(name, calories);
    }

    @Override
    public boolean isQuick()
    {
        return false;
    }

    void setName(String name)
    {
        this.name = name;
    }
}
