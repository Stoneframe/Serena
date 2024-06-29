package stoneframe.chorelist.gui.util;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class EditTextButtonEnabledLink implements TextWatcher
{
    private final List<EditTextCriteria> editTextCriteria = new LinkedList<>();
    private final Button button;

    public EditTextButtonEnabledLink(Button button, EditTextCriteria... editTextsCriteria)
    {
        this.button = button;

        Arrays.stream(editTextsCriteria).forEach(criteria ->
        {
            criteria.addWatcher(this);

            this.editTextCriteria.add(criteria);
        });

        updateButtonEnabled();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after)
    {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count)
    {
    }

    @Override
    public void afterTextChanged(Editable s)
    {
        updateButtonEnabled();
    }

    private void updateButtonEnabled()
    {
        boolean isEnabled = editTextCriteria.stream().allMatch(EditTextCriteria::isValid);

        button.setEnabled(isEnabled);
    }
}
