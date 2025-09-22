package stoneframe.serena.reminders;

import org.joda.time.LocalDateTime;

import stoneframe.serena.util.Revertible;

public class Reminder extends Revertible<ReminderData>
{
    Reminder(LocalDateTime dateTime, String text)
    {
        super(new ReminderData(dateTime, text));
    }

    public LocalDateTime getDateTime()
    {
        return data().dateTime;
    }

    void setDateTime(LocalDateTime dateTime)
    {
        data().dateTime = dateTime;
    }

    public String getText()
    {
        return data().text;
    }

    void setText(String text)
    {
        data().text = text;
    }

    boolean isDone()
    {
        return data().isDone;
    }

    void setDone(boolean isDone)
    {
        data().isDone = isDone;
    }

    public void snooze()
    {
        data().dateTime = data().dateTime.plusMinutes(10);
    }
}
