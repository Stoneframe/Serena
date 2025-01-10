package stoneframe.serena.model.routines;

import androidx.annotation.NonNull;

import org.joda.time.LocalTime;

import java.util.Objects;

public class Procedure implements Comparable<Procedure>
{
    private final String description;
    private final LocalTime time;
    private final boolean hasAlarm;

    public Procedure(String description, @NonNull LocalTime time, boolean hasAlarm)
    {
        this.description = description;
        this.time = time;
        this.hasAlarm = hasAlarm;
    }

    public String getDescription()
    {
        return description;
    }

    @NonNull
    public LocalTime getTime()
    {
        return time;
    }

    public boolean hasAlarm()
    {
        return hasAlarm;
    }

    public Procedure copy()
    {
        return new Procedure(description, time, hasAlarm);
    }

    @Override
    public int compareTo(Procedure other)
    {
        return this.time.compareTo(other.time);
    }

    @Override
    public boolean equals(Object other)
    {
        if (this == other) return true;

        if (other == null || getClass() != other.getClass()) return false;

        Procedure procedure = (Procedure)other;

        return Objects.equals(description, procedure.description)
            && Objects.equals(time, procedure.time);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(description, time);
    }

    @NonNull
    @Override
    public String toString()
    {
        return time.toString("HH:mm") + " - " + description;
    }
}
