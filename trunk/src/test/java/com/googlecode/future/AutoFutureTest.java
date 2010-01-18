package com.googlecode.future;

import org.junit.Test;

import static org.junit.Assert.*;
import static com.googlecode.future.Auto.auto;

public class AutoFutureTest {
    @Test
    public void wrappedAutoFutureIsAlwaysEvaluated() {
        AutoFuture<Boolean> auto = auto(new FutureAction<Boolean>() {
            public void run() {
                set(true);
            }
        });
        assertTrue(auto.isDone());
        assertTrue(auto.get());       
    }
    
    @Test
    public void autoFutureActionIsAlwaysEvaluated() {
        AutoFuture<Boolean> auto = new AutoFutureAction<Boolean>() {
            public void run() {
                set(true);
            }
        };
        assertTrue(auto.isDone());
        assertTrue(auto.get());       
    }
}