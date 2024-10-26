package stoneframe.chorelist.model.chores;


import org.joda.time.LocalDate;

public abstract class Repetition
{
    public static final int Interval = 0;
    public static final int DaysInWeek = 1;

    private final int repetitionType;

    final ChoreData data;

    Repetition(int repetitionType, ChoreData data)
    {
        this.repetitionType = repetitionType;
        this.data = data;
    }

    public int getRepetitionType()
    {
        return repetitionType;
    }

    public abstract LocalDate getNext();

    public abstract double getEffortPerWeek();

    abstract void updateNext(LocalDate today);

    abstract void reschedule(LocalDate today);

    abstract double getFrequency();
}
