package stoneframe.serena.gui.util;

import android.text.TextWatcher;
import android.widget.EditText;

import java.util.function.Predicate;

public class EditTextCriteria
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

    boolean isValid()
    {
        return criteria.test(editText);
    }

    void addWatcher(TextWatcher textWatcher)
    {
        editText.addTextChangedListener(textWatcher);
    }

    private static boolean isNotEmpty(EditText e)
    {
        return e.getText().length() > 0;
    }

    private static boolean isValidInteger(EditText e)
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
}
