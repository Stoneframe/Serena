package stoneframe.chorelist.model;

import org.joda.time.DateTime;

public interface EffortTracker
{

    int getTodaysEffort(DateTime now);

    void spend(int effort);
}
