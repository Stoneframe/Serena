package stoneframe.serena.gui.util;

public abstract class EnableCriteria
{
    private ButtonEnabledLink link;

    void subscribe(ButtonEnabledLink link)
    {
        this.link = link;

        addWatcher();
    }

    protected void criteriaValueChanged()
    {
        link.criteriaValueChanged();
    }

    protected abstract boolean isValid();

    protected abstract void addWatcher();
}
