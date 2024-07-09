package stoneframe.chorelist.model;

import androidx.annotation.NonNull;

import org.joda.time.LocalDateTime;

import java.util.Objects;

public class PendingProcedure implements Comparable<PendingProcedure>
{
    private final Procedure procedure;
    private final LocalDateTime dateTime;

    public PendingProcedure(Procedure procedure, LocalDateTime dateTime)
    {
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

        return Objects.equals(this.procedure.getDescription(), other.procedure.getDescription())
            && Objects.equals(this.dateTime, other.dateTime);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(procedure.getDescription(), dateTime);
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
