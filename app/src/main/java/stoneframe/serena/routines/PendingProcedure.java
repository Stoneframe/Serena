package stoneframe.serena.routines;

import androidx.annotation.NonNull;

import org.joda.time.LocalDateTime;

import java.util.Objects;

public class PendingProcedure implements Comparable<PendingProcedure>
{
    private final Routine<?> routine;
    private final Procedure procedure;
    private final LocalDateTime dateTime;

    PendingProcedure(Routine<?> routine, Procedure procedure, LocalDateTime dateTime)
    {
        this.routine = routine;
        this.procedure = procedure;
        this.dateTime = dateTime;
    }

    public String getDescription()
    {
        return procedure.getDescription();
    }

    public LocalDateTime getDateTime()
    {
        return dateTime;
    }

    @Override
    public int compareTo(PendingProcedure other)
    {
        return this.dateTime.compareTo(other.dateTime);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        PendingProcedure other = (PendingProcedure)obj;

        return this.routine == other.routine
            && this.procedure == other.procedure
            && Objects.equals(this.dateTime, other.dateTime);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(System.identityHashCode(procedure), dateTime);
    }

    @NonNull
    @Override
    public String toString()
    {
        return procedure.toString();
    }

    Routine<?> getRoutine()
    {
        return routine;
    }

    Procedure getProcedure()
    {
        return procedure;
    }
}
