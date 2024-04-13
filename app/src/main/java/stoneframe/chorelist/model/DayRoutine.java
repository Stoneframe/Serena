package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
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
    public List<PendingProcedure> getPendingProcedures(DateTime now)
    {
        return procedures.stream()
            .flatMap(p ->
            {
                List<PendingProcedure> pendingProcedures = new LinkedList<>();

                for (DateTime i = lastCompleted.withTimeAtStartOfDay();
                     i.isBefore(now);
                     i = i.plusDays(1))
                {
                    PendingProcedure pendingProcedure = new PendingProcedure(
                        p,
                        p.getTime().toDateTime(i));

                    pendingProcedures.add(pendingProcedure);
                }

                return pendingProcedures.stream();
            })
            .filter(p -> p.getDateTime().isAfter(lastCompleted))
            .filter(p -> p.getDateTime().isBefore(now) || p.getDateTime().isEqual(now))
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    public void procedureDone(PendingProcedure procedure)
    {
        lastCompleted = procedure.getDateTime();
    }
}
