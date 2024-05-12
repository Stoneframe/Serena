package stoneframe.chorelist.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.CheckForNull;

public class FortnightRoutine extends Routine
{
    private final Week week1;
    private final Week week2;

    public FortnightRoutine(String name, LocalDate startDate, DateTime now)
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
    public DateTime getNextProcedureTime(DateTime now)
    {
        DateTime week1Next = getNextProcedureTime(week1, now);
        DateTime week2Next = getNextProcedureTime(week2, now);

        return getEarliestDateTime(week1Next, week2Next);
    }

    @Override
    public List<PendingProcedure> getPendingProcedures(DateTime now)
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
    private DateTime getNextProcedureTime(Week week, DateTime now)
    {
        return week.getNextProcedureTime(now);
    }

    @Nullable
    private static DateTime getEarliestDateTime(DateTime week1Next, DateTime week2Next)
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
