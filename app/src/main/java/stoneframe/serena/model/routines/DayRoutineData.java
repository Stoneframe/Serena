package stoneframe.serena.model.routines;

import org.joda.time.LocalDateTime;

import java.util.LinkedList;
import java.util.List;

class DayRoutineData extends RoutineData
{
    final List<Procedure> procedures = new LinkedList<>();

    DayRoutineData(String name, LocalDateTime now)
    {
        super(name, now);
    }
}
