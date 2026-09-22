package stoneframe.serena.chores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import stoneframe.serena.chores.choreselectors.SimpleChoreSelector;
import stoneframe.serena.chores.efforttrackers.SimpleEffortTracker;
import stoneframe.serena.mocks.MockTimeService;
import stoneframe.serena.mocks.TestContext;


public class ChoreManagerTest
{
    private static final LocalDate TODAY = new LocalDate(2024, 1, 1);

    private TestContext context;

    private ChoreManager choreManager;

    @Before
    public void before()
    {
        context = new TestContext();

        choreManager = context.getChoreManager();
    }

    @Test
    public void getTodaysChores_twoChoresWithNextBeforeToday_listContainsBothChores()
    {
        // ARRANGE
        Chore chore1 = createChore("Chore1", 5, 2, TODAY.minusDays(2), true);
        Chore chore2 = createChore("Chore2", 3, 5, TODAY.minusDays(1), true);

        // ACT
        context.setCurrentTime(TODAY);

        List<Chore> todaysChores = choreManager.getTodaysChores();

        // ASSERT
        assertEquals(Arrays.asList(chore2, chore1), todaysChores);
    }

    @Test
    public void skip_afterAnotherChoreCompletes_targetsOriginalChore()
    {
        // ARRANGE
        Chore completedChore = createChore("Completed", 1, 1, TODAY, true);
        Chore selectedChore = createChore("Selected", 2, 1, TODAY, true);

        context.setCurrentTime(TODAY);

        choreManager.complete(completedChore);

        // ACT
        boolean wasSkipped = choreManager.skip(selectedChore);

        // ASSERT
        assertTrue(wasSkipped);
        assertEquals(TODAY.plusDays(3), selectedChore.getNext());
        assertTrue(choreManager.getTodaysChores().isEmpty());
    }

    @Test
    public void skip_dueChore_doesNotSpendEffort()
    {
        // ARRANGE
        SimpleEffortTracker effortTracker = new SimpleEffortTracker(10);
        context.setEffortTracker(effortTracker);

        Chore chore = createChore("Skipped", 1, 3, TODAY, true);

        context.setCurrentTime(TODAY);

        // ACT
        boolean wasSkipped = choreManager.skip(chore);

        // ASSERT
        assertTrue(wasSkipped);
        assertEquals(10, choreManager.getRemainingEffort());
    }

    @Test
    public void postpone_dueChore_doesNotSpendEffortAndHidesChore()
    {
        // ARRANGE
        SimpleEffortTracker effortTracker = new SimpleEffortTracker(10);
        context.setEffortTracker(effortTracker);

        Chore chore = createChore("Postponed", 1, 3, TODAY, true);

        context.setCurrentTime(TODAY);

        // ACT
        boolean wasPostponed = choreManager.postpone(chore);

        // ASSERT
        assertTrue(wasPostponed);
        assertEquals(10, choreManager.getRemainingEffort());
        assertTrue(choreManager.getTodaysChores().isEmpty());
    }

    @Test
    public void skip_afterTargetCompletes_rejectsWithoutMutation()
    {
        // ARRANGE
        Chore chore = createChore("Completed target", 1, 1, TODAY, true);

        context.setCurrentTime(TODAY);

        choreManager.complete(chore);
        LocalDate nextAfterCompletion = chore.getNext();

        // ACT
        boolean wasSkipped = choreManager.skip(chore);

        // ASSERT
        assertFalse(wasSkipped);
        assertEquals(nextAfterCompletion, chore.getNext());
    }

    @Test
    public void postpone_afterListOrderChanges_targetsOriginalChore()
    {
        // ARRANGE
        Chore firstChore = createChore("First", 1, 1, TODAY, true);
        Chore selectedChore = createChore("Selected", 2, 1, TODAY, true);

        context.setCurrentTime(TODAY);

        choreManager.getChoreEditor(firstChore).setPriority(3);

        // ACT
        boolean wasPostponed = choreManager.postpone(selectedChore);

        // ASSERT
        assertTrue(wasPostponed);
        assertEquals(Arrays.asList(firstChore), choreManager.getTodaysChores());
    }

