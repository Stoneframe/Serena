package stoneframe.chorelist.model.routines;

import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;

import stoneframe.chorelist.model.util.DeepCopy;

public class FortnightRoutine extends Routine
{
    private FortnightRoutineData data;

    private transient FortnightRoutineData checkpoint;

    public FortnightRoutine(String name, LocalDate startDate, LocalDateTime now)
    {
        super(FORTNIGHT_ROUTINE);

        data = new FortnightRoutineData(name, startDate, now);
    }

    public LocalDate getStartDate()
    {
        return data.getStartDate();
    }

    public void setStartDate(LocalDate startDate)
    {
        data.setStartDate(startDate);
    }

    public Week getWeek1()
    {
        return data.getWeek1();
    }

    public Week getWeek2()
    {
        return data.getWeek2();
    }

    public Week getWeek(int nbr)
    {
        return data.getWeek(nbr);
    }

    @Override
    public void save()
    {
        checkpoint = DeepCopy.copy(data);
    }

    @Override
    public void revert()
    {
        data = checkpoint;
    }

    @Override
    protected RoutineData data()
    {
        return data;
    }
}
