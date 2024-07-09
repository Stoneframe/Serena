package stoneframe.chorelist.model;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

public class DayRoutine extends Routine
{
    private final List<Procedure> procedures = new LinkedList<>();

    public DayRoutine(String name, LocalDateTime now)
    {
        super(DAY_ROUTINE, name, now);
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
    public LocalDateTime getNextProcedureTime(LocalDateTime now)
    {
        if (procedures.isEmpty()) return null;

        return procedures.stream()
            .sorted()
            .filter(p -> p.getTime().isAfter(now.toLocalTime()))
            .map(p -> now.withTime(0, 0, 0, 0)
                .plusHours(p.getTime().getHourOfDay())
                .plusMinutes(p.getTime().getMinuteOfHour()))
            .findFirst()
            .orElse(now.withTime(0, 0, 0, 0)
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
    public List<PendingProcedure> getPendingProcedures(LocalDateTime now)
    {
        return procedures.stream()
            .flatMap(p ->
            {
                List<PendingProcedure> pendingProcedures = new LinkedList<>();

                for (LocalDate i = lastCompleted.toLocalDate(); i.isBefore(now.toLocalDate()); i = i.plusDays(1))
                {
                    PendingProcedure pendingProcedure = new PendingProcedure(
                        p,
                        i.toLocalDateTime(p.getTime()));

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
