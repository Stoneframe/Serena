package stoneframe.serena.gui.util.enable;

import java.util.function.Supplier;

public class BooleanCriteria extends EnableCriteria
{
    private final Supplier<Boolean> getBoolean;

    public BooleanCriteria(Supplier<Boolean> getBoolean)
    {
        this.getBoolean = getBoolean;
    }

    @Override
    protected boolean isValid()
    {
        return getBoolean.get();
    }

    @Override
    protected void addWatcher()
    {
    }
}
