package stoneframe.serena.gui.balancers;

import stoneframe.serena.gui.util.enable.EnableCriteria;
import stoneframe.serena.balancers.BalancerEditor;
import stoneframe.serena.balancers.CustomTransactionType;

public class BalancerEnabledCriteria extends EnableCriteria
{
    private final BalancerEditor balancerEditor;

    public BalancerEnabledCriteria(BalancerEditor balancerEditor)
    {
        this.balancerEditor = balancerEditor;
    }

    @Override
    protected boolean isValid()
    {
        return balancerEditor.isEnabled();
    }

    @Override
    protected void addWatcher()
    {
        balancerEditor.addListener(new BalancerEditor.BalanceEditorListener()
        {
            @Override
            public void nameChanged()
            {

            }

            @Override
            public void unitChanged()
            {

            }

            @Override
            public void isQuickChanged(boolean isAllowed)
            {

            }

            @Override
            public void isEnabledChanged(boolean isEnabled)
            {
                criteriaValueChanged();
            }

            @Override
            public void changePerIntervalChanged()
            {

            }

            @Override
            public void intervalTypeChanged()
            {

            }

            @Override
            public void transactionTypesChanged()
            {

            }

            @Override
            public void transactionTypeAdded(CustomTransactionType transactionType)
            {

            }

            @Override
            public void transactionTypeEdited(CustomTransactionType transactionType)
            {

            }

            @Override
            public void transactionTypeRemoved(CustomTransactionType transactionType)
            {

            }

            @Override
            public void transactionAdded()
            {

            }

            @Override
            public void availableChanged()
            {

            }
        });
    }
}
