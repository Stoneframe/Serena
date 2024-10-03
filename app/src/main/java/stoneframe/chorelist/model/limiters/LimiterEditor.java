package stoneframe.chorelist.model.limiters;

import java.util.LinkedList;
import java.util.List;

import stoneframe.chorelist.model.timeservices.TimeService;

public class LimiterEditor
{
    private final List<LimiterEditorListener> listeners = new LinkedList<>();

    private final LimiterManager limiterManager;
    private final Limiter limiter;
    private final TimeService timeService;

    public LimiterEditor(LimiterManager limiterManager, Limiter limiter, TimeService timeService)
    {
        this.limiterManager = limiterManager;
        this.limiter = limiter;
        this.timeService = timeService;
    }

    public void addListener(LimiterEditorListener listener)
    {
        listeners.add(listener);
    }

    public void removeListener(LimiterEditorListener listener)
    {
        listeners.remove(listener);
    }

    public String getName()
    {
        return limiter.getName();
    }

    public void setName(String name)
    {
        limiter.setName(name);

        listeners.forEach(LimiterEditorListener::nameChanged);
    }

    public String getUnit()
    {
        return limiter.getUnit();
    }

    public void setUnit(String unit)
    {
        limiter.setUnit(unit);

        listeners.forEach(LimiterEditorListener::unitChanged);
    }

    public int getIncrementPerDay()
    {
        return limiter.getIncrementPerDay();
    }

    public void setIncrementPerDay(int incrementPerDay)
    {
        limiter.setIncrementPerDay(timeService.getNow(), incrementPerDay);

        listeners.forEach(LimiterEditorListener::incrementPerDayChanged);
    }

    public boolean hasMaxValue()
    {
        return limiter.hasMaxValue();
    }

    public int getMaxValue()
    {
        return limiter.getMaxValue();
    }

    public void setMaxValue(Integer maxValue)
    {
        limiter.setMaxValue(maxValue, timeService.getNow());

        listeners.forEach(LimiterEditorListener::availableChanged);
    }

    public boolean isQuickDisableable()
    {
        return limiter.isQuickDisableable();
    }

    public boolean isQuickAllowed()
    {
        return limiter.isQuickAllowed();
    }

    public void setAllowQuick(boolean allowQuick)
    {
        limiter.setAllowQuick(allowQuick);

        listeners.forEach(l -> l.isQuickChanged(allowQuick));
    }

    public List<ExpenditureType> getExpenditureTypes()
    {
        return limiter.getExpenditureTypes();
    }

    public void addExpenditureType(CustomExpenditureType expenditureType)
    {
        limiter.addExpenditureType(expenditureType);

        listeners.forEach(l -> l.expenditureTypeAdded(expenditureType));
        listeners.forEach(LimiterEditorListener::expenditureTypesChanged);
    }

    public void removeExpenditureType(CustomExpenditureType expenditureType)
    {
        limiter.removeExpenditureType(expenditureType);

        if (limiter.getExpenditureTypes().isEmpty())
        {
            limiter.setAllowQuick(true);
        }

        listeners.forEach(l -> l.expenditureTypeRemoved(expenditureType));
        listeners.forEach(LimiterEditorListener::expenditureTypesChanged);
    }

    public void addExpenditure(String name, int expenditureAmount)
    {
        limiter.addExpenditure(new Expenditure(name, expenditureAmount), timeService.getNow());

        listeners.forEach(LimiterEditorListener::expenditureAdded);
    }

    public int getAvailable()
    {
        return limiter.getAvailable(timeService.getNow());
    }

    public void delete()
    {
        limiterManager.removeLimiter(limiter);
    }

    public void setExpenditureTypeName(CustomExpenditureType expenditureType, String name)
    {
        expenditureType.setName(name);

        listeners.forEach(l -> l.expenditureTypeEdited(expenditureType));
        listeners.forEach(LimiterEditorListener::expenditureTypesChanged);
    }

    public void setExpenditureTypeAmount(CustomExpenditureType expenditureType, int amount)
    {
        expenditureType.setAmount(amount);

        listeners.forEach(l -> l.expenditureTypeEdited(expenditureType));
        listeners.forEach(LimiterEditorListener::expenditureTypesChanged);
    }

    public interface LimiterEditorListener
    {
        void nameChanged();

        void unitChanged();

        void isQuickChanged(boolean isAllowed);

        void incrementPerDayChanged();

        void expenditureTypesChanged();

        void expenditureTypeAdded(CustomExpenditureType expenditureType);

        void expenditureTypeEdited(CustomExpenditureType expenditureType);

        void expenditureTypeRemoved(CustomExpenditureType expenditureType);

        void expenditureAdded();

        void availableChanged();
    }
}
