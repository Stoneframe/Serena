package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

public class DayRoutine extends Routine
{
    private final List<Procedure> procedures = new LinkedList<>();

    private DateTime lastCompleted;

    public DayRoutine(String name, DateTime now)
    {
        super(DAY_ROUTINE, name);

        this.lastCompleted = now;
    }

    private DayRoutine(
        UUID identity,
        String name,
        DateTime lastCompleted,
        List<Procedure> procedures)
    {
        super(identity, DAY_ROUTINE, name);

        this.lastCompleted = lastCompleted;

        this.procedures.addAll(procedures);
    }

    @Override
    public List<Procedure> getAllProcedures()
    {
        return Collections.unmodifiableList(procedures.stream()
            .sorted()
            .collect(Collectors.toList()));
    }

    @Override
    @CheckForNull
    public DateTime getNextProcedureTime(DateTime now)
    {
        if (procedures.isEmpty()) return null;

        return procedures.stream()
            .sorted()
            .filter(p -> p.getTime().isAfter(now.toLocalTime()))
            .map(p -> now.withTimeAtStartOfDay()
                .plusHours(p.getTime().getHourOfDay())
                .plusMinutes(p.getTime().getMinuteOfHour()))
            .findFirst()
            .orElse(now.withTimeAtStartOfDay()
                .plusDays(1)
                .plusHours(procedures.get(0).getTime().getHourOfDay())
                .plusMinutes(procedures.get(0).getTime().getMinuteOfHour()));
    }

    public void addProcedure(Procedure procedure)
    {
        procedures.add(procedure);
    }

    public void removeProcedure(Procedure procedure)
    {
        procedures.remove(procedure);
    }

    @Override
    public List<Procedure> getPendingProcedures(DateTime now)
    {
        return procedures.stream()
            .sorted()
            .filter(p -> isPending(p, lastCompleted, now))
            .collect(Collectors.toList());
    }

    @Override
    public void procedureDone(Procedure procedure, DateTime now)
    {
        lastCompleted = procedure.getTime().toDateTime(now);
    }

    @Override
    Routine copy()
    {
        return new DayRoutine(
            identity,
            name,
            lastCompleted,
            procedures.stream().map(Procedure::copy).collect(Collectors.toList()));
    }

    private static boolean isPending(Procedure procedure, DateTime lastCompleted, DateTime now)
    {
        DateTime next = procedure.getTime().toDateTime(lastCompleted);

        if (next.isBefore(lastCompleted) || next.isEqual(lastCompleted))
        {
            next = next.plusDays(1);
        }

        return next.isBefore(now);
    }
}
