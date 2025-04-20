package stoneframe.serena.gui.util.enable;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class OrEnableCriteria extends EnableCriteria
{
    private final List<EnableCriteria> criteria;

    public OrEnableCriteria(EnableCriteria...criteria)
    {
        this.criteria = Arrays.stream(criteria).collect(Collectors.toList());
    }

    @Override
    protected boolean isValid()
    {
        return criteria.stream().anyMatch(EnableCriteria::isValid);
    }

    @Override
    protected void addWatcher()
    {
        criteria.forEach(c -> c.subscribe(OrEnableCriteria.super::criteriaValueChanged));
    }
}
