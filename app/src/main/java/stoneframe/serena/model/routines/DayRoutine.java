package stoneframe.serena.model.routines;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class DayRoutine extends Routine<DayRoutineData>
{
    DayRoutine(String name, LocalDateTime now)
    {
        super(DAY_ROUTINE, new DayRoutineData(name.trim(), now));
    }

    @Override
    public List<Procedure> getAllProcedures()
    {
        return Collections.unmodifiableList(data().procedures.stream()
            .sorted()
            .collect(Collectors.toList()));
    }

    @Override
    List<PendingProcedure> getPendingProcedures(LocalDateTime now)
    {
        return data().procedures.stream()
            .flatMap(p ->
            {
                List<PendingProcedure> pendingProcedures = new LinkedList<>();

                for (LocalDateTime i = data().lastCompleted.toLocalDate()
                    .toLocalDateTime(LocalTime.MIDNIGHT);
                     i.isBefore(now);
                     i = i.plusDays(1))
                {
                    PendingProcedure pendingProcedure = new PendingProcedure(
                        p,
                        i.toLocalDate().toLocalDateTime(p.getTime()));

                    pendingProcedures.add(pendingProcedure);
                }

                return pendingProcedures.stream();
            })
            .filter(p -> p.getDateTime().isAfter(data().lastCompleted))
            .filter(p -> p.getDateTime().isBefore(now) || p.getDateTime().isEqual(now))
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    LocalDateTime getNextProcedureTime(LocalDateTime now)
    {
        if (data().procedures.isEmpty()) return null;

        return data().procedures.stream()
            .sorted()
            .filter(p -> p.getTime().isAfter(now.toLocalTime()))
            .map(p -> now.withTime(0, 0, 0, 0)
                .plusHours(p.getTime().getHourOfDay())
                .plusMinutes(p.getTime().getMinuteOfHour()))
            .findFirst()
            .orElse(now.withTime(0, 0, 0, 0)
                .plusDays(1)
                .plusHours(data().procedures.get(0).getTime().getHourOfDay())
                .plusMinutes(data().procedures.get(0).getTime().getMinuteOfHour()));
    }

    @Override
    List<Procedure> getProceduresForDate(LocalDate date)
    {
        return Collections.unmodifiableList(data().procedures);
    }

    @Override
    void procedureDone(PendingProcedure procedure)
    {
        data().lastCompleted = procedure.getDateTime();
    }

    void addProcedure(Procedure procedure)
    {
        data().procedures.add(procedure);
    }

    void removeProcedure(Procedure procedure)
    {
        data().procedures.remove(procedure);
    }
}
