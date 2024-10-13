package stoneframe.chorelist.model.routines;

import org.joda.time.LocalDateTime;

import stoneframe.chorelist.model.util.DeepCopy;

public class WeekRoutine extends Routine<WeekRoutineData>
{
    WeekRoutine(String name, LocalDateTime now)
    {
        super(WEEK_ROUTINE, new WeekRoutineData(name, now));
    }

    public Week getWeek()
    {
        return data().getWeek();
    }
}
