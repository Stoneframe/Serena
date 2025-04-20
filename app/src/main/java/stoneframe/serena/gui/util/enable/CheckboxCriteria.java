package stoneframe.serena.gui.util.enable;

import android.widget.CheckBox;

import java.util.function.Predicate;

public class CheckboxCriteria extends EnableCriteria
{
    public static final Predicate<CheckBox> IS_CHECKED = CheckboxCriteria::isChecked;
    public static final Predicate<CheckBox> IS_NOT_CHECKED = CheckboxCriteria::isNotChecked;

    private final CheckBox checkBox;
    private final Predicate<CheckBox> criteria;

    public CheckboxCriteria(CheckBox checkBox, Predicate<CheckBox> criteria)
    {
        this.checkBox = checkBox;
        this.criteria = criteria;
    }

    public static boolean isChecked(CheckBox c)
    {
        return c.isChecked();
    }

    public static boolean isNotChecked(CheckBox c)
    {
        return !c.isChecked();
    }

    @Override
    protected boolean isValid()
    {
        return criteria.test(checkBox);
    }

    @Override
    protected void addWatcher()
    {
        checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> criteriaValueChanged());
    }
}
