package stoneframe.serena.routines;

import androidx.annotation.Nullable;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import java.util.Collections;
import java.util.List;

public class WeekRoutine extends Routine<WeekRoutineData>
{
    WeekRoutine(String name, LocalDateTime now)
    {
        super(WEEK_ROUTINE, new WeekRoutineData(name, now));
    }

    public Week getWeek()
    {
        return data().week;
    }

    @Override
    public List<Procedure> getAllProcedures()
    {
        return data().week.getProcedures();
    }

    @Override
    List<PendingProcedure> getPendingProcedures(LocalDateTime now)
    {
        return data().week.getPendingProceduresBetween(data().lastCompleted, now);
    }

    @Nullable
    @Override
    LocalDateTime getNextProcedureTime(LocalDateTime now)
    {
        return data().week.getNextProcedureTime(now);
    }

    @Override
    List<Procedure> getProceduresForDate(LocalDate date)
    {
        return Collections.unmodifiableList(
            data().week.getWeekDay(date.getDayOfWeek()).getProcedures());
    }

    @Override
    void procedureDone(PendingProcedure procedure)
    {
        data().lastCompleted = procedure.getDateTime();
    }
}
