package stoneframe.serena.reminders;

import org.joda.time.LocalDateTime;

import stoneframe.serena.Editor;
import stoneframe.serena.timeservices.TimeService;

public class ReminderEditor extends Editor<ReminderEditor.ReminderEditorListener>
{
    private final ReminderManager reminderManager;
    private final Reminder reminder;

    private final PropertyUtil<LocalDateTime> dateTimeProperty;
    private final PropertyUtil<String> textProperty;
    private final PropertyUtil<Boolean> isDoneProperty;

    public ReminderEditor(
        ReminderManager reminderManager,
        Reminder reminder,
        TimeService timeService)
    {
        super(timeService);

        this.reminderManager = reminderManager;
        this.reminder = reminder;
        this.reminder.edit();

        dateTimeProperty = new PropertyUtil<>(
            reminder::getDateTime,
            reminder::setDateTime,
            v -> notifyListeners(ReminderEditorListener::dateTimeChanged));

        textProperty = new PropertyUtil<>(
            reminder::getText,
            reminder::setText,
            v -> notifyListeners(ReminderEditorListener::textChanged));

        isDoneProperty = new PropertyUtil<>(
            reminder::isDone,
            reminder::setDone,
            v -> notifyListeners(ReminderEditorListener::isDoneChanged));
    }

    public LocalDateTime getDateTime()
    {
        return dateTimeProperty.getValue();
    }

    public void setDateTime(LocalDateTime dateTime)
    {
        dateTimeProperty.setValue(dateTime);
    }

    public String getText()
    {
        return textProperty.getValue();
    }

    public void setText(String text)
    {
        textProperty.setValue(text);
    }

    public boolean isDone()
    {
        return isDoneProperty.getValue();
    }

    public void setDone(boolean isDone)
    {
        isDoneProperty.setValue(isDone);
    }

    public void save()
    {
        reminder.save();

        if (!reminderManager.containsReminder(reminder))
        {
            reminderManager.addReminder(reminder);
        }
    }

    public void revert()
    {
        reminder.revert();
    }

    public void remove()
    {
        if (reminderManager.containsReminder(reminder))
        {
            reminderManager.removeReminder(reminder);
        }
    }

    public interface ReminderEditorListener
    {
        void dateTimeChanged();

        void textChanged();

        void isDoneChanged();
    }
}