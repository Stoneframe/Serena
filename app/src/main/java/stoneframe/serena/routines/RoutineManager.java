package stoneframe.serena.routines;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

    public List<ProcedureOccurrence> getProceduresForDate(LocalDate date)
    {
        return getContainer().routines.stream()
            .filter(Routine::isEnabled)
            .flatMap(r -> r.getProceduresForDate(date)
                .stream()
                .map(p -> new ProcedureOccurrence(r, p, date.toLocalDateTime(p.getTime()))))
            .sorted()
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

    public List<ProcedureOccurrence> getPendingProcedures()
    {
        return getContainer().routines.stream()
            .filter(Routine::isEnabled)
            .flatMap(r -> r.getPendingProcedures(timeService.getNow()).stream())
            .sorted()
            .collect(Collectors.toList());
    }

    public List<ProcedureOccurrence> getFirstPendingProcedures()
    {
        return getContainer().routines.stream()
            .filter(Routine::isEnabled)
            .map(r -> r.getPendingProcedure(timeService.getNow()))
            .filter(Objects::nonNull)
            .sorted()
            .collect(Collectors.toList());
    }

    public void procedureDone(@NonNull ProcedureOccurrence occurrence)
    {
        Objects.requireNonNull(occurrence);

        Routine<?> routine = getRoutine(occurrence);

        if (routine == null || occurrence.isDone())
        {
            return;
        }

        routine.procedureDone(occurrence);
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

    @Nullable
    private Routine<?> getRoutine(@NonNull ProcedureOccurrence procedure)
    {
        return getContainer().routines.stream()
            .filter(r -> r.equals(procedure.getRoutine()))
            .findFirst()
            .orElse(null);
    }
}
