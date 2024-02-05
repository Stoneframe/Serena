package stoneframe.chorelist.model;

import org.joda.time.DateTime;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public class RoutineManager
{
    private final List<Routine> routines = new LinkedList<>();

    private final TimeService timeService;

    public RoutineManager(TimeService timeService)
    {
        this.timeService = timeService;
    }

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

    public Procedure getNextProcedure()
    {
        DateTime now = timeService.getNow();

        return routines.stream()
            .flatMap(r -> Stream.of(r.getNextProcedure(now.toLocalTime())))
            .filter(Objects::nonNull)
            .sorted()
            .findFirst()
            .orElse(null);
    }

    public Procedure getPreviousProcedure()
    {
        DateTime now = timeService.getNow();

        return routines.stream()
            .flatMap(r -> Stream.of(r.getPreviosusProcedure(now.toLocalTime())))
            .filter(Objects::nonNull)
            .sorted()
            .reduce((first, second) -> second)
            .orElse(null);
    }
}
