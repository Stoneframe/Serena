package stoneframe.chorelist.model.routines;

import androidx.annotation.NonNull;

import org.joda.time.LocalDateTime;

import java.util.List;

abstract class RoutineData
{
    protected String name;
    protected boolean isEnabled;

    protected LocalDateTime lastCompleted;

    public RoutineData(String name, LocalDateTime now)
    {
        this.name = name;

        this.lastCompleted = now;

        this.isEnabled = true;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isEnabled()
    {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }

    public void reset(LocalDateTime now)
    {
        lastCompleted = now;
    }

    public abstract List<Procedure> getAllProcedures();

    public abstract LocalDateTime getNextProcedureTime(LocalDateTime now);

    public abstract List<PendingProcedure> getPendingProcedures(LocalDateTime now);

    public PendingProcedure getPendingProcedure(LocalDateTime now)
    {
        return getPendingProcedures(now).stream().findFirst().orElse(null);
    }

    public abstract void procedureDone(PendingProcedure procedure);

    @NonNull
    @Override
    public String toString()
    {
        return name;
    }
}
