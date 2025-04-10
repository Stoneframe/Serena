package stoneframe.serena.gui.util.enable;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public abstract class EnabledLink implements CriteriaListener
{
    private final List<EnableCriteria> criteria = new LinkedList<>();

    protected EnabledLink(EnableCriteria... criteria)
    {
        Arrays.stream(criteria).forEach(c ->
        {
            c.subscribe(this);

            this.criteria.add(c);
        });
    }

    @Override
    public void criteriaValueChanged()
    {
        boolean isEnabled = criteria.stream().allMatch(EnableCriteria::isValid);

        updateEnabled(isEnabled);
    }

    protected abstract void updateEnabled(boolean isEnabled);
}
