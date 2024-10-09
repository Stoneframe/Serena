package stoneframe.chorelist.model.routines;

import org.joda.time.LocalDateTime;

import stoneframe.chorelist.model.util.DeepCopy;

public class DayRoutine extends Routine
{
    private DayRoutineData data;

    private transient DayRoutineData checkpoint;

    public DayRoutine(String name, LocalDateTime now)
    {
        super(DAY_ROUTINE);

        data = new DayRoutineData(name, now);
    }

    public void addProcedure(Procedure procedure)
    {
        data.addProcedure(procedure);
    }

    public void removeProcedure(Procedure procedure)
    {
        data.removeProcedure(procedure);
    }

    @Override
    public void save()
    {
        checkpoint = DeepCopy.copy(data);
    }

    @Override
    public void revert()
    {
        data = checkpoint;
    }

    @Override
    RoutineData data()
    {
        return data;
    }
}
