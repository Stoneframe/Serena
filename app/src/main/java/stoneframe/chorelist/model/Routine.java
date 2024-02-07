package stoneframe.chorelist.model;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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

    public Routine(int routineType, String name)
    {
        this.routineType = routineType;
        this.name = name;
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

    public abstract List<Procedure> getAllProcedures();

    @CheckForNull
    public abstract DateTime getNextProcedureTime(DateTime now);

    public abstract List<Procedure> getPendingProcedures(DateTime now);

    public abstract void procedureDone(Procedure procedure, DateTime now);

    @NonNull
    @Override
    public String toString()
    {
        return name;
    }

    public static class WeekDay
    {
        private final List<Procedure> procedures = new LinkedList<>();

        private final int dayOfWeek;
        private final String name;

        public WeekDay(int dayOfWeek, String name)
        {
            this.dayOfWeek = dayOfWeek;
            this.name = name;
        }

        public String getName()
        {
            return name;
        }

        public int getDayOfWeek()
        {
            return dayOfWeek;
        }

        public List<Procedure> getProcedures()
        {
            return procedures.stream()
                .sorted()
                .collect(Collectors.toList());
        }

        public void addProcedure(Procedure procedure)
        {
            procedures.add(procedure);
        }

        public void removeProcedure(Procedure procedure)
        {
            procedures.remove(procedure);
        }

        public DateTime getNextProcedureTime(DateTime now, int skip)
        {
            if (procedures.isEmpty()) return null;

            int dayDiff = dayOfWeek - now.getDayOfWeek();

            return procedures.stream()
                .map(p ->
                {
                    DateTime dateTime = p.getTime()
                        .toDateTime(now.withTimeAtStartOfDay())
                        .plusDays(dayDiff);

                    if (!dateTime.isAfter(now))
                    {
                        dateTime = dateTime.plusWeeks(1 + skip);
                    }

                    return dateTime;
                })
                .sorted()
                .findFirst()
                .orElse(null);
        }
    }

    public static class Week
    {
        private final WeekDay monday = new WeekDay(DateTimeConstants.MONDAY, "Monday");
        private final WeekDay tuesday = new WeekDay(DateTimeConstants.TUESDAY, "Tuesday");
        private final WeekDay wednesday = new WeekDay(DateTimeConstants.WEDNESDAY, "Wednesday");
        private final WeekDay thursday = new WeekDay(DateTimeConstants.THURSDAY, "Thursday");
        private final WeekDay friday = new WeekDay(DateTimeConstants.FRIDAY, "Friday");
        private final WeekDay saturday = new WeekDay(DateTimeConstants.SATURDAY, "Saturday");
        private final WeekDay sunday = new WeekDay(DateTimeConstants.SUNDAY, "Sunday");

        private final int skip;

        public Week(int skip)
        {
            this.skip = skip;
        }

        public WeekDay getMonday()
        {
            return monday;
        }

        public WeekDay getTuesday()
        {
            return tuesday;
        }

        public WeekDay getWednesday()
        {
            return wednesday;
        }

        public WeekDay getThursday()
        {
            return thursday;
        }

        public WeekDay getFriday()
        {
            return friday;
        }

        public WeekDay getSaturday()
        {
            return saturday;
        }

        public WeekDay getSunday()
        {
            return sunday;
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
                .map(p -> p.getNextProcedureTime(now, skip))
                .filter(Objects::nonNull)
                .sorted()
                .findFirst()
                .get();
        }

        public WeekDay getWeekDay(int dayOfWeek)
        {
            return Stream.of(monday, tuesday, wednesday, thursday, friday, saturday, sunday)
                .filter(d -> d.getDayOfWeek() == dayOfWeek)
                .findFirst()
                .get();
        }

        public int getWeekDay(Procedure procedure)
        {
            Optional<WeekDay> weekDayOptional = getAllDaysStream()
                .filter(day -> day.getProcedures().contains(procedure))
                .findFirst();

            assert weekDayOptional.isPresent();

            return weekDayOptional.get().getDayOfWeek();
        }

        public Map<Procedure, DateTime> getProcedureDateTimesBefore(DateTime now)
        {
            return this
                .concat(
                    getAdjustedProcedureDateTimesOfWeekDay(DateTimeConstants.MONDAY, now),
                    getAdjustedProcedureDateTimesOfWeekDay(DateTimeConstants.TUESDAY, now),
                    getAdjustedProcedureDateTimesOfWeekDay(DateTimeConstants.WEDNESDAY, now),
                    getAdjustedProcedureDateTimesOfWeekDay(DateTimeConstants.THURSDAY, now),
                    getAdjustedProcedureDateTimesOfWeekDay(DateTimeConstants.FRIDAY, now),
                    getAdjustedProcedureDateTimesOfWeekDay(DateTimeConstants.SATURDAY, now),
                    getAdjustedProcedureDateTimesOfWeekDay(DateTimeConstants.SUNDAY, now))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        }

        private Stream<WeekDay> getAllDaysStream()
        {
            return Stream.of(monday, tuesday, wednesday, thursday, friday, saturday, sunday);
        }

        @SafeVarargs
        private final Stream<Map.Entry<Procedure, DateTime>> concat(@NonNull Map<Procedure, DateTime>... procedureMaps)
        {
            Stream<Map.Entry<Procedure, DateTime>> stream = Stream.of();

            for (Map<Procedure, DateTime> procedureMap : procedureMaps)
            {
                stream = Stream.concat(stream, procedureMap.entrySet().stream());
            }

            return stream;
        }

        @NonNull
        private Map<Procedure, DateTime> getAdjustedProcedureDateTimesOfWeekDay(
            int dayOfWeek,
            DateTime now)
        {
            return getWeekDay(dayOfWeek)
                .getProcedures()
                .stream()
                .collect(Collectors.toMap(p -> p, p -> getProcedureDateTime(dayOfWeek, now, p)));
        }

        @NonNull
        private DateTime getProcedureDateTime(int dayOfWeek, DateTime now, Procedure procedure)
        {
            DateTime dateTime = procedure.getTime()
                .toDateTime(now)
                .plusDays(dayOfWeek - now.getDayOfWeek());

            return dateTime.isAfter(now) ? dateTime.minusWeeks(1 + skip) : dateTime;
        }
    }
}
