package stoneframe.serena.routines;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

class WeekRoutineData extends RoutineData
{
    final Week week;

    WeekRoutineData(String name, LocalDateTime now)
    {
        super(name, now);

        week = new Week(0, new LocalDate(2024, 1, 1));
    }
}
