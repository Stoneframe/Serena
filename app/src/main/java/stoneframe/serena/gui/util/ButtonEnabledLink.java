package stoneframe.serena.gui.util;

import android.widget.Button;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ButtonEnabledLink
{
    private final List<EnableCriteria> criteria = new LinkedList<>();

    private final Button button;

    public ButtonEnabledLink(Button button, EnableCriteria... criteria)
    {
        this.button = button;

        Arrays.stream(criteria).forEach(c ->
        {
            c.subscribe(this);

            this.criteria.add(c);
        });

        updateButtonEnabled();
    }

    void criteriaValueChanged()
    {
        updateButtonEnabled();
    }

    private void updateButtonEnabled()
    {
        boolean isEnabled = criteria.stream().allMatch(EnableCriteria::isValid);

        button.setEnabled(isEnabled);
    }
}
