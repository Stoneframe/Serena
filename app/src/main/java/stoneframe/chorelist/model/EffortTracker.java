package stoneframe.chorelist.model;

import org.joda.time.LocalDate;

public interface EffortTracker
{
    int getTodaysEffort(LocalDate today);

    void spend(int effort);
}
