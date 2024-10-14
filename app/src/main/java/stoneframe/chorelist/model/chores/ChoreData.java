package stoneframe.chorelist.model.chores;

import org.joda.time.LocalDate;

public class ChoreData
{
    boolean isEnabled;

    LocalDate next;
    LocalDate postpone;

    String description;

    int priority;
    int effort;

    int intervalUnit;
    int intervalLength;

    public ChoreData(
        boolean isEnabled,
        LocalDate next,
        LocalDate postpone,
        String description,
        int priority,
        int effort,
        int intervalUnit,
        int intervalLength)
    {
        this.isEnabled = isEnabled;
        this.next = next;
        this.postpone = postpone;
        this.description = description;
        this.priority = priority;
        this.effort = effort;
        this.intervalUnit = intervalUnit;
        this.intervalLength = intervalLength;
    }
}
