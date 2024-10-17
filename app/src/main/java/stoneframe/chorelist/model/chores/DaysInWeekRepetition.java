package stoneframe.chorelist.model.chores;

import org.joda.time.LocalDate;

public class DaysInWeekRepetition extends Repetition
{
    DaysInWeekRepetition(ChoreData data) {super(Repetition.DaysInWeek, data);}

    @Override
    public LocalDate getNext()
    {
        return null;
    }

    @Override
    public double getEffortPerWeek()
    {
        return 0;
    }

    @Override
    void reschedule(LocalDate today)
    {

    }
}
