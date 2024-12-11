package stoneframe.serena.model.routines;

import androidx.annotation.NonNull;

import org.joda.time.LocalTime;

public class Procedure implements Comparable<Procedure>
{
    private final String description;
    private final LocalTime time;

    public Procedure(String description, @NonNull LocalTime time)
    {
        this.description = description.trim();
        this.time = time;
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

    @Override
    public int compareTo(Procedure other)
    {
        return this.time.compareTo(other.time);
    }

    @NonNull
    @Override
    public String toString()
    {
        return time.toString("HH:mm") + " - " + description;
    }
}
