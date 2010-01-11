package com.googlecode.future;

import org.junit.Test;

import static org.junit.Assert.*;


/**
 * Test of FutureTask
 * 
 * @author Dean Povey
 *
 */
public class FutureTest {
    
    @Test
    public void canEvaluateSimpleFutureTask() {
        FutureAction<Boolean> simple = new FutureAction<Boolean>() {
            public void run() {
               set(true);             
            }
        };
        assertTrue(simple.get());        
        assertTrue(simple.isDone());

    }
    
    @Test(expected=AssertionError.class)
    public void canEvaluateSimpleFailureForUncheckedException() throws Exception {
        try {
            FutureAction<Boolean> simple = new FutureAction<Boolean>() {
                public void run() {
                    setException(new AssertionError());            
                }
            };
            assertTrue(simple.get());
        } catch(ExecutionException e) {
            e.rethrowUncheckedCause();
        }
    }
    
    @SuppressWarnings("serial")
    private static class CheckedException extends Exception {}
    
    @SuppressWarnings("unchecked")
    @Test(expected=CheckedException.class)
    public void canEvaluateSimpleFailureForCheckedException() throws Exception {
        try {
            FutureAction<Boolean> simple = new FutureAction<Boolean>() {
                public void run() {
                    setException(new CheckedException());            
                }
            };
            assertTrue(simple.get());
        } catch(ExecutionException e) {
            throw e.getCheckedCauseOrRethrow(CheckedException.class);
        }
    }
    
