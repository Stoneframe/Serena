package stoneframe.serena.model.limiters;

import androidx.annotation.NonNull;

import org.joda.time.LocalDateTime;

import java.util.List;

import stoneframe.serena.model.Editor;
import stoneframe.serena.model.timeservices.TimeService;

public class LimiterEditor extends Editor<LimiterEditor.LimiterEditorListener>
{
    private final LimiterManager limiterManager;
    private final Limiter limiter;

    private final PropertyUtil<String> nameProperty;
    private final PropertyUtil<String> unitProperty;
    private final PropertyUtil<Integer> incrementPerDayProperty;
    private final PropertyUtil<Boolean> isQuickAllowableProperty;

    public LimiterEditor(LimiterManager limiterManager, Limiter limiter, TimeService timeService)
    {
        super(timeService);

        this.limiterManager = limiterManager;
        this.limiter = limiter;

        nameProperty = getNameProperty();
        unitProperty = getUnitProperty();
        incrementPerDayProperty = getIncrementPerDayProperty();
        isQuickAllowableProperty = getIsQuickAllowableProperty();
    }

    public String getName()
    {
        return nameProperty.getValue();
    }

    public void setName(String name)
    {
        nameProperty.setValue(name);
    }

    public String getUnit()
    {
        return unitProperty.getValue();
    }

    public void setUnit(String unit)
    {
        unitProperty.setValue(unit);
    }

    public int getIncrementPerDay()
    {
        return incrementPerDayProperty.getValue();
    }

    public void setIncrementPerDay(int incrementPerDay)
    {
        incrementPerDayProperty.setValue(incrementPerDay);
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
        if (hasMaxValueChanged(maxValue))
        {
            limiter.setMaxValue(maxValue, LocalDateTime.now());
            notifyListeners(LimiterEditorListener::availableChanged);
        }
    }

    public boolean isQuickDisableable()
    {
        return limiter.isQuickDisableable();
    }

    public boolean isQuickAllowed()
    {
        return isQuickAllowableProperty.getValue();
    }

    public void setAllowQuick(boolean allowQuick)
    {
        isQuickAllowableProperty.setValue(allowQuick);
    }

    public List<ExpenditureType> getExpenditureTypes()
    {
        return limiter.getExpenditureTypes();
    }

    public void addExpenditureType(CustomExpenditureType expenditureType)
    {
        limiter.addExpenditureType(expenditureType);

        notifyListeners(l -> l.expenditureTypeAdded(expenditureType));
        notifyListeners(LimiterEditorListener::expenditureTypesChanged);
    }

    public void removeExpenditureType(CustomExpenditureType expenditureType)
    {
        limiter.removeExpenditureType(expenditureType);

        if (limiter.getExpenditureTypes().isEmpty())
        {
            limiter.setAllowQuick(true);
        }

        notifyListeners(l -> l.expenditureTypeRemoved(expenditureType));
    }

    public void addExpenditure(String name, int expenditureAmount)
    {
        limiter.addExpenditure(new Expenditure(name, expenditureAmount), getNow());

        notifyListeners(LimiterEditorListener::expenditureAdded);
    }

    public int getAvailable()
    {
        return limiter.getAvailable(getNow());
    }

    public void delete()
    {
        limiterManager.removeLimiter(limiter);
    }

    public void setExpenditureTypeName(CustomExpenditureType expenditureType, String name)
    {
        expenditureType.setName(name);

        notifyListeners(l -> l.expenditureTypeEdited(expenditureType));
        notifyListeners(LimiterEditorListener::expenditureTypesChanged);
    }

    public void setExpenditureTypeAmount(CustomExpenditureType expenditureType, int amount)
    {
        expenditureType.setAmount(amount);

        notifyListeners(l -> l.expenditureTypeEdited(expenditureType));
        notifyListeners(LimiterEditorListener::expenditureTypesChanged);
    }

    public void setFavorite(CustomExpenditureType expenditureType, boolean isFavorite)
    {
        expenditureType.setFavorite(isFavorite);

        notifyListeners(l -> l.expenditureTypeEdited(expenditureType));
        notifyListeners(LimiterEditorListener::expenditureTypesChanged);
    }

    private boolean hasMaxValueChanged(Integer maxValue)
    {
        return maxValue == null && limiter.hasMaxValue()
            || maxValue != null && maxValue != limiter.getMaxValue();
    }

    private @NonNull PropertyUtil<String> getNameProperty()
    {
        return new PropertyUtil<>(
            limiter::getName,
            limiter::setName,
            v -> notifyListeners(LimiterEditorListener::nameChanged));
    }

    private @NonNull PropertyUtil<String> getUnitProperty()
    {
        return new PropertyUtil<>(
            limiter::getUnit,
            limiter::setUnit,
            v -> notifyListeners(LimiterEditorListener::unitChanged));
    }

    private @NonNull PropertyUtil<Integer> getIncrementPerDayProperty()
    {
        return new PropertyUtil<>(
            limiter::getIncrementPerDay,
            v -> limiter.setIncrementPerDay(getNow(), v),
            v -> notifyListeners(LimiterEditorListener::incrementPerDayChanged));
    }

    private @NonNull PropertyUtil<Boolean> getIsQuickAllowableProperty()
    {
        return new PropertyUtil<>(
            limiter::isQuickAllowed,
            limiter::setAllowQuick,
            v -> notifyListeners(l -> l.isQuickChanged(v)));
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
