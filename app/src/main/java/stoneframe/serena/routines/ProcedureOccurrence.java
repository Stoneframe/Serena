package stoneframe.serena.routines;

import androidx.annotation.NonNull;

import org.joda.time.LocalDateTime;

import java.util.Objects;

public class ProcedureOccurrence implements Comparable<ProcedureOccurrence>
{
    private final Routine<?> routine;
    private final Procedure procedure;
    private final LocalDateTime dateTime;

    ProcedureOccurrence(Routine<?> routine, Procedure procedure, LocalDateTime dateTime)
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

    public boolean isDone()
    {
        return routine.isDone(dateTime.toLocalDate(), procedure);
    }

    @Override
    public int compareTo(ProcedureOccurrence other)
    {
        return this.dateTime.compareTo(other.dateTime);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        ProcedureOccurrence other = (ProcedureOccurrence)obj;

        return this.routine == other.routine
            && this.procedure == other.procedure
            && Objects.equals(this.dateTime, other.dateTime);
    }

    public Routine<?> getRoutine()
    {
        return routine;
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

    Procedure getProcedure()
    {
        return procedure;
    }
}
