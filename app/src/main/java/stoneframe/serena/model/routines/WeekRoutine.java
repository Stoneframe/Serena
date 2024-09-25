package stoneframe.serena.model.routines;

import androidx.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;

public class WeekRoutine extends Routine
{
    private final Week week;

    public WeekRoutine(String name, LocalDateTime now)
    {
        super(WEEK_ROUTINE, name, now);

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
    public LocalDateTime getNextProcedureTime(LocalDateTime now)
    {
        return week.getNextProcedureTime(now);
    }

    @Override
    public List<PendingProcedure> getPendingProcedures(LocalDateTime now)
    {
        return week.getPendingProceduresBetween(lastCompleted, now);
    }

    @Override
    public void procedureDone(PendingProcedure procedure)
    {
        lastCompleted = procedure.getDateTime();
    }

    public Week getWeek()
    {
        return week;
    }
}
