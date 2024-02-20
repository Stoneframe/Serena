package stoneframe.chorelist.model;

import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.CheckForNull;

public class FortnightRoutine extends Routine
{
    private final Week week1;
    private final Week week2;

    private LocalDate startDate;
    private DateTime lastCompleted;

    public FortnightRoutine(String name, LocalDate startDate, DateTime now)
    {
        super(Routine.FORTNIGHT_ROUTINE, name);

        setStartDate(startDate);

        lastCompleted = now;

        week1 = new Week(1, startDate.plusWeeks(0));
        week2 = new Week(1, startDate.plusWeeks(1));
    }

    public LocalDate getStartDate()
    {
        return startDate;
    }

    public void setStartDate(LocalDate startDate)
    {
        this.startDate = startDate.minusDays(startDate.getDayOfWeek() - 1);
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
    public List<Procedure> getPendingProcedures(DateTime now)
    {
        Stream<Map.Entry<Procedure, DateTime>> week1ProcedureDateTimes = week1
            .getProcedureDateTimesBetween(lastCompleted, now)
            .entrySet()
            .stream();

        Stream<Map.Entry<Procedure, DateTime>> week2ProcedureDateTimes = week2
            .getProcedureDateTimesBetween(lastCompleted, now)
            .entrySet()
            .stream();

        return Stream.concat(week1ProcedureDateTimes, week2ProcedureDateTimes)
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @Override
    public void procedureDone(Procedure procedure, DateTime now)
    {
        if (week1.getProcedures().contains(procedure))
        {
            lastCompleted = week1.getNextTimeOfProcedureAfter(procedure, lastCompleted);
        }

        if (week2.getProcedures().contains(procedure))
        {
            lastCompleted = week2.getNextTimeOfProcedureAfter(procedure, lastCompleted);
        }
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
}
