package com.googlecode.future;

import org.junit.Test;

import static org.junit.Assert.*;
import static com.googlecode.future.ConstantResult.*;


public class FutureDelegatorTest {
    
    @SuppressWarnings("unchecked")
    @Test
    public void withNoDelegatesReturnsNull() {
        FutureDelegator<Boolean> result = new FutureDelegator<Boolean>();
        assertNull(result.get());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void withSingleDelegateReturnsResult() {
        FutureDelegator<Boolean> result = 
            new FutureDelegator<Boolean>(constant(true));
        assertTrue(result.get());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void withMultipleDelegatesReturnsFirstNonNullResult() {
        FutureDelegator<Integer> result = 
            new FutureDelegator<Integer>(constant((Integer)null), constant(1), constant(2));
        assertEquals(1, (int)result.get());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void withCustomIsFinishedReturnsFirstValidResult() {
        FutureDelegator<Integer> result = 
            new FutureDelegator<Integer>(constant(1), constant(2), constant(3)) {
            @Override
            public boolean isFinished(Integer result) {
                return result > 2; 
            }
        };
        assertEquals(3, (int)result.get());
    }

}