    @Test
    public void skip_disabledChore_rejectsWithoutMutation()
    {
        // ARRANGE
        Chore chore = createChore("Disabled", 1, 1, TODAY, false);

        context.setCurrentTime(TODAY);

        LocalDate nextBeforeAction = chore.getNext();

        // ACT
        boolean wasSkipped = choreManager.skip(chore);

        // ASSERT
        assertFalse(wasSkipped);
        assertEquals(nextBeforeAction, chore.getNext());
        assertTrue(choreManager.getTodaysChores().isEmpty());
    }

    @Test
    public void postpone_deletedChore_rejectsWithoutMutation()
    {
        // ARRANGE
        Chore chore = createChore("Deleted", 1, 1, TODAY, true);

        context.setCurrentTime(TODAY);

        LocalDate nextBeforeAction = chore.getNext();

        choreManager.getChoreEditor(chore).remove();

        // ACT
        boolean wasPostponed = choreManager.postpone(chore);

        // ASSERT
        assertFalse(wasPostponed);
        assertEquals(nextBeforeAction, chore.getNext());
        assertTrue(choreManager.getTodaysChores().isEmpty());
    }

    @Test
    public void skip_afterTargetIsPostponed_rejectsWithoutMutation()
    {
        // ARRANGE
        Chore chore = createChore("Postponed", 1, 1, TODAY, true);

        context.setCurrentTime(TODAY);

        choreManager.postpone(chore);

        LocalDate nextBeforeAction = chore.getNext();

        // ACT
        boolean wasSkipped = choreManager.skip(chore);

        // ASSERT
        assertFalse(wasSkipped);
        assertEquals(nextBeforeAction, chore.getNext());
        assertTrue(choreManager.getTodaysChores().isEmpty());
    }

    @Test
    public void skip_afterDescriptionChanges_targetsSameChore()
    {
        // ARRANGE
        Chore chore = createChore("Original", 1, 1, TODAY, true);

        context.setCurrentTime(TODAY);

        choreManager.getChoreEditor(chore).setDescription("Edited");

        // ACT
        boolean wasSkipped = choreManager.skip(chore);

        // ASSERT
        assertTrue(wasSkipped);
        assertEquals(TODAY.plusDays(3), chore.getNext());
        assertTrue(choreManager.getTodaysChores().isEmpty());
    }

    @Test
    public void skip_afterContainerReplacement_rejectsOldEqualDescriptionChore()
    {
        // ARRANGE
        MockTimeService timeService = new MockTimeService(
            TODAY.toLocalDateTime(LocalTime.MIDNIGHT));
        AtomicReference<ChoreContainer> container = new AtomicReference<>(createContainer());
        ChoreManager manager = new ChoreManager(container::get, timeService);

        Chore oldChore = createChore(manager, "Same description", TODAY);
        LocalDate oldNext = oldChore.getNext();

        container.set(createContainer());
        Chore replacement = createChore(manager, "Same description", TODAY);
        LocalDate replacementNext = replacement.getNext();

        // ACT
        boolean wasSkipped = manager.skip(oldChore);

        // ASSERT
        assertFalse(wasSkipped);
        assertEquals(oldNext, oldChore.getNext());
        assertEquals(replacementNext, replacement.getNext());
    }

    private ChoreContainer createContainer()
    {
        return new ChoreContainer(new SimpleEffortTracker(10), new SimpleChoreSelector());
    }

    private Chore createChore(ChoreManager manager, String description, LocalDate next)
    {
        Chore chore = manager.createChore();
        ChoreEditor editor = manager.getChoreEditor(chore);

        editor.setDescription(description);

        IntervalRepetition repetition = (IntervalRepetition)editor.getRepetition();
        repetition.setNext(next);
        repetition.setIntervalLength(3);
        repetition.setIntervalUnit(IntervalRepetition.DAYS);

        editor.save();

        return chore;
    }

    private Chore createChore(String description, int priority, int effort, LocalDate next, boolean isEnabled)
    {
        Chore chore = choreManager.createChore();

        ChoreEditor choreEditor = choreManager.getChoreEditor(chore);

        choreEditor.setDescription(description);
        choreEditor.setPriority(priority);
        choreEditor.setEffort(effort);
        choreEditor.setEnabled(isEnabled);

        IntervalRepetition repetition = (IntervalRepetition)choreEditor.getRepetition();

        repetition.setNext(next);
        repetition.setIntervalLength(3);
        repetition.setIntervalUnit(IntervalRepetition.DAYS);

        choreEditor.save();

        return chore;
    }
}
