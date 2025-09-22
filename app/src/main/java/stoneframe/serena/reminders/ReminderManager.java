package stoneframe.serena.reminders;

import org.joda.time.LocalDateTime;

import java.util.Comparator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import stoneframe.serena.timeservices.TimeService;

public class ReminderManager
{
    private final Supplier<ReminderContainer> container;

    private final TimeService timeService;

    public ReminderManager(Supplier<ReminderContainer> container, TimeService timeService)
    {
        this.container = container;
        this.timeService = timeService;
    }

    public List<Reminder> getAllReminders()
    {
        removeDoneReminders();

        return container.get().reminders.stream()
            .sorted(Comparator.comparing(Reminder::getDateTime))
            .collect(Collectors.toList());
    }

    public ReminderEditor getEditor(Reminder reminder)
    {
        return new ReminderEditor(this, reminder, timeService);
    }

    public Reminder createReminder()
    {
        return new Reminder(LocalDateTime.now(), "");
    }

    public void complete(Reminder reminder)
    {
        reminder.setDone(true);
    }

    public void snooze(Reminder reminder)
    {
        reminder.snooze();
    }

    public boolean containsReminder(Reminder reminder)
    {
        return container.get().reminders.contains(reminder);
    }

    public void addReminder(Reminder reminder)
    {
        container.get().reminders.add(reminder);
    }

    public void removeReminder(Reminder reminder)
    {
        container.get().reminders.remove(reminder);
    }

    private void removeDoneReminders()
    {
        container.get().reminders.removeIf(Reminder::isDone);
    }
}
