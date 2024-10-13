package stoneframe.chorelist.model.routines;

import org.joda.time.LocalDateTime;

public class DayRoutine extends Routine<DayRoutineData>
{
    DayRoutine(String name, LocalDateTime now)
    {
        super(DAY_ROUTINE, new DayRoutineData(name, now));
    }

    void addProcedure(Procedure procedure)
    {
        data().addProcedure(procedure);
    }

    void removeProcedure(Procedure procedure)
    {
        data().removeProcedure(procedure);
    }
}
