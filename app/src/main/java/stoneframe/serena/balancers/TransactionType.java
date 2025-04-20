package stoneframe.serena.balancers;

import androidx.annotation.NonNull;

public abstract class TransactionType
{
    protected String name;
    protected int amount;

    protected TransactionType(String name, int amount)
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

    void setAmount(int amount)
    {
        this.amount = amount;
    }

    public abstract boolean isQuick();

    public abstract boolean isFavorite();

    @NonNull
    @Override
    public String toString()
    {
        return name;
    }
}
