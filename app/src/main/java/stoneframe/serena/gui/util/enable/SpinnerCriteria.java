package stoneframe.serena.gui.util.enable;

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
    protected void addWatcher()
    {
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                criteriaValueChanged();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
                criteriaValueChanged();
            }
        });
    }

    @Override
    protected boolean isValid()
    {
        return criteria.test(spinner);
    }
}
