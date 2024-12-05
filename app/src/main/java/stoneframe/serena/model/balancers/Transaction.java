package stoneframe.serena.model.balancers;

public class Transaction
{
    private final String name;
    private final int amount;

    public Transaction(String name, int amount)
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
