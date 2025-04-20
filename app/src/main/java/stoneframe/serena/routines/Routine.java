package stoneframe.serena.routines;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;

import stoneframe.serena.util.Revertible;

public abstract class Routine<T extends RoutineData> extends Revertible<T>
{
    public static final int DAY_ROUTINE = 0;
    public static final int WEEK_ROUTINE = 1;
    public static final int FORTNIGHT_ROUTINE = 2;

    private final int routineType;

    protected Routine(int routineType, T data)
    {
        super(data);

        this.routineType = routineType;
    }

    public int getRoutineType()
    {
        return routineType;
    }

    public String getName()
    {
        return data().name;
    }

    public void setName(String name)
    {
        data().name = name;
    }

    public boolean isEnabled()
    {
        return data().isEnabled;
    }

    public void setEnabled(boolean isEnabled)
    {
        data().isEnabled = isEnabled;
    }

    public abstract List<Procedure> getAllProcedures();

    PendingProcedure getPendingProcedure(LocalDateTime now)
    {
        return getPendingProcedures(now).stream().findFirst().orElse(null);
    }

    abstract List<PendingProcedure> getPendingProcedures(LocalDateTime now);

    abstract LocalDateTime getNextProcedureTime(LocalDateTime now);

    abstract List<Procedure> getProceduresForDate(LocalDate date);

    abstract void procedureDone(PendingProcedure procedure);

    void reset(LocalDateTime now)
    {
        data().lastCompleted = now;
    }
}
