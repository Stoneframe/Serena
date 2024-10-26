package stoneframe.serena.gui.util;

public abstract class EnableCriteria
{
    abstract void addWatcher(EditTextButtonEnabledLink link);

    abstract boolean isValid();
}
