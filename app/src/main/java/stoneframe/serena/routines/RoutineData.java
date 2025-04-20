package stoneframe.serena.routines;

import org.joda.time.LocalDateTime;

abstract class RoutineData
{
    String name;
    boolean isEnabled;
    LocalDateTime lastCompleted;

    RoutineData(String name, LocalDateTime now)
    {
        this.name = name;
        this.lastCompleted = now;
        this.isEnabled = true;
    }
}