    @Test
    public void whenFutureHasDependencyThatIsRunFirst() {
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
                set(true);
            }
        };
        
        FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                set(first.get());
            }
        };
       
        assertTrue(second.get());
    }
    
    @Test
    public void whenResultsAreDelayedCanEvaluateSimpleFutureTask() {        
        final RunLoopSimulator runloop = new RunLoopSimulator();
        FutureAction<Boolean> simple = new FutureAction<Boolean>() {
            public void run() {
               runloop.setValueLater(true, this); 
            }
        };
        FutureResult<Boolean> result = new FutureResult<Boolean>();
        simple.getAsync(result);
        assertFalse(simple.isDone());                
        runloop.run();
        assertTrue(result.get());
    }    
    
    @Test
    public void whenResultsAreDelayedcanEvaluateSimpleFutureTaskWithDependency() {        
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
               runloop.setValueLater(true, this); 
            }
        };
                
        FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                set(first.get());
            }
        };
       
        FutureResult<Boolean> result = new FutureResult<Boolean>();
        second.getAsync(result);
        assertFalse(first.isDone());
        assertFalse(second.isDone());
        runloop.run();
        assertTrue(result.get());
    }
    
    @Test
    public void whenResultsAreDelayedcanEvaluateFutureTaskWithMultipleDependenciesOnSameValue() {        
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
               runloop.setValueLater(true, this); 
            }
        };
                
        final FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                runloop.setValueLater(first.get(), this);
            }
        };
        
        final FutureAction<Boolean> third = new FutureAction<Boolean>() {
            public void run() {
                set(second.get() && first.get());
            }
        };        
       
        FutureResult<Boolean> result = new FutureResult<Boolean>();
        third.getAsync(result);
        assertFalse(first.isDone());
        assertFalse(second.isDone());
        assertFalse(third.isDone());
        runloop.run();
        assertTrue(result.get());
    }
    
    @Test
    public void whenResultsAreDelayedcanEvaluateFutureTaskWithMultipleDependenciesOnMultipleValues() {        
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
               runloop.setValueLater(true, this); 
            }
        };
                
        final FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                runloop.setValueLater(first.get(), this);
            }
        };
        
        final FutureAction<Boolean> third = new FutureAction<Boolean>() {
            public void run() {
                set(first.get());
            }
        };        
        
        final FutureAction<Boolean> fourth = new FutureAction<Boolean>() {
            public void run() {
                set(second.get());
            }
        };
       
        FutureResult<Boolean> result1 = new FutureResult<Boolean>();
        third.getAsync(result1);
        FutureResult<Boolean> result2 = new FutureResult<Boolean>();
        fourth.getAsync(result2);
        assertFalse(first.isDone());
        assertFalse(second.isDone());
        assertFalse(third.isDone());
        runloop.run();
        assertTrue(result1.get());
        assertTrue(result2.get());
    }
    
    @Test
    public void whenResultsAreDelayedLoopsInUnresolvedDependenciesAreDetected() {        
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
               runloop.setValueLater(true, this); 
            }
        };
                
        final FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                runloop.setValueLater(first.get(), this);
            }
        };
        
        final FutureAction<Boolean> third = new FutureAction<Boolean>() {
            public void run() {
                set(first.get());
            }
        };        
        
        final FutureAction<Boolean> fourth = new FutureAction<Boolean>() {
            public void run() {
                set(second.get());
            }
        };
       
        FutureResult<Boolean> result1 = new FutureResult<Boolean>();
        third.getAsync(result1);
        FutureResult<Boolean> result2 = new FutureResult<Boolean>();
        fourth.getAsync(result2);
        assertFalse(first.isDone());
        assertFalse(second.isDone());
        assertFalse(third.isDone());
        runloop.run();
        assertTrue(result1.get());
        assertTrue(result2.get());
    }
    
    @Test(expected=IllegalStateException.class)
    public void cannotSetBothValueAndException() {
        FutureResult<Boolean> future = new FutureResult<Boolean>();
        future.set(true);
        future.setException(new Exception());
    }
    
    @Test(expected=IllegalStateException.class)
    public void cannotSetValueTwice() {
        FutureResult<Boolean> future = new FutureResult<Boolean>();
        future.set(true);
        future.set(false);
    }
    
    @Test
    public void whenRunMethodThrowsExceptionItIsTrappedAndSetExceptionCalled() {
        FutureAction<Boolean> future = new FutureAction<Boolean>() {            
            public void run() {
                throw new NullPointerException();
            }
        };
        FutureResult<Boolean> result = new FutureResult<Boolean>();
        future.getAsync(result);
        assertTrue(future.isFailure());
        assertTrue(future.getException() instanceof NullPointerException);
    }
    
    @Test
    public void canCancelActionAndChainedResultsAreAlsoCancelled() {
        FutureResult<Boolean> actionThatNeverCompletes = new FutureAction<Boolean>() {            
            public void run() {
                return;
            }
        };
        
        FutureResult<Boolean> result = new FutureResult<Boolean>();
        actionThatNeverCompletes.getAsync(result);
        actionThatNeverCompletes.cancel();
        assertTrue(result.isCancelled());
        
    }
    
    @Test
    public void whenActionIsCancelledSubsequentChainedActionsAreNotRun() {
        final RunLoopSimulator runloop = new RunLoopSimulator();
        final FutureAction<Boolean> first = new FutureAction<Boolean>() {
            public void run() {
               cancel(); 
            }
        };
                
        final FutureAction<Boolean> second = new FutureAction<Boolean>() {
            public void run() {
                runloop.setValueLater(first.get(), this);
            }
        };
        
        final FutureAction<Boolean> third = new FutureAction<Boolean>() {
            public void run() {
                set(first.get());
                throw new AssertionError("Should not be reached");
            }
        };        
        
        final FutureAction<Boolean> fourth = new FutureAction<Boolean>() {
            public void run() {
                set(second.get());
                throw new AssertionError("Should not be reached");
            }
        };
       
        FutureResult<Boolean> result1 = new FutureResult<Boolean>();
        third.getAsync(result1);
        FutureResult<Boolean> result2 = new FutureResult<Boolean>();
        fourth.getAsync(result2);
        assertTrue(first.isCancelled());
        runloop.run();
        assertTrue(result1.isCancelled());
        assertTrue(result2.isCancelled());
        
    }
    
    @Test
    public void canCreatePresentResult() {
        final ConstantResult<Boolean> existing = ConstantResult.constant(true);
        FutureAction<Boolean> result = new FutureAction<Boolean>() {            
            public void run() {
                set(existing.get());
            }
        };
        assertTrue(result.get());
    }
    
    @Test
    public void whenEvalCalledResultEvaluatedButNotReturned() {
        FutureAction<Boolean> action = new FutureAction<Boolean>() {
            public void run() {
                set(true);
            }
        };
        
        // Run action but do not set callback on result
        action.eval();        
        assertTrue(action.get());
        
    }
        
}
