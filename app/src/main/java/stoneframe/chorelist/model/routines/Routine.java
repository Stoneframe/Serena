package stoneframe.chorelist.model.routines;

import org.joda.time.LocalDateTime;

import java.util.List;

public abstract class Routine
{
    public static final int DAY_ROUTINE = 0;
    public static final int WEEK_ROUTINE = 1;
    public static final int FORTNIGHT_ROUTINE = 2;

    private final int routineType;

    protected Routine(int routineType)
    {
        this.routineType = routineType;
    }

    public int getRoutineType()
    {
        return routineType;
    }

    public String getName()
    {
        return data().getName();
    }

    public void setName(String name)
    {
        data().setName(name);
    }

    public boolean isEnabled()
    {
        return data().isEnabled();
    }

    public void setEnabled(boolean isEnabled)
    {
        data().setEnabled(isEnabled);
    }

    public List<Procedure> getAllProcedures()
    {
        return data().getAllProcedures();
    }

    public LocalDateTime getNextProcedureTime(LocalDateTime now)
    {
        return data().getNextProcedureTime(now);
    }

    public List<PendingProcedure> getPendingProcedures(LocalDateTime now)
    {
        return data().getPendingProcedures(now);
    }

    public PendingProcedure getPendingProcedure(LocalDateTime now)
    {
        return data().getPendingProcedure(now);
    }

    public void procedureDone(PendingProcedure procedure)
    {
        data().procedureDone(procedure);
    }

    void reset(LocalDateTime now)
    {
        data().reset(now);
    }

    public void edit()
    {
        save();
    }

    public abstract void save();

    public abstract void revert();

    abstract RoutineData data();
}
