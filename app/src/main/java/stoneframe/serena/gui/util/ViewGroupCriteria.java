package stoneframe.serena.gui.util;

import android.view.View;
import android.view.ViewGroup;

import java.util.function.Predicate;

public class ViewGroupCriteria extends EnableCriteria
{
    private final ViewGroup viewGroup;
    private final Predicate<ViewGroup> criteria;

    public ViewGroupCriteria(ViewGroup viewGroup, Predicate<ViewGroup> criteria)
    {
        this.viewGroup = viewGroup;
        this.criteria = criteria;
    }

    public static boolean isVisible(ViewGroup viewGroup)
    {
        return viewGroup.getVisibility() == View.VISIBLE;
    }

    @Override
    protected void addWatcher()
    {
        viewGroup.getViewTreeObserver().addOnDrawListener(this::criteriaValueChanged);
    }

    @Override
    protected boolean isValid()
    {
        return criteria.test(viewGroup);
    }
}
