package com.googlecode.future;

import org.junit.Test;

import static org.junit.Assert.*;
import static com.googlecode.future.ConstantResult.*;


public class FutureDelegationChainTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void withNoDelegatesReturnsNull() {
        FutureDelegationChain<Boolean> result = new FutureDelegationChain<Boolean>();
        assertNull(result.get());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void withSingleDelegateReturnsResult() {
        FutureDelegationChain<Boolean> result = 
            new FutureDelegationChain<Boolean>(constant(true));
        assertTrue(result.get());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void withMultipleDelegatesReturnsFirstNonNullResult() {
        FutureDelegationChain<Integer> result = 
            new FutureDelegationChain<Integer>(constant((Integer)null), constant(1), constant(2));
        assertEquals(1, (int)result.get());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void withCustomIsFinishedReturnsFirstValidResult() {
        FutureDelegationChain<Integer> result = 
            new FutureDelegationChain<Integer>(constant(1), constant(2), constant(3)) {
            @Override
            public boolean isFinished(Integer result) {
                return result > 2; 
            }
        };
        assertEquals(3, (int)result.get());
    }

}
