package stoneframe.chorelist.model.routines;

import org.joda.time.LocalDateTime;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class RoutineManager
{
    private final List<Routine> routines = new LinkedList<>();

    public void addRoutine(Routine routine)
    {
        routines.add(routine);
    }

    public void removeRoutine(Routine routine)
    {
        routines.remove(routine);
    }

    public List<Routine> getAllRoutines()
    {
        return routines.stream()
            .sorted(Comparator.comparing(Routine::getName))
            .collect(Collectors.toList());
    }

    public LocalDateTime getNextProcedureTime(LocalDateTime now)
    {
        return routines.stream()
            .filter(Routine::isEnabled)
            .flatMap(r -> Stream.of(r.getNextProcedureTime(now)))
            .filter(Objects::nonNull)
            .sorted()
            .findFirst()
            .orElse(null);
    }

    public List<PendingProcedure> getPendingProcedures(LocalDateTime now)
    {
        return routines.stream()
            .filter(Routine::isEnabled)
            .flatMap(r -> r.getPendingProcedures(now).stream())
            .sorted()
            .collect(Collectors.toList());
    }

    public List<PendingProcedure> getFirstPendingProcedures(LocalDateTime now)
    {
        return routines.stream()
            .filter(Routine::isEnabled)
            .map(r -> r.getPendingProcedure(now))
            .filter(Objects::nonNull)
            .sorted()
            .collect(Collectors.toList());
    }

    public void procedureDone(PendingProcedure procedure)
    {
        Routine routine = routines.stream()
            .filter(r -> r.getAllProcedures().contains(procedure.getProcedure()))
            .findFirst()
            .orElse(null);

        assert routine != null;

        routine.procedureDone(procedure);
    }

    public void resetRoutine(Routine routine, LocalDateTime now)
    {
        routine.reset(now);
    }
}
