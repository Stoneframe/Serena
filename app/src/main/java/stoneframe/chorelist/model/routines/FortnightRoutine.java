package stoneframe.chorelist.model.routines;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.CheckForNull;

public class FortnightRoutine extends Routine
{
    private final Week week1;
    private final Week week2;

    public FortnightRoutine(String name, LocalDate startDate, LocalDateTime now)
    {
        super(Routine.FORTNIGHT_ROUTINE, name, now);

        week1 = new Week(1, getMondayOfWeek(startDate).plusWeeks(0));
        week2 = new Week(1, getMondayOfWeek(startDate).plusWeeks(1));
    }

    public LocalDate getStartDate()
    {
        return week1.getStartDate();
    }

    public void setStartDate(LocalDate startDate)
    {
        week1.setStartDate(getMondayOfWeek(startDate).plusWeeks(0));
        week2.setStartDate(getMondayOfWeek(startDate).plusWeeks(1));
    }

    @Override
    public List<Procedure> getAllProcedures()
    {
        return Stream.concat(week1.getProcedures().stream(), week2.getProcedures().stream())
            .collect(Collectors.toList());
    }

    @Nullable
    @Override
    public LocalDateTime getNextProcedureTime(LocalDateTime now)
    {
        LocalDateTime week1Next = getNextProcedureTime(week1, now);
        LocalDateTime week2Next = getNextProcedureTime(week2, now);

        return getEarliestDateTime(week1Next, week2Next);
    }

    @Override
    public List<PendingProcedure> getPendingProcedures(LocalDateTime now)
    {
        List<PendingProcedure> week1PendingProcedures = week1.getPendingProceduresBetween(
            lastCompleted,
            now);

        List<PendingProcedure> week2PendingProcedures = week2.getPendingProceduresBetween(
            lastCompleted,
            now);

        return Stream.concat(week1PendingProcedures.stream(), week2PendingProcedures.stream())
            .sorted()
            .collect(Collectors.toList());
    }

    @Override
    public void procedureDone(PendingProcedure procedure)
    {
        lastCompleted = procedure.getDateTime();
    }

    public Week getWeek1()
    {
        return week1;
    }

    public Week getWeek2()
    {
        return week2;
    }

    public Week getWeek(int nbr)
    {
        switch (nbr)
        {
            case 1:
                return week1;
            case 2:
                return week2;
            default:
                throw new IllegalArgumentException();
        }
    }

    @CheckForNull
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
