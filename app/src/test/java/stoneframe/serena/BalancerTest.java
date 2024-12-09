package stoneframe.serena;

import static org.junit.Assert.assertEquals;

import org.joda.time.LocalDate;
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
}
