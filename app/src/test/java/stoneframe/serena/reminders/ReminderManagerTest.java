package stoneframe.serena.reminders;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import stoneframe.serena.mocks.MockTimeService;

public class ReminderManagerTest
{
    private LocalDateTime now;
    private ReminderManager reminderManager;

    @Before
    public void setUp()
    {
        now = new LocalDateTime(2026, 9, 4, 12, 0);

        ReminderContainer container = new ReminderContainer();
        reminderManager = new ReminderManager(
            () -> container,
            new MockTimeService(now));
    }

    @Test
    public void snooze_pendingReminder_setsTimeToNineMinutesFromNow()
    {
        Reminder reminder = new Reminder(now.minusMinutes(1), "Test reminder");
        reminderManager.addReminder(reminder);

        reminderManager.snooze(reminder);

        assertEquals(now.plusMinutes(9), reminder.getDateTime());
    }

    @Test
    public void complete_pendingReminder_removesItFromPendingReminders()
    {
        Reminder reminder = new Reminder(now.minusMinutes(1), "Test reminder");
        reminderManager.addReminder(reminder);

        reminderManager.complete(reminder);

        assertFalse(reminderManager.getPendingReminders().contains(reminder));
        assertFalse(reminderManager.containsReminder(reminder));
    }
}
