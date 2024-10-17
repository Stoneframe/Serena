package stoneframe.chorelist.gui.util;

public abstract class EnableCriteria
{
    abstract void addWatcher(EditTextButtonEnabledLink link);

    abstract boolean isValid();
}
