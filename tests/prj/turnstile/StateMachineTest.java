package prj.turnstile;


import org.junit.Assert;
import org.junit.Test;

public class StateMachineTest
{
    enum TestState
    {
        ONE, TWO, THREE, ERROR;
    }

    enum TestEvent
    {
        A, B;
    }

    @Test
    public void checkNoStateProcessing()
    {
        StateMachine<TestState, TestEvent> sm = new StateMachine<TestState, TestEvent>(TestState.ERROR);
        sm.start(TestState.ONE);
        try
        {
            sm.process(TestEvent.B);
            Assert.fail();
        }
        catch (InitializationException e)
        {
            Assert.assertTrue(true);
        }
        catch (Exception e)
        {
            Assert.fail();
        }
    }

    @Test
    public void checkIsAllowedForOneState()
    {
        StateMachine<TestState, TestEvent> sm = new StateMachine<TestState, TestEvent>(TestState.ERROR);
        sm.addTransition(TestState.ONE, TestEvent.B, TestState.TWO);
        if (!sm.isAllowed(TestState.ONE, TestEvent.B))
        {
            Assert.fail();
        }

        sm.start(TestState.ONE);

        try
        {
            sm.process(TestEvent.B);
        }
        catch (InitializationException e)
        {
            Assert.fail();
        }
    }
    @Test
    public void checkIsAllowedCircularTransition()
    {
        StateMachine<TestState, TestEvent> sm = new StateMachine<TestState, TestEvent>(TestState.ERROR);
        sm.addCircularTransition(TestState.ONE, TestEvent.B);
        if (!sm.isAllowed(TestState.ONE, TestEvent.B))
        {
            Assert.fail();
        }

        sm.start(TestState.ONE);

        try
        {
            sm.process(TestEvent.B);
        }
        catch (InitializationException e)
        {
            Assert.fail();
        }
    }

    @Test
    public void checkIsAllowedForDuplicateState()
    {
        StateMachine<TestState, TestEvent> sm = new StateMachine<TestState, TestEvent>(TestState.ERROR);
        sm.addTransition(TestState.ONE, TestEvent.B, TestState.TWO);
        sm.addTransition(TestState.ONE, TestEvent.B, TestState.TWO);
        if (!sm.isAllowed(TestState.ONE, TestEvent.B))
        {
            Assert.fail();
        }

        sm.start(TestState.ONE);

        try
        {
            sm.process(TestEvent.B);
        }
        catch (InitializationException e)
        {
            Assert.fail();
        }

    }

    @Test
    public void checkIsAllowedMultipleEventsOnOneState() throws Exception
    {
        StateMachine<TestState, TestEvent> sm = new StateMachine<TestState, TestEvent>(TestState.ERROR);
        sm.addTransition(TestState.ONE, TestEvent.B, TestState.TWO);
        sm.addTransition(TestState.ONE, TestEvent.A, TestState.TWO);
        if (!sm.isAllowed(TestState.ONE, TestEvent.A))
        {
            Assert.fail("Old state not overwritten");
        }

        if (!sm.isAllowed(TestState.ONE, TestEvent.B))
        {
            Assert.fail("Old state not overwritten");
        }
    }

    @Test
    public void checkIsAllowedForOverwrittenState()
    {
        StateMachine<TestState, TestEvent> sm = new StateMachine<TestState, TestEvent>(TestState.ERROR);
        sm.addTransition(TestState.ONE, TestEvent.B, TestState.TWO);
        sm.addTransition(TestState.ONE, TestEvent.B, TestState.THREE);
        sm.start(TestState.ONE);

        try
        {
            sm.process(TestEvent.B);
            Assert.assertTrue(sm.getCurrentState().equals(TestState.THREE));
        }
        catch (InitializationException e)
        {
            Assert.fail();
        }
    }

    @Test
    public void checkIsAllowedForMultipleStates()
    {
        StateMachine<TestState, TestEvent> sm = new StateMachine<TestState, TestEvent>(TestState.ERROR);
        sm.addTransition(TestState.ONE, TestEvent.B, TestState.TWO);
        sm.addTransition(TestState.TWO, TestEvent.A, TestState.ONE);
        if (!sm.isAllowed(TestState.ONE, TestEvent.B))
        {
            Assert.fail();
        }
        if (!sm.isAllowed(TestState.TWO, TestEvent.A))
        {
            Assert.fail();
        }

        sm.start(TestState.ONE);

        try
        {
            sm.process(TestEvent.B);
            sm.process(TestEvent.A);
            Assert.assertTrue(sm.getCurrentState().equals(TestState.ONE));
        }
        catch (InitializationException e)
        {
            Assert.fail();
        }

    }

    @Test
    public void noStartCalled()
    {
        StateMachine<TestState, TestEvent> sm = new StateMachine<TestState, TestEvent>(TestState.ERROR);
        sm.addTransition(TestState.ONE, TestEvent.A, TestState.TWO);
        try
        {
            sm.process(TestEvent.A);
            Assert.fail("Expecting initialization exception");
        }
        catch (InitializationException e)
        {
            Assert.assertTrue(true);
        }
        catch (Exception e)
        {
            Assert.fail();
        }
    }

    @Test
    public void startAndEndStatsShouldBeDifferent()
    {
        StateMachine<TestState, TestEvent> sm = new StateMachine<TestState, TestEvent>(TestState.ERROR);
        try
        {
            sm.addTransition(TestState.ONE, TestEvent.A, TestState.ONE);
            Assert.fail();
        }
        catch (IllegalArgumentException e)
        {
            Assert.assertTrue(true);
        }
    }
}
