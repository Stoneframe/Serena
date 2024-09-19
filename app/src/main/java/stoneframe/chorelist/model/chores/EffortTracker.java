package stoneframe.chorelist.model.chores;

import org.joda.time.LocalDate;

public interface EffortTracker
{
    int getTodaysEffort(LocalDate today);

    void spend(int effort);

    void reset(LocalDate today);
}
