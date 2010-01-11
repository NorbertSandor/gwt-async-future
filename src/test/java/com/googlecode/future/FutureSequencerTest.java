package com.googlecode.future;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import static org.junit.Assert.*;
import static com.googlecode.future.ConstantResult.constant;

public class FutureSequencerTest {

    @Test
    public void canSequenceSingleResult() {
        FutureResult<Boolean> resultToSequence = constant(true);
        FutureSequencer result = new FutureSequencer(resultToSequence);
        assertTrue(result.get());
        assertTrue(resultToSequence.get());
    }
    
    @Test
    public void canSequenceMultipleResults() {
        List<FutureResult<Integer>> resultsToSequence = new ArrayList<FutureResult<Integer>>();
        for (int i=0; i<10; i++) {
            resultsToSequence.add(constant(i));
        }
        FutureSequencer result = new FutureSequencer(resultsToSequence);
        assertTrue(result.get());
        for (int i=0; i<10; i++) {
            assertEquals(i, (int)resultsToSequence.get(i).get());
        }
    }
    
    @Test
    public void canSequenceMultipleResultsWhenResultsDelayed() {
        final RunLoopSimulator runloop = new RunLoopSimulator();
        List<FutureResult<Integer>> resultsToSequence = new ArrayList<FutureResult<Integer>>();
        for (int i=0; i<10; i++) {
            final Integer value = i;
            resultsToSequence.add(new FutureAction<Integer>() {
                public void run() {
                    runloop.setValueLater(value, this);
                }
            });
        }
        FutureSequencer result = new FutureSequencer(resultsToSequence);
        result.eval();
        runloop.run();
        assertTrue(result.get());
        for (int i=0; i<10; i++) {
            assertEquals(i, (int)resultsToSequence.get(i).get());
        }
    }
    
    @Test
    public void whenASequencedResultIsCancelledFutureSequencerIsAlsoCancelled() {
        final RunLoopSimulator runloop = new RunLoopSimulator();
        List<FutureResult<Integer>> resultsToSequence = new ArrayList<FutureResult<Integer>>();
        for (int i=0; i<2; i++) {
            final Integer value = i;
            resultsToSequence.add(new FutureAction<Integer>() {
                public void run() {
                    runloop.setValueLater(value, this);
                }
            });
        }
        resultsToSequence.add(new FutureAction<Integer>() {
            public void run() {
                runloop.cancelLater(this);
            }
        });
        FutureSequencer result = new FutureSequencer(resultsToSequence);
        result.eval();
        runloop.run();
        assertTrue(result.isCancelled());
        // Everything but the last result is completed.
        for (int i=0; i<2; i++) {
            assertEquals(i, (int)resultsToSequence.get(i).get());
        }
    }
    
    @Test
    public void whenASequencedResultFailsFutureSequencerAlsoFails() {
        final int NUMBER_OF_RESULTS_TO_SEQUENCE_AFTER_FAILURE = 2;
        
        List<FutureResult<Integer>> resultsToSequence = new ArrayList<FutureResult<Integer>>();
        resultsToSequence.add(new FutureAction<Integer>() {
            public void run() {
                throw new NullPointerException();                
            }
        });
        // Results which will not evaluated due to above failure
        for (int i=0; i<NUMBER_OF_RESULTS_TO_SEQUENCE_AFTER_FAILURE; i++) {
            final Integer value = i;
            resultsToSequence.add(new FutureAction<Integer>() {
                public void run() {
                    set(value);
                }
            });
        }
        FutureSequencer result = new FutureSequencer(resultsToSequence);
        result.eval();
        assertTrue(result.isFailure());
        // Make sure no result after the first was evaulated
        for (int i=1; i<NUMBER_OF_RESULTS_TO_SEQUENCE_AFTER_FAILURE + 1; i++) {
            assertFalse(resultsToSequence.get(i).isDone());
        }
    }


}
