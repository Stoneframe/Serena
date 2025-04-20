package stoneframe.serena.balancers;

import androidx.annotation.NonNull;

import java.util.List;

import stoneframe.serena.Editor;
import stoneframe.serena.timeservices.TimeService;

public class BalancerEditor extends Editor<BalancerEditor.BalanceEditorListener>
{
    private final BalancerManager balancerManager;
    private final Balancer balancer;

    private final PropertyUtil<String> nameProperty;
    private final PropertyUtil<String> unitProperty;
    private final PropertyUtil<Integer> changePerIntervalProperty;
    private final PropertyUtil<Integer> intervalTypeProperty;
    private final PropertyUtil<Boolean> isQuickAllowableProperty;
    private final PropertyUtil<Boolean> isEnabledProperty;

    BalancerEditor(BalancerManager balancerManager, Balancer balancer, TimeService timeService)
    {
        super(timeService);

        this.balancerManager = balancerManager;
        this.balancer = balancer;

        nameProperty = getNameProperty();
        unitProperty = getUnitProperty();
        changePerIntervalProperty = getChangePerIntervalProperty();
        intervalTypeProperty = getIntervalTypeProperty();
        isQuickAllowableProperty = getIsQuickAllowableProperty();
        isEnabledProperty = getIsEnabledProperty();
    }

    public boolean isEnabled()
    {
        return isEnabledProperty.getValue();
    }

    public void setEnabled(boolean isEnabled)
    {
        isEnabledProperty.setValue(isEnabled);
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

    public int getChangePerInterval()
    {
        return changePerIntervalProperty.getValue();
    }

    public void setChangePerInterval(int changePerInterval)
    {
        changePerIntervalProperty.setValue(changePerInterval);
    }

    public int getIntervalType()
    {
        return intervalTypeProperty.getValue();
    }

    public void setIntervalType(int intervalType)
    {
        intervalTypeProperty.setValue(intervalType);
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
            balancer.setMaxValue(getNow(), maxValue);
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
            balancer.setMinValue(getNow(), minValue);
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
        notifyListeners(BalanceEditorListener::transactionTypesChanged);
    }

    public void addTransaction(int transactionAmount)
    {
        balancer.addTransaction(transactionAmount, getNow());

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

    public void reset()
    {
        balancer.reset(getNow());

        notifyListeners(BalanceEditorListener::availableChanged);
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

    private @NonNull PropertyUtil<Integer> getChangePerIntervalProperty()
    {
        return new PropertyUtil<>(
            balancer::getChangePerInterval,
            v -> balancer.setChangePerInterval(getNow(), v),
            v -> notifyListeners(BalanceEditorListener::changePerIntervalChanged));
    }

    private PropertyUtil<Integer> getIntervalTypeProperty()
    {
        return new PropertyUtil<>(
            balancer::getIntervalType,
            v -> balancer.setIntervalType(getNow(), v),
            v -> notifyListeners(BalanceEditorListener::intervalTypeChanged));
    }

    private @NonNull PropertyUtil<Boolean> getIsQuickAllowableProperty()
    {
        return new PropertyUtil<>(
            balancer::isQuickAllowed,
            balancer::setAllowQuick,
            v -> notifyListeners(l -> l.isQuickChanged(v)));
    }

    private @NonNull PropertyUtil<Boolean> getIsEnabledProperty()
    {
        return new PropertyUtil<>(
            balancer::isEnabled,
            balancer::setEnabled,
            v -> notifyListeners(l -> l.isEnabledChanged(v)));
    }

    public interface BalanceEditorListener
    {
        void nameChanged();

        void unitChanged();

        void isQuickChanged(boolean isAllowed);

        void isEnabledChanged(boolean isEnabled);

        void changePerIntervalChanged();

        void intervalTypeChanged();

        void transactionTypesChanged();

        void transactionTypeAdded(CustomTransactionType transactionType);

        void transactionTypeEdited(CustomTransactionType transactionType);

        void transactionTypeRemoved(CustomTransactionType transactionType);

        void transactionAdded();

        void availableChanged();
    }
}
