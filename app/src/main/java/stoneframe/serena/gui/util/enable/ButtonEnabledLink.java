package stoneframe.serena.gui.util.enable;

import android.widget.Button;

public class ButtonEnabledLink extends EnabledLink
{
    private final Button button;

    public ButtonEnabledLink(Button button, EnableCriteria... criteria)
    {
        super(criteria);

        this.button = button;

        criteriaValueChanged();
    }

    @Override
    protected void updateEnabled(boolean isEnabled)
    {
        button.setEnabled(isEnabled);
    }
}
