package stoneframe.chorelist.model.limiters;

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

    public void setName(String name)
    {
        this.name = name;
    }
}
