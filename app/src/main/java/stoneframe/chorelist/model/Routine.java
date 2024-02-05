package stoneframe.chorelist.model;

import org.joda.time.LocalTime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

public class Routine
{
    private final List<Procedure> procedures = new LinkedList<>();

    private final UUID id;
    private final String name;

    private LocalTime lastCompleted;

    public Routine(String name, LocalTime now)
    {
        this.name = name;
        this.lastCompleted = now;

        id = UUID.randomUUID();
    }

    public UUID getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public List<Procedure> getAllProcedures()
    {
        return Collections.unmodifiableList(procedures.stream()
            .sorted()
            .collect(Collectors.toList()));
    }

    @CheckForNull
    public Procedure getNextProcedure(LocalTime now)
    {
        return procedures.stream()
            .sorted()
            .filter(p -> p.getTime().isAfter(now))
            .findFirst()
            .orElse(null);
    }

    public Procedure getPreviosusProcedure(LocalTime now)
    {
        return procedures.stream()
            .sorted()
            .filter(p -> p.getTime().isBefore(now))
            .reduce((first, second) -> second)
            .orElse(null);
    }

    public void addProcedure(Procedure procedure)
    {
        procedures.add(procedure);
        procedure.setRoutine(this);
    }

    public void removeProcedure(Procedure procedure)
    {
        procedures.remove(procedure);
        procedure.setRoutine(null);
    }

    public List<Procedure> getPendingProcedures(LocalTime now)
    {
        return procedures.stream()
            .sorted()
            .filter(p -> p.getTime().isAfter(lastCompleted))
            .filter(p -> p.getTime().isBefore(now))
            .collect(Collectors.toList());
    }

    public void completeProcedure(Procedure procedure)
    {
        lastCompleted = procedure.getTime();
    }
}
