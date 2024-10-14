package stoneframe.chorelist.model.routines;

import org.joda.time.LocalDateTime;

class FortnightRoutineData extends RoutineData
{
    final Week week1;
    final Week week2;

    FortnightRoutineData(
        String name,
        LocalDateTime now,
        Week week1,
        Week week2)
    {
        super(name, now);

        this.week1 = week1;
        this.week2 = week2;
    }
}
