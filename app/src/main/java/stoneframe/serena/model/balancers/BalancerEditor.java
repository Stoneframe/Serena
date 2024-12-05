package stoneframe.serena.model.balancers;

import androidx.annotation.NonNull;

import org.joda.time.LocalDateTime;

import java.util.List;

import stoneframe.serena.model.Editor;
import stoneframe.serena.model.timeservices.TimeService;

public class BalancerEditor extends Editor<BalancerEditor.BalanceEditorListener>
{
    private final BalancerManager balancerManager;
    private final Balancer balancer;

    private final PropertyUtil<String> nameProperty;
    private final PropertyUtil<String> unitProperty;
    private final PropertyUtil<Integer> incrementPerDayProperty;
    private final PropertyUtil<Boolean> isQuickAllowableProperty;

    public BalancerEditor(BalancerManager balancerManager, Balancer balancer, TimeService timeService)
    {
        super(timeService);

        this.balancerManager = balancerManager;
        this.balancer = balancer;

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
        return balancer.hasMaxValue();
    }

    public int getMaxValue()
    {
        return balancer.getMaxValue();
    }

    public void setMaxValue(Integer maxValue)
    {
        if (hasMaxValueChanged(maxValue))
        {
            balancer.setMaxValue(maxValue, LocalDateTime.now());
            notifyListeners(BalanceEditorListener::availableChanged);
        }
    }

    public boolean isQuickDisableable()
    {
        return balancer.isQuickDisableable();
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
        return balancer.getExpenditureTypes();
    }

    public void addExpenditureType(CustomExpenditureType expenditureType)
    {
        balancer.addExpenditureType(expenditureType);

        notifyListeners(l -> l.expenditureTypeAdded(expenditureType));
        notifyListeners(BalanceEditorListener::expenditureTypesChanged);
    }

    public void removeExpenditureType(CustomExpenditureType expenditureType)
    {
        balancer.removeExpenditureType(expenditureType);

        if (balancer.getExpenditureTypes().isEmpty())
        {
            balancer.setAllowQuick(true);
        }

        notifyListeners(l -> l.expenditureTypeRemoved(expenditureType));
    }

    public void addExpenditure(String name, int expenditureAmount)
    {
        balancer.addExpenditure(new Expenditure(name, expenditureAmount), getNow());

        notifyListeners(BalanceEditorListener::expenditureAdded);
    }

    public int getAvailable()
    {
        return balancer.getAvailable(getNow());
    }

    public void delete()
    {
        balancerManager.removeBalancer(balancer);
    }

    public void setExpenditureTypeName(CustomExpenditureType expenditureType, String name)
    {
        expenditureType.setName(name);

        notifyListeners(l -> l.expenditureTypeEdited(expenditureType));
        notifyListeners(BalanceEditorListener::expenditureTypesChanged);
    }

    public void setExpenditureTypeAmount(CustomExpenditureType expenditureType, int amount)
    {
        expenditureType.setAmount(amount);

        notifyListeners(l -> l.expenditureTypeEdited(expenditureType));
        notifyListeners(BalanceEditorListener::expenditureTypesChanged);
    }

    public void setFavorite(CustomExpenditureType expenditureType, boolean isFavorite)
    {
        expenditureType.setFavorite(isFavorite);

        notifyListeners(l -> l.expenditureTypeEdited(expenditureType));
        notifyListeners(BalanceEditorListener::expenditureTypesChanged);
    }

    private boolean hasMaxValueChanged(Integer maxValue)
    {
        return maxValue == null && balancer.hasMaxValue()
            || maxValue != null && maxValue != balancer.getMaxValue();
    }

    private @NonNull PropertyUtil<String> getNameProperty()
    {
        return new PropertyUtil<>(
            balancer::getName,
            balancer::setName,
            v -> notifyListeners(BalanceEditorListener::nameChanged));
    }

    private @NonNull PropertyUtil<String> getUnitProperty()
    {
        return new PropertyUtil<>(
            balancer::getUnit,
            balancer::setUnit,
            v -> notifyListeners(BalanceEditorListener::unitChanged));
    }

    private @NonNull PropertyUtil<Integer> getIncrementPerDayProperty()
    {
        return new PropertyUtil<>(
            balancer::getIncrementPerDay,
            v -> balancer.setIncrementPerDay(getNow(), v),
            v -> notifyListeners(BalanceEditorListener::incrementPerDayChanged));
    }

    private @NonNull PropertyUtil<Boolean> getIsQuickAllowableProperty()
    {
        return new PropertyUtil<>(
            balancer::isQuickAllowed,
            balancer::setAllowQuick,
            v -> notifyListeners(l -> l.isQuickChanged(v)));
    }

    public interface BalanceEditorListener
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
