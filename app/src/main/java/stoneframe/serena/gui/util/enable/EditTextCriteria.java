package stoneframe.serena.gui.util.enable;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.function.Predicate;

public class EditTextCriteria extends EnableCriteria
{
    public static final Predicate<EditText> IS_NOT_EMPTY = EditTextCriteria::isNotEmpty;
    public static final Predicate<EditText> IS_VALID_INT = EditTextCriteria::isValidInteger;

    private final EditText editText;
    private final Predicate<EditText> criteria;

    public EditTextCriteria(EditText editText, Predicate<EditText> criteria)
    {
        this.editText = editText;
        this.criteria = criteria;
    }

    public static boolean isNotEmpty(EditText e)
    {
        return e.getText().length() > 0;
    }

    public static boolean isValidInteger(EditText e)
    {
        try
        {
            Integer.parseInt(e.getText().toString());
            return true;
        }
        catch (NumberFormatException ex)
        {
            return false;
        }
    }

    @Override
    protected boolean isValid()
    {
        return criteria.test(editText);
    }

    @Override
    protected void addWatcher()
    {
        editText.addTextChangedListener(new TextWatcher()
        {
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
                criteriaValueChanged();
            }
        });
    }
}
