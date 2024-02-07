package stoneframe.chorelist.model;

import androidx.annotation.Nullable;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WeekRoutine extends Routine
{
    private final Week week = new Week(0);

    private DateTime lastCompleted;

    public WeekRoutine(String name, DateTime now)
    {
        super(WEEK_ROUTINE, name);

        lastCompleted = now;
    }

    @Override
    public List<Procedure> getAllProcedures()
    {
        return week.getProcedures();
    }

    public WeekDay getWeekDay(int dayOfWeek)
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
        return week.getProcedureDateTimesBefore(now)
            .entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .filter(pd -> isPending(now, pd.getValue()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }

    @Override
    public void procedureDone(Procedure procedure, DateTime now)
    {
        int procedureWeekDay = week.getWeekDay(procedure);

        lastCompleted = procedure.getTime()
            .toDateTime(now)
            .plusDays(procedureWeekDay - now.getDayOfWeek());
    }

    private boolean isPending(DateTime now, DateTime procedureDateTime)
    {
        return procedureDateTime.isAfter(lastCompleted)
            && (procedureDateTime.isBefore(now) || procedureDateTime.isEqual(now));
    }
}
