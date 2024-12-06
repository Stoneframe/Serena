package stoneframe.serena.model.balancers;

import androidx.annotation.NonNull;

import java.util.List;

import stoneframe.serena.model.Editor;
import stoneframe.serena.model.timeservices.TimeService;

public class BalancerEditor extends Editor<BalancerEditor.BalanceEditorListener>
{
    private final BalancerManager balancerManager;
    private final Balancer balancer;

    private final PropertyUtil<String> nameProperty;
    private final PropertyUtil<String> unitProperty;
    private final PropertyUtil<Integer> changePerDayProperty;
    private final PropertyUtil<Boolean> isQuickAllowableProperty;

    public BalancerEditor(BalancerManager balancerManager, Balancer balancer, TimeService timeService)
    {
        super(timeService);

        this.balancerManager = balancerManager;
        this.balancer = balancer;

        nameProperty = getNameProperty();
        unitProperty = getUnitProperty();
        changePerDayProperty = getChangePerDayProperty();
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

    public int getChangePerDay()
    {
        return changePerDayProperty.getValue();
    }

    public void setChangePerDay(int changePerDay)
    {
        changePerDayProperty.setValue(changePerDay);
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
            balancer.setMaxValue(maxValue, getNow());
            notifyListeners(BalanceEditorListener::availableChanged);
        }
    }

    public boolean hasMinValue()
    {
        return balancer.hasMinValue();
    }

    public int getMinValue()
    {
        return balancer.getMinValue();
    }

    public void setMinValue(Integer minValue)
    {
        if (hasMinValueChanged(minValue))
        {
            balancer.setMinValue(minValue, getNow());
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

    public List<TransactionType> getTransactionTypes()
    {
        return balancer.getTransactionTypes();
    }

    public void addTransactionType(CustomTransactionType transactionType)
    {
        balancer.addTransactionType(transactionType);

        notifyListeners(l -> l.transactionTypeAdded(transactionType));
        notifyListeners(BalanceEditorListener::transactionTypesChanged);
    }

    public void removeTransactionType(CustomTransactionType transactionType)
    {
        balancer.removeTransactionType(transactionType);

        if (balancer.getTransactionTypes().isEmpty())
        {
            balancer.setAllowQuick(true);
        }

        notifyListeners(l -> l.transactionTypeRemoved(transactionType));
    }

    public void addTransaction(String name, int transactionAmount)
    {
        balancer.addTransaction(new Transaction(name, transactionAmount), getNow());

        notifyListeners(BalanceEditorListener::transactionAdded);
    }

    public int getAvailable()
    {
        return balancer.getAvailable(getNow());
    }

    public void delete()
    {
        balancerManager.removeBalancer(balancer);
    }

    public void setTransactionTypeName(CustomTransactionType transactionType, String name)
    {
        transactionType.setName(name);

        notifyListeners(l -> l.transactionTypeEdited(transactionType));
        notifyListeners(BalanceEditorListener::transactionTypesChanged);
    }

    public void setTransactionTypeAmount(CustomTransactionType transactionType, int amount)
    {
        transactionType.setAmount(amount);

        notifyListeners(l -> l.transactionTypeEdited(transactionType));
        notifyListeners(BalanceEditorListener::transactionTypesChanged);
    }

    public void setFavorite(CustomTransactionType transactionType, boolean isFavorite)
    {
        transactionType.setFavorite(isFavorite);

        notifyListeners(l -> l.transactionTypeEdited(transactionType));
        notifyListeners(BalanceEditorListener::transactionTypesChanged);
    }

    private boolean hasMaxValueChanged(Integer maxValue)
    {
        return maxValue == null && balancer.hasMaxValue()
            || maxValue != null && maxValue != balancer.getMaxValue();
    }

    private boolean hasMinValueChanged(Integer minValue)
    {
        return minValue == null && balancer.hasMinValue()
            || minValue != null && minValue != balancer.getMinValue();
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

    private @NonNull PropertyUtil<Integer> getChangePerDayProperty()
    {
        return new PropertyUtil<>(
            balancer::getChangePerDay,
            v -> balancer.setChangePerDay(getNow(), v),
            v -> notifyListeners(BalanceEditorListener::changePerDayChanged));
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

        void changePerDayChanged();

        void transactionTypesChanged();

        void transactionTypeAdded(CustomTransactionType transactionType);

        void transactionTypeEdited(CustomTransactionType transactionType);

        void transactionTypeRemoved(CustomTransactionType transactionType);

        void transactionAdded();

        void availableChanged();
    }
}
