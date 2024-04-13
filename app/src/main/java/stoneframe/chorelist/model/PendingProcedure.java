package stoneframe.chorelist.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;

public class PendingProcedure implements Comparable<PendingProcedure>
{
    private final Procedure procedure;
    private final DateTime dateTime;

    public PendingProcedure(Procedure procedure, DateTime dateTime)
    {
        this.procedure = procedure;
        this.dateTime = dateTime;
    }

    public String getDescription()
    {
        return procedure.getDescription();
    }

    public DateTime getDateTime()
    {
        return dateTime;
    }

    @Override
    public int compareTo(PendingProcedure other)
    {
        return this.dateTime.compareTo(other.dateTime);
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
