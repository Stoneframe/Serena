package stoneframe.serena.gui.util.enable;

public abstract class EnableCriteria
{
    private CriteriaListener listener;

    void subscribe(CriteriaListener listener)
    {
        this.listener = listener;

        addWatcher();
    }

    protected void criteriaValueChanged()
    {
        listener.criteriaValueChanged();
    }

    protected abstract boolean isValid();

    protected abstract void addWatcher();
}
