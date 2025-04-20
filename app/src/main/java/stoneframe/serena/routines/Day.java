package stoneframe.serena.routines;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class Day
{
    private final List<Procedure> procedures = new LinkedList<>();

    private final String name;
    private final int interval;

    private LocalDate startDate;

    Day(String name, LocalDate startDate, int interval)
    {
        this.name = name;
        this.interval = interval;
        this.startDate = startDate;
    }

    public String getName()
    {
        return name;
    }

    public LocalDate getStartDate()
    {
        return startDate;
    }

    void setStartDate(LocalDate startDate)
    {
        this.startDate = startDate;
    }

    public List<Procedure> getProcedures()
    {
        return procedures.stream().sorted().collect(Collectors.toList());
    }

    public LocalDateTime getNextProcedureTime(LocalDateTime now)
    {
        if (procedures.isEmpty()) return null;

        return procedures.stream()
            .map(p -> getNextTimeOfProcedureAfter(p, now))
            .sorted()
            .findFirst()
            .get();
    }

    public LocalDateTime getNextTimeOfProcedureAfter(
        Procedure procedure,
        LocalDateTime dateTime)
    {
        LocalDateTime nextTime = startDate.toLocalDateTime(procedure.getTime());

        while (!nextTime.isAfter(dateTime))
        {
            nextTime = nextTime.plusDays(interval);
        }

        return nextTime;
    }

    public List<PendingProcedure> getPendingProceduresBetween(
        LocalDateTime start,
        LocalDateTime end)
    {
        return procedures.stream()
            .flatMap(p ->
            {
                List<PendingProcedure> pendingProcedures = new LinkedList<>();

                for (LocalDateTime i = startDate.toLocalDateTime(LocalTime.MIDNIGHT);
                     i.isBefore(end);
                     i = i.plusDays(interval))
                {
                    if (i.isBefore(start.withTime(0, 0, 0, 0)))
                    {
                        continue;
                    }

                    PendingProcedure pendingProcedure = new PendingProcedure(
                        p,
                        i.toLocalDate().toLocalDateTime(p.getTime()));

                    pendingProcedures.add(pendingProcedure);
                }

                return pendingProcedures.stream();
            })
            .filter(p -> p.getDateTime().isAfter(start))
            .filter(p -> p.getDateTime().isBefore(end) || p.getDateTime().isEqual(end))
            .sorted()
            .collect(Collectors.toList());
    }

    public boolean isToday(LocalDate today)
    {
        LocalDate nextTime = startDate;

        while (nextTime.isBefore(today))
        {
            nextTime = nextTime.plusDays(interval);
        }

        return nextTime.equals(today);
    }

    void addProcedure(Procedure procedure)
    {
        if (procedures.contains(procedure))
        {
            return;
        }

        procedures.add(procedure);
    }

    void removeProcedure(Procedure procedure)
    {
        procedures.remove(procedure);
    }
}
