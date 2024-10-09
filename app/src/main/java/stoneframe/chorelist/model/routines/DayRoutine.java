package stoneframe.chorelist.model.routines;

import org.joda.time.LocalDateTime;

public class DayRoutine extends Routine<DayRoutineData>
{
    public DayRoutine(String name, LocalDateTime now)
    {
        super(DAY_ROUTINE, new DayRoutineData(name, now));
    }

    public void addProcedure(Procedure procedure)
    {
        data().addProcedure(procedure);
    }

    public void removeProcedure(Procedure procedure)
    {
        data().removeProcedure(procedure);
    }
}
