package stoneframe.chorelist.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.LocalTime;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) return false;

        Procedure other = (Procedure)obj;

        return Objects.equals(this.description, other.description)
            && Objects.equals(this.time, other.time);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(description, time);
    }

    Procedure copy()
    {
        return new Procedure(description, time);
    }
}
