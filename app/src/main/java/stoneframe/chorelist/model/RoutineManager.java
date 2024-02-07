package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.Collections;
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
        return Collections.unmodifiableList(routines);
    }

    public DateTime getNextProcedureTime(DateTime now)
    {
        return routines.stream()
            .flatMap(r -> Stream.of(r.getNextProcedureTime(now)))
            .filter(Objects::nonNull)
            .sorted()
            .findFirst()
            .orElse(null);
    }

    public List<Procedure> getPendingProcedures(DateTime now)
    {
        return routines.stream()
            .flatMap(r -> r.getPendingProcedures(now).stream())
            .collect(Collectors.toList());
    }

    public void procedureDone(Procedure procedure, DateTime now)
    {
        Routine routine = routines.stream()
            .filter(r -> r.getAllProcedures().contains(procedure))
            .findFirst()
            .orElse(null);

        assert routine != null;

        routine.procedureDone(procedure, now);
    }
}
