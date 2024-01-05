package stoneframe.chorelist;

import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import stoneframe.chorelist.model.Schedule;
import stoneframe.chorelist.model.SimpleEffortTracker;
import stoneframe.chorelist.model.SimpleChoreSelector;
import stoneframe.chorelist.model.Chore;

import static org.junit.Assert.*;

public class ScheduleTest
{
    @Test
    public void two_chores_scheduled_for_before_now()
    {
        Chore t1 = new Chore("Chore1", 5, 5, TestUtils.MOCK_NOW.minusDays(2), Chore.DAILY, 1);
        Chore t2 = new Chore("Chore2", 3, 8, TestUtils.MOCK_NOW.minusDays(1), Chore.DAILY, 1);

        Schedule schedule = new Schedule(new SimpleEffortTracker(15), new SimpleChoreSelector());

        schedule.addChore(t1);
        schedule.addChore(t2);

        List<Chore> expected = new LinkedList<>();
        expected.add(t2);
        expected.add(t1);

        List<Chore> actual = schedule.getChores(TestUtils.MOCK_NOW);

        assertEquals(expected, actual);
    }
}
