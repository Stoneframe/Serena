package stoneframe.chorelist.model.limiters;

public class Expenditure
{
    private final String name;
    private final int amount;

    public Expenditure(String name, int amount)
    {
        this.name = name;
        this.amount = amount;
    }

    public String getName()
    {
        return name;
    }

    public int getAmount()
    {
        return amount;
    }
}
