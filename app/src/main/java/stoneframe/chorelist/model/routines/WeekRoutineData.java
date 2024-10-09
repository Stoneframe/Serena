package stoneframe.chorelist.model.routines;

import androidx.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.List;

class WeekRoutineData extends RoutineData
{
    private final Week week;

    public WeekRoutineData(String name, LocalDateTime now)
    {
        super(name, now);

        week = new Week(0, new LocalDate(2024, 1, 1));
    }

    @Override
    public List<Procedure> getAllProcedures()
    {
        return week.getProcedures();
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
