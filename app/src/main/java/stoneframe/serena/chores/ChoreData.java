package stoneframe.serena.chores;

import org.joda.time.LocalDate;

class ChoreData
{
    boolean isEnabled;

    LocalDate next;
    LocalDate postpone;

    String description;

    int priority;
    int effort;

    int repetitionType;

    int intervalUnit;
    int intervalLength;

    boolean monday;
    boolean tuesday;
    boolean wednesday;
    boolean thursday;
    boolean friday;
    boolean saturday;
    boolean sunday;

    ChoreData(
        boolean isEnabled,
        LocalDate next,
        LocalDate postpone,
        String description,
        int priority,
        int effort,
        int repetitionType,
        int intervalUnit,
        int intervalLength)
    {
        this.isEnabled = isEnabled;
        this.next = next;
        this.postpone = postpone;
        this.description = description;
        this.priority = priority;
        this.effort = effort;
        this.repetitionType = repetitionType;
        this.intervalUnit = intervalUnit;
        this.intervalLength = intervalLength;
    }
}
