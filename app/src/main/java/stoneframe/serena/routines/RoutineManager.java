package stoneframe.serena.routines;

import android.util.Pair;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import stoneframe.serena.timeservices.TimeService;

public class RoutineManager
{
    private final Supplier<RoutineContainer> container;

    private final TimeService timeService;

    public RoutineManager(Supplier<RoutineContainer> container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public DayRoutineEditor getDayRoutineEditor(DayRoutine routine)
    {
        return new DayRoutineEditor(this, routine, timeService);
    }

    public WeekRoutineEditor getWeekRoutineEditor(WeekRoutine routine)
    {
        return new WeekRoutineEditor(this, routine, timeService);
    }

    public FortnightRoutineEditor getFortnightRoutineEditor(FortnightRoutine routine)
    {
        return new FortnightRoutineEditor(this, routine, timeService);
    }

    public DayRoutine createDayRoutine()
    {
        return new DayRoutine("", timeService.getNow());
    }

    public WeekRoutine createWeekRoutine()
    {
        return new WeekRoutine("", timeService.getNow());
    }

    public FortnightRoutine createFortnightRoutine()
    {
        return new FortnightRoutine("", timeService.getToday(), timeService.getNow());
    }

    public List<Routine<?>> getAllRoutines()
    {
        return getContainer().routines.stream()
            .sorted(Comparator.comparing(Routine::getName))
            .collect(Collectors.toList());
    }

    public List<Pair<Procedure, Routine<?>>> getProceduresForDate(LocalDate date)
    {
        return getContainer().routines.stream()
            .filter(Routine::isEnabled)
            .flatMap(r -> r.getProceduresForDate(date)
                .stream()
                .map(p -> new Pair<Procedure, Routine<?>>(p, r)))
            .sorted(Comparator.comparing(p -> p.first))
            .collect(Collectors.toList());
    }

    public boolean containsRoutine(Routine<?> routine)
    {
        return getContainer().routines.contains(routine);
    }

    public LocalDateTime getNextProcedureTime()
    {
        return getContainer().routines.stream()
            .filter(Routine::isEnabled)
            .flatMap(r -> Stream.of(r.getNextProcedureTime(timeService.getNow())))
            .filter(Objects::nonNull)
            .sorted()
            .findFirst()
            .orElse(null);
    }

    public List<PendingProcedure> getPendingProcedures()
    {
        return getContainer().routines.stream()
            .filter(Routine::isEnabled)
            .flatMap(r -> r.getPendingProcedures(timeService.getNow()).stream())
            .sorted()
            .collect(Collectors.toList());
    }

    public List<PendingProcedure> getFirstPendingProcedures()
    {
        return getContainer().routines.stream()
            .filter(Routine::isEnabled)
            .map(r -> r.getPendingProcedure(timeService.getNow()))
            .filter(Objects::nonNull)
            .sorted()
            .collect(Collectors.toList());
    }

    public void procedureDone(PendingProcedure procedure)
    {
        Routine<?> routine = getContainer().routines.stream()
            .filter(r -> r.getAllProcedures().contains(procedure.getProcedure()))
            .findFirst()
            .orElse(null);

        assert routine != null;

        routine.procedureDone(procedure);
    }

    void addRoutine(Routine<?> routine)
    {
        getContainer().routines.add(routine);
    }

    void removeRoutine(Routine<?> routine)
    {
        getContainer().routines.remove(routine);
    }

    private RoutineContainer getContainer()
    {
        return container.get();
    }
}
