package stoneframe.chorelist.model.routines;

import org.joda.time.LocalDateTime;

import stoneframe.chorelist.model.util.DeepCopy;

public class WeekRoutine extends Routine
{
    private WeekRoutineData data;

    private transient WeekRoutineData checkpoint;

    public WeekRoutine(String name, LocalDateTime now)
    {
        super(WEEK_ROUTINE);

        data = new WeekRoutineData(name, now);
    }

    public Week getWeek()
    {
        return data.getWeek();
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
    RoutineData data()
    {
        return data;
    }
}
