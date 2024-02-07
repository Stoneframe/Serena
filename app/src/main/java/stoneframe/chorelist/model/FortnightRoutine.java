package stoneframe.chorelist.model;

import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.Weeks;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.CheckForNull;

public class FortnightRoutine extends Routine
{
    private final Week week1 = new Week(1);
    private final Week week2 = new Week(1);

    private LocalDate startDate;
    private DateTime lastCompleted;

    public FortnightRoutine(String name, LocalDate startDate, DateTime now)
    {
        super(Routine.FORTNIGHT_ROUTINE, name);

        setStartDate(startDate);

        lastCompleted = now;
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
        DateTime week1Next = getNextProcedureTime(week1, now, isWeek1(now));
        DateTime week2Next = getNextProcedureTime(week2, now, isWeek2(now));

        return getEarliestDateTime(week1Next, week2Next);
    }

    @Override
    public List<Procedure> getPendingProcedures(DateTime now)
    {
        boolean isWeek1 = isWeek1(now);
        boolean isWeek2 = isWeek2(now);

        Map<Procedure, DateTime> week1ProcedureDateTimes = week1.getProcedureDateTimesBefore(now)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> isWeek1 ? e.getValue() : e.getValue().minusWeeks(1)));

        Map<Procedure, DateTime> week2ProcedureDateTimes = week2.getProcedureDateTimesBefore(now)
            .entrySet()
            .stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> isWeek2 ? e.getValue() : e.getValue().minusWeeks(1)));

        return Stream.concat(
                week1ProcedureDateTimes.entrySet().stream(),
                week2ProcedureDateTimes.entrySet().stream())
            .filter(pd -> isPending(now, pd.getValue()))
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @Override
    public void procedureDone(Procedure procedure, DateTime now)
    {
        if (week1.getProcedures().contains(procedure))
        {
            DateTime dateTime = procedure.getTime()
                .toDateTime(now)
                .plusDays(week1.getWeekDay(procedure) - now.getDayOfWeek());

            lastCompleted = isWeek1(now) ? dateTime : dateTime.minusWeeks(1);
        }

        if (week2.getProcedures().contains(procedure))
        {
            DateTime dateTime = procedure.getTime()
                .toDateTime(now)
                .plusDays(week2.getWeekDay(procedure) - now.getDayOfWeek());

            lastCompleted = isWeek2(now) ? dateTime : dateTime.minusWeeks(1);
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

    private boolean isWeek1(DateTime now)
    {
        Weeks weeksSinceStart = Weeks.weeksBetween(startDate, now.toLocalDate());

        return weeksSinceStart.getWeeks() % 2 == 0;
    }

    private boolean isWeek2(DateTime now)
    {
        Weeks weeksSinceStart = Weeks.weeksBetween(startDate, now.toLocalDate());

        return weeksSinceStart.getWeeks() % 2 == 1;
    }

    @CheckForNull
    private DateTime getNextProcedureTime(Week week, DateTime now, boolean isWeek)
    {
        DateTime weekNext = week.getNextProcedureTime(now);

        if (weekNext == null)
        {
            return null;
        }

        return isWeek ? weekNext : weekNext.plusWeeks(1);
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

    private boolean isPending(DateTime now, DateTime procedureDateTime)
    {
        return procedureDateTime.isAfter(lastCompleted)
            && (procedureDateTime.isBefore(now) || procedureDateTime.isEqual(now));
    }
}
