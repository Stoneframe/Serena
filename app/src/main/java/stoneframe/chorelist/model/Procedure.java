package stoneframe.chorelist.model;

import androidx.annotation.NonNull;

import org.joda.time.LocalTime;

public class Procedure implements Comparable<Procedure>
{
    private final String description;
    private final LocalTime time;

    public Procedure(String description, LocalTime time)
    {
        this.description = description;
        this.time = time;
    }

    public String getDescription()
    {
        return description;
    }

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
