package stoneframe.chorelist.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;
import org.joda.time.LocalDate;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.CheckForNull;

public abstract class Routine
{
    public static final int DAY_ROUTINE = 0;
    public static final int WEEK_ROUTINE = 1;
    public static final int FORTNIGHT_ROUTINE = 2;

    private final int routineType;

    protected String name;
    protected boolean isEnabled;

    protected DateTime lastCompleted;

    public Routine(int routineType, String name, DateTime now)
    {
        this.routineType = routineType;
        this.name = name;

        this.lastCompleted = now;

        this.isEnabled = true;
    }

    public int getRoutineType()
    {
        return routineType;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isEnabled()
    {
        return isEnabled;
    }

    public void setEnabled(boolean isEnabled)
    {
        this.isEnabled = isEnabled;
    }

    public abstract List<Procedure> getAllProcedures();

    @CheckForNull
    public abstract DateTime getNextProcedureTime(DateTime now);

    public abstract List<PendingProcedure> getPendingProcedures(DateTime now);

    public PendingProcedure getPendingProcedure(DateTime now)
    {
        return getPendingProcedures(now).stream().findFirst().orElse(null);
    }

    public abstract void procedureDone(PendingProcedure procedure);

    @NonNull
    @Override
    public String toString()
    {
        return name;
    }

    public static class Day
    {
        private final List<Procedure> procedures = new LinkedList<>();

        private final String name;
        private final int interval;

        private LocalDate startDate;

        public Day(String name, LocalDate startDate, int interval)
        {
            this.name = name;
            this.interval = interval;
            this.startDate = startDate;
        }

        public String getName()
        {
            return name;
        }

        public LocalDate getStartDate()
        {
            return startDate;
        }

        public void setStartDate(LocalDate startDate)
        {
            this.startDate = startDate;
        }

        public List<Procedure> getProcedures()
        {
            return procedures.stream().sorted().collect(Collectors.toList());
        }

        public void addProcedure(Procedure procedure)
        {
            procedures.add(procedure);
        }

        public void removeProcedure(Procedure procedure)
        {
            procedures.remove(procedure);
        }

        public DateTime getNextProcedureTime(DateTime now)
        {
            if (procedures.isEmpty()) return null;

            return procedures.stream()
                .map(p -> getNextTimeOfProcedureAfter(p, now))
                .sorted()
                .findFirst()
                .get();
        }

        public DateTime getNextTimeOfProcedureAfter(Procedure procedure, DateTime dateTime)
        {
            DateTime nextTime = startDate.toDateTime(procedure.getTime());

            while (!nextTime.isAfter(dateTime))
            {
                nextTime = nextTime.plusDays(interval);
            }

            return nextTime;
        }

        public List<PendingProcedure> getPendingProceduresBetween(DateTime start, DateTime end)
        {
            return procedures.stream()
                .flatMap(p ->
                {
                    List<PendingProcedure> pendingProcedures = new LinkedList<>();

                    for (DateTime i = startDate.toDateTimeAtStartOfDay();
                         i.isBefore(end);
                         i = i.plusDays(interval))
                    {
                        if (i.isBefore(start.withTimeAtStartOfDay()))
                        {
                            continue;
                        }

                        PendingProcedure pendingProcedure = new PendingProcedure(
                            p,
                            p.getTime().toDateTime(i));

                        pendingProcedures.add(pendingProcedure);
                    }

                    return pendingProcedures.stream();
                })
                .filter(p -> p.getDateTime().isAfter(start))
                .filter(p -> p.getDateTime().isBefore(end) || p.getDateTime().isEqual(end))
                .sorted()
                .collect(Collectors.toList());
        }
    }

    public static class Week
    {
        private final Day monday;
        private final Day tuesday;
        private final Day wednesday;
        private final Day thursday;
        private final Day friday;
        private final Day saturday;
        private final Day sunday;

        public Week(int skip, LocalDate startDate)
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

        public void setStartDate(LocalDate startDate)
        {
            monday.setStartDate(startDate.plusDays(0));
            tuesday.setStartDate(startDate.plusDays(1));
            wednesday.setStartDate(startDate.plusDays(2));
            thursday.setStartDate(startDate.plusDays(3));
            friday.setStartDate(startDate.plusDays(4));
            saturday.setStartDate(startDate.plusDays(5));
            sunday.setStartDate(startDate.plusDays(6));
        }

        public List<Procedure> getProcedures()
        {
            return Stream.of(monday, tuesday, wednesday, thursday, friday, saturday, sunday)
                .flatMap(d -> d.getProcedures().stream())
                .collect(Collectors.toList());
        }

        public DateTime getNextProcedureTime(DateTime now)
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

        public List<PendingProcedure> getPendingProceduresBetween(DateTime start, DateTime end)
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
}
