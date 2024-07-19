package stoneframe.chorelist.model.limiters;

import org.joda.time.LocalDateTime;

public class Expenditure
{
    private final String name;
    private final int amount;
    private final LocalDateTime time;

    public Expenditure(String name, int amount, LocalDateTime now)
    {
        this.name = name;
        this.amount = amount;
        this.time = now;
    }

    public String getName()
    {
        return name;
    }

    public int getAmount()
    {
        return amount;
    }

    public LocalDateTime getTime()
    {
        return time;
    }
}
