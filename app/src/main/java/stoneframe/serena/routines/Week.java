package stoneframe.serena.routines;

import androidx.annotation.NonNull;

import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Week
{
    private final Day monday;
    private final Day tuesday;
    private final Day wednesday;
    private final Day thursday;
    private final Day friday;
    private final Day saturday;
    private final Day sunday;

    Week(int skip, LocalDate startDate)
    {
        monday = new Day("Monday", startDate.plusDays(0), (skip + 1) * 7);
        tuesday = new Day("Tuesday", startDate.plusDays(1), (skip + 1) * 7);
        wednesday = new Day("Wednesday", startDate.plusDays(2), (skip + 1) * 7);
        thursday = new Day("Thursday", startDate.plusDays(3), (skip + 1) * 7);
        friday = new Day("Friday", startDate.plusDays(4), (skip + 1) * 7);
        saturday = new Day("Saturday", startDate.plusDays(5), (skip + 1) * 7);
        sunday = new Day("Sunday", startDate.plusDays(6), (skip + 1) * 7);
    }

    public Day getMonday()
    {
        return monday;
    }

    public Day getTuesday()
    {
        return tuesday;
    }

    public Day getWednesday()
    {
        return wednesday;
    }

    public Day getThursday()
    {
        return thursday;
    }

    public Day getFriday()
    {
        return friday;
    }

    public Day getSaturday()
    {
        return saturday;
    }

    public Day getSunday()
    {
        return sunday;
    }

    public LocalDate getStartDate()
    {
        return monday.getStartDate();
    }

    public List<Procedure> getProcedures()
    {
        return Stream.of(monday, tuesday, wednesday, thursday, friday, saturday, sunday)
            .flatMap(d -> d.getProcedures().stream())
            .collect(Collectors.toList());
    }

    public LocalDateTime getNextProcedureTime(LocalDateTime now)
    {
        if (getProcedures().isEmpty()) return null;

        return Stream.of(monday, tuesday, wednesday, thursday, friday, saturday, sunday)
            .map(p -> p.getNextProcedureTime(now))
            .filter(Objects::nonNull)
            .sorted()
            .findFirst()
            .get();
    }

    public Day getWeekDay(int dayOfWeek)
    {
        switch (dayOfWeek)
        {
            case DateTimeConstants.MONDAY:
                return monday;
            case DateTimeConstants.TUESDAY:
                return tuesday;
            case DateTimeConstants.WEDNESDAY:
                return wednesday;
            case DateTimeConstants.THURSDAY:
                return thursday;
            case DateTimeConstants.FRIDAY:
                return friday;
            case DateTimeConstants.SATURDAY:
                return saturday;
            case DateTimeConstants.SUNDAY:
                return sunday;
            default:
                throw new IllegalArgumentException();
        }
    }

    public List<PendingProcedure> getPendingProceduresBetween(
        LocalDateTime start,
        LocalDateTime end)
    {
        return this
            .concat(
                getWeekDay(DateTimeConstants.MONDAY).getPendingProceduresBetween(start, end),
                getWeekDay(DateTimeConstants.TUESDAY).getPendingProceduresBetween(start, end),
                getWeekDay(DateTimeConstants.WEDNESDAY).getPendingProceduresBetween(start, end),
                getWeekDay(DateTimeConstants.THURSDAY).getPendingProceduresBetween(start, end),
                getWeekDay(DateTimeConstants.FRIDAY).getPendingProceduresBetween(start, end),
                getWeekDay(DateTimeConstants.SATURDAY).getPendingProceduresBetween(start, end),
                getWeekDay(DateTimeConstants.SUNDAY).getPendingProceduresBetween(start, end))
            .sorted()
            .collect(Collectors.toList());
    }

    void setStartDate(LocalDate startDate)
    {
        monday.setStartDate(startDate.plusDays(0));
        tuesday.setStartDate(startDate.plusDays(1));
        wednesday.setStartDate(startDate.plusDays(2));
        thursday.setStartDate(startDate.plusDays(3));
        friday.setStartDate(startDate.plusDays(4));
        saturday.setStartDate(startDate.plusDays(5));
        sunday.setStartDate(startDate.plusDays(6));
    }

    @SafeVarargs
    private final Stream<PendingProcedure> concat(@NonNull List<PendingProcedure>... pendingProcedures)
    {
        Stream<PendingProcedure> stream = Stream.of();

        for (List<PendingProcedure> procedures : pendingProcedures)
        {
            stream = Stream.concat(stream, procedures.stream());
        }

        return stream;
    }
}
