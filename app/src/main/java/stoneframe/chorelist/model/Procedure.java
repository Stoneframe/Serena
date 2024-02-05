package stoneframe.chorelist.model;

import org.joda.time.LocalTime;

public class Procedure implements Comparable<Procedure>
{
    private final String description;
    private final LocalTime time;

    private Routine routine;

    public Procedure(String description, LocalTime time)
    {
        this.description = description;
        this.time = time;
    }

    public String getDescription()
    {
        return description;
    }

    @Override
    public int compareTo(Procedure other)
    {
        return this.time.compareTo(other.time);
    }

    public LocalTime getTime()
    {
        return time;
    }

    public Routine getRoutine()
    {
        return routine;
    }

    void setRoutine(Routine routine)
    {
        this.routine = routine;
    }
}
