package stoneframe.chorelist.model.limiters;

import androidx.annotation.NonNull;

import org.joda.time.LocalDateTime;

public abstract class ExpenditureType
{
    protected String name;
    protected int amount;

    protected ExpenditureType(String name, int amount)
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

    public void setExpenditure(int amount)
    {
        this.amount = amount;
    }

    public Expenditure getExpenditure(LocalDateTime now)
    {
        return new Expenditure(name, amount, now);
    }

    public abstract boolean isQuick();

    @NonNull
    @Override
    public String toString()
    {
        return name;
    }
}
