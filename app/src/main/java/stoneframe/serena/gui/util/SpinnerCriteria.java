package stoneframe.serena.gui.util;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import java.util.function.Predicate;

public class SpinnerCriteria extends EnableCriteria
{
    private final Spinner spinner;
    private final Predicate<Spinner> criteria;

    public SpinnerCriteria(Spinner spinner, Predicate<Spinner> criteria)
    {
        this.spinner = spinner;
        this.criteria = criteria;
    }

    public static boolean hasSelection(Spinner spinner)
    {
        return spinner.getSelectedItemPosition() != AdapterView.INVALID_POSITION;
    }

    @Override
    void addWatcher(EditTextButtonEnabledLink link)
    {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                link.componentChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                link.componentChanged();
            }
        });
    }

    @Override
    boolean isValid()
    {
        return criteria.test(spinner);
    }
}
