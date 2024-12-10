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
    public void setChangePerDay_changeNewBalancerToLimiter_availableIsZero()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);

        // ACT
        balancerEditor.setChangePerDay(50);

        // ASSERT
        assertEquals(0, balancer.getAvailable(now));
    }

    @Test
    public void setChangePerDay_changeNewBalancerToEnhancer_availableIsZero()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);

        // ACT
        balancerEditor.setChangePerDay(-50);

        // ASSERT
        assertEquals(0, balancer.getAvailable(now));
    }

    @Test
    public void setChangePerDay_changeBalancerFromEnhancerToLimiterOneDayLater_availableIsUnchanged()
    {
        // ARRANGE
        LocalDateTime starTime = LocalDateTime.now();

        context.setCurrentTime(starTime);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerDay(-50);

        context.setCurrentTime(starTime.plusDays(1));

        // ACT
        int oldAvailable = balancer.getAvailable(starTime.plusDays(1));

        balancerEditor.setChangePerDay(50);

        int newAvailable = balancer.getAvailable(starTime.plusDays(1));

        // ASSERT
        assertEquals(oldAvailable, newAvailable);
    }

    @Test
    public void setChangePerDay_changeBalancerFromLimiterToEnhancerOneDayLater_availableIsUnchanged()
    {
        // ARRANGE
        LocalDateTime starTime = LocalDateTime.now();

        context.setCurrentTime(starTime);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerDay(50);

        context.setCurrentTime(starTime.plusDays(1));

        // ACT
        int oldAvailable = balancer.getAvailable(starTime.plusDays(1));

        balancerEditor.setChangePerDay(-50);

        int newAvailable = balancer.getAvailable(starTime.plusDays(1));

        // ASSERT
        assertEquals(oldAvailable, newAvailable);
    }

    @Test
    public void setChangePerDay_changeBalancerFromLimiterToEnhancerWithTransaction_availableIsUnchanged()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerDay(50);

        balancerEditor.addTransaction("Test", 50);

        // ACT
        int oldAvailable = balancer.getAvailable(now);

        balancerEditor.setChangePerDay(-50);

        int newAvailable = balancer.getAvailable(now);

        // ASSERT
        assertEquals(oldAvailable, newAvailable);
    }

    @Test
    public void addTransaction_balancerIsCounter_positiveTransactionReducesAvailable()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);

        // ACT
        balancerEditor.addTransaction("Test", 10);

        // ASSERT
        assertEquals(-10, balancer.getAvailable(now));
    }

    @Test
    public void addTransaction_balancerIsLimiter_positiveTransactionReducesAvailable()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerDay(1);

        // ACT
        balancerEditor.addTransaction("Test", 10);

        // ASSERT
        assertEquals(-10, balancer.getAvailable(now));
    }

    @Test
    public void addTransaction_balancerIsEnhancer_positiveTransactionIncreaseAvailable()
    {
        // ARRANGE
        LocalDateTime now = LocalDateTime.now();

        context.setCurrentTime(now);

        Balancer balancer = balancerManager.createBalancer("Test");
        BalancerEditor balancerEditor = balancerManager.getBalancerEditor(balancer);
        balancerEditor.setChangePerDay(-1);

        // ACT
        balancerEditor.addTransaction("Test", 10);

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
        balancerEditor.setChangePerDay(1);
        balancerEditor.setMaxValue(10);
        balancerEditor.addTransaction("Initial", -20);

        // ACT
        balancerEditor.addTransaction("Test", 5);

        // ASSERT
        assertEquals(5, balancer.getAvailable(now));
    }
}
