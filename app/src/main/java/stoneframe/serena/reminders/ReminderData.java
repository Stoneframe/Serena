package stoneframe.serena.reminders;

import org.joda.time.LocalDateTime;

class ReminderData
{
    LocalDateTime dateTime;
    String text;
    boolean isDone;

    ReminderData(LocalDateTime dateTime, String text)
    {
        this.dateTime = dateTime;
        this.text = text;
        this.isDone = false;
    }
}
