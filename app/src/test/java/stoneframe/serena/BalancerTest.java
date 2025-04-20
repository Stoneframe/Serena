package stoneframe.serena;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDateTime;
import org.junit.Before;
import org.junit.Test;

import stoneframe.serena.mocks.TestContext;
import stoneframe.serena.model.balancers.Balancer;
import stoneframe.serena.model.balancers.BalancerEditor;
import stoneframe.serena.model.balancers.BalancerManager;

public class BalancerTest
{
    private TestContext context;

    private BalancerManager balancerManager;

    @Before
    public void before()
    {
        context = new TestContext();

        balancerManager = context.getBalancerManager();
    }

    @Test
    public void getAvailable_createNewBalancer_availableIsZero()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");

        // ACT
        int available = balancer.getAvailable(now);

        // ASSERT
        assertEquals(0, available);
    }

    @Test
    public void setChangePerInterval_changeNewBalancerToLimiter_availableIsZero()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);

        // ACT
        balancerEditor.setChangePerInterval(50);

        // ASSERT
        assertEquals(0, balancer.getAvailable(now));
    }

    @Test
    public void setChangePerInterval_changeNewBalancerToEnhancer_availableIsZero()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);

        // ACT
        balancerEditor.setChangePerInterval(-50);

        // ASSERT
        assertEquals(0, balancer.getAvailable(now));
    }

    @Test
    public void setChangePerInterval_changeBalancerFromEnhancerToLimiterOneIntervalLater_availableIsUnchanged()
    {
        // ARRANGE
        LocalDateTime starTime = LocalDateTime.now();

        context.setCurrentTime(starTime);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerInterval(-50);

        context.setCurrentTime(starTime.plusDays(1));

        // ACT
        int oldAvailable = balancer.getAvailable(starTime.plusDays(1));

        balancerEditor.setChangePerInterval(50);

        int newAvailable = balancer.getAvailable(starTime.plusDays(1));

        // ASSERT
        assertEquals(oldAvailable, newAvailable);
    }

    @Test
    public void setChangePerInterval_changeBalancerFromLimiterToEnhancerOneIntervalLater_availableIsUnchanged()
    {
        // ARRANGE
        LocalDateTime starTime = LocalDateTime.now();

        context.setCurrentTime(starTime);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerInterval(50);

        context.setCurrentTime(starTime.plusDays(1));

        // ACT
        int oldAvailable = balancer.getAvailable(starTime.plusDays(1));

        balancerEditor.setChangePerInterval(-50);

        int newAvailable = balancer.getAvailable(starTime.plusDays(1));

        // ASSERT
        assertEquals(oldAvailable, newAvailable);
    }

    @Test
    public void setChangePerInterval_changeBalancerFromLimiterToEnhancerWithTransaction_availableIsUnchanged()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerInterval(50);

        balancerEditor.addTransaction(50);

        // ACT
        int oldAvailable = balancer.getAvailable(now);

        balancerEditor.setChangePerInterval(-50);

        int newAvailable = balancer.getAvailable(now);

        // ASSERT
        assertEquals(oldAvailable, newAvailable);
    }

    @Test
    public void addTransaction_balancerIsCounter_positiveTransactionIncreasesAvailable()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);

        // ACT
        balancerEditor.addTransaction(10);

        // ASSERT
        assertEquals(10, balancer.getAvailable(now));
    }

    @Test
    public void addTransaction_balancerIsLimiter_negativeTransactionReducesAvailable()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerInterval(1);

        // ACT
        balancerEditor.addTransaction(10);

        // ASSERT
        assertEquals(10, balancer.getAvailable(now));
    }

    @Test
    public void addTransaction_balancerIsEnhancer_positiveTransactionIncreaseAvailable()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerInterval(-1);

        // ACT
        balancerEditor.addTransaction(10);

        // ASSERT
        assertEquals(10, balancer.getAvailable(now));
    }

    @Test
    public void addTransaction_balancerIsLimiterAtMaxValue_newValueSubtractsFromMaxValue()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerInterval(1);
        balancerEditor.setMaxValue(10);
        balancerEditor.addTransaction(20);

        // ACT
        balancerEditor.addTransaction(-5);

        // ASSERT
        assertEquals(5, balancer.getAvailable(now));
    }

    @Test
    public void addTransaction_subtractOneFromLimiterAtMaxValue_oneSubtractedFromMaxValue()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setIntervalType(Balancer.MONTHLY);
        balancerEditor.setChangePerInterval(1);
        balancerEditor.setMaxValue(3);
        balancerEditor.addTransaction(200);

        // ACT
        balancerEditor.addTransaction(-1);

        // ASSERT
        assertEquals(2, balancer.getAvailable(now));
    }

    @Test
    public void setIntervalType_intervalIsDaily_changePerIntervalIncrementsDaily()
    {
        // ARRANGE
        LocalDateTime startTime = LocalDateTime.now();

        context.setCurrentTime(startTime);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerInterval(1);
        balancerEditor.setIntervalType(Balancer.DAILY);

        // ACT
        int value = balancer.getAvailable(startTime.plusDays(1));

        // ASSERT
        assertEquals(1, value);
    }

    @Test
    public void setIntervalType_intervalIsWeekly_changePerIntervalIncrementsWeekly()
    {
        // ARRANGE
        LocalDateTime startTime = LocalDateTime.now();

        context.setCurrentTime(startTime);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerInterval(1);
        balancerEditor.setIntervalType(Balancer.WEEKLY);

        // ACT
        int value = balancer.getAvailable(startTime.plusDays(7));

        // ASSERT
        assertEquals(1, value);
    }

    @Test
    public void setIntervalType_intervalIsMonthly_changePerIntervalIncrementsMonthly()
    {
        // ARRANGE
        LocalDateTime startTime = LocalDateTime.now();

        context.setCurrentTime(startTime);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerInterval(1);
        balancerEditor.setIntervalType(Balancer.MONTHLY);

        // ACT
        int value = balancer.getAvailable(startTime.plusDays(30));

        // ASSERT
        assertEquals(1, value);
    }

    @Test
    public void setIntervalType_intervalIsYearly_changePerIntervalIncrementsYearly()
    {
        // ARRANGE
        LocalDateTime startTime = LocalDateTime.now();

        context.setCurrentTime(startTime);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerInterval(1);
        balancerEditor.setIntervalType(Balancer.YEARLY);

        // ACT
        int value = balancer.getAvailable(startTime.plusDays(365));

        // ASSERT
        assertEquals(1, value);
    }

    @Test
    public void getAvailable_decreaseBelowMinValue_balancerTicksUpFromMinValue()
    {
        // ARRANGE
        LocalDateTime startTime = LocalDateTime.now();

        context.setCurrentTime(startTime);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerInterval(5);
        balancerEditor.setIntervalType(Balancer.DAILY);
        balancerEditor.setMinValue(-20);

        // ACT
        balancerEditor.addTransaction(-100);

        int available = balancer.getAvailable(startTime.plusDays(1));

        // ASSERT
        assertEquals(-15, available);
    }
}
