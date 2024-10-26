package stoneframe.serena.gui.util;

import android.widget.Button;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class EditTextButtonEnabledLink
{
    private final List<EnableCriteria> editTextCriteria = new LinkedList<>();

    private final Button button;

    public EditTextButtonEnabledLink(Button button, EnableCriteria... editTextsCriteria)
    {
        this.button = button;

        Arrays.stream(editTextsCriteria).forEach(criteria ->
        {
            criteria.addWatcher(this);

            this.editTextCriteria.add(criteria);
        });

        updateButtonEnabled();
    }

    void componentChanged()
    {
        updateButtonEnabled();
    }

    private void updateButtonEnabled()
    {
        boolean isEnabled = editTextCriteria.stream().allMatch(EnableCriteria::isValid);

        button.setEnabled(isEnabled);
    }
}
