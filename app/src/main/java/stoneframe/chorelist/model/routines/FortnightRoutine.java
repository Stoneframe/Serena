package stoneframe.chorelist.model.routines;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FortnightRoutine extends Routine<FortnightRoutineData>
{
    FortnightRoutine(String name, LocalDate startDate, LocalDateTime now)
    {
        super(
            FORTNIGHT_ROUTINE,
            new FortnightRoutineData(name,
                now,
                new Week(1, getMondayOfWeek(startDate).plusWeeks(0)),
                new Week(1, getMondayOfWeek(startDate).plusWeeks(1))));
    }

    public LocalDate getStartDate()
    {
        return data().week1.getStartDate();
    }

    public Week getWeek1()
    {
        return data().week1;
    }

    public Week getWeek2()
    {
        return data().week2;
    }

    public Week getWeek(int nbr)
    {
        switch (nbr)
        {
            case 1:
                return data().week1;
            case 2:
                return data().week2;
            default:
                throw new IllegalArgumentException();
        }
    }

    @Override
    public List<Procedure> getAllProcedures()
    {
        return Stream.concat(
                data().week1.getProcedures().stream(),
                data().week2.getProcedures().stream())
            .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public LocalDateTime getNextProcedureTime(LocalDateTime now)
    {
        LocalDateTime week1Next = getNextProcedureTime(data().week1, now);
        LocalDateTime week2Next = getNextProcedureTime(data().week2, now);

        return getEarliestDateTime(week1Next, week2Next);
    }

    @Override
    public List<PendingProcedure> getPendingProcedures(LocalDateTime now)
    {
        List<PendingProcedure> week1PendingProcedures = data().week1.getPendingProceduresBetween(
            data().lastCompleted,
            now);

        List<PendingProcedure> week2PendingProcedures = data().week2.getPendingProceduresBetween(
            data().lastCompleted,
            now);

        return Stream.concat(week1PendingProcedures.stream(), week2PendingProcedures.stream())
            .sorted()
            .collect(Collectors.toList());
    }

    void setStartDate(LocalDate startDate)
    {
        data().week1.setStartDate(getMondayOfWeek(startDate).plusWeeks(0));
        data().week2.setStartDate(getMondayOfWeek(startDate).plusWeeks(1));
    }

    @Override
    void procedureDone(PendingProcedure procedure)
    {
        data().lastCompleted = procedure.getDateTime();
    }

    private LocalDateTime getNextProcedureTime(Week week, LocalDateTime now)
    {
        return week.getNextProcedureTime(now);
    }

    @Nullable
    private static LocalDateTime getEarliestDateTime(
        LocalDateTime week1Next,
        LocalDateTime week2Next)
    {
        return Stream.of(week1Next, week2Next)
            .filter(Objects::nonNull)
            .sorted()
            .findFirst()
            .orElse(null);
    }

    @NonNull
    private static LocalDate getMondayOfWeek(LocalDate startDate)
    {
        return startDate.minusDays(startDate.getDayOfWeek() - 1);
    }
}
