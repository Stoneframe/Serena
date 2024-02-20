package stoneframe.chorelist.model;

import androidx.annotation.Nullable;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WeekRoutine extends Routine
{
    private final Week week;

    private DateTime lastCompleted;

    public WeekRoutine(String name, DateTime now)
    {
        super(WEEK_ROUTINE, name);

        lastCompleted = now;

        week = new Week(0, new LocalDate(2024, 1, 1));
    }

    @Override
    public List<Procedure> getAllProcedures()
    {
        return week.getProcedures();
    }

    public Day getWeekDay(int dayOfWeek)
    {
        return week.getWeekDay(dayOfWeek);
    }

    @Nullable
    @Override
    public DateTime getNextProcedureTime(DateTime now)
    {
        return week.getNextProcedureTime(now);
    }

    @Override
    public List<Procedure> getPendingProcedures(DateTime now)
    {
        return week.getProcedureDateTimesBetween(lastCompleted, now)
            .entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @Override
    public void procedureDone(Procedure procedure, DateTime now)
    {
        lastCompleted = week.getNextTimeOfProcedureAfter(procedure, lastCompleted);
    }

    public Week getWeek()
    {
        return week;
    }
}
