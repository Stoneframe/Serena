package stoneframe.chorelist.model.routines;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import stoneframe.chorelist.model.util.DeepCopy;

public class FortnightRoutine extends Routine<FortnightRoutineData>
{
    FortnightRoutine(String name, LocalDate startDate, LocalDateTime now)
    {
        super(FORTNIGHT_ROUTINE, new FortnightRoutineData(name, startDate, now));
    }

    public LocalDate getStartDate()
    {
        return data().getStartDate();
    }

    public Week getWeek1()
    {
        return data().getWeek1();
    }

    public Week getWeek2()
    {
        return data().getWeek2();
    }

    public Week getWeek(int nbr)
    {
        return data().getWeek(nbr);
    }

    void setStartDate(LocalDate startDate)
    {
        data().setStartDate(startDate);
    }
}
