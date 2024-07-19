package stoneframe.chorelist.model.limiters;

import java.util.List;

import stoneframe.chorelist.model.timeservices.TimeService;

public class LimiterEditor
{
    private final LimiterManager limiterManager;
    private final Limiter limiter;
    private final TimeService timeService;

    public LimiterEditor(LimiterManager limiterManager, Limiter limiter, TimeService timeService)
    {
        this.limiterManager = limiterManager;
        this.limiter = limiter;
        this.timeService = timeService;
    }

    public String getName()
    {
        return limiter.getName();
    }

    public void setName(String name)
    {
        limiter.setName(name);
    }

    public String getUnit()
    {
        return limiter.getUnit();
    }

    public void setUnit(String unit)
    {
        limiter.setUnit(unit);
    }

    public void addExpenditureType(CustomExpenditureType expenditureType)
    {
        limiter.addExpenditureType(expenditureType);
    }

    public void removeExpenditureType(CustomExpenditureType expenditureType)
    {
        limiter.removeExpenditureType(expenditureType);
    }

    public List<ExpenditureType> getExpenditureTypes()
    {
        return limiter.getExpenditureTypes();
    }

    public void addExpenditure(String name, int expenditureAmount)
    {
        limiter.addExpenditure(new Expenditure(name, expenditureAmount), timeService.getNow());
    }

    public int getAvailable()
    {
        return limiter.getAvailable(timeService.getNow());
    }

    public int getIncrementPerDay()
    {
        return limiter.getIncrementPerDay();
    }

    public void setIncrementPerDay(int incrementPerDay)
    {
        limiter.setIncrementPerDay(timeService.getNow(), incrementPerDay);
    }

    public void delete()
    {
        limiterManager.removeLimiter(limiter);
    }
}
