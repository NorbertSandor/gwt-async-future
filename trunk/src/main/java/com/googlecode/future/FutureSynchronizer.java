package com.googlecode.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * A FutureResult that returns true when one or more other results have values. If one
 * ore more of the synchronized results is cancelled then the FutureSynchronizer will be
 * cancelled, however if one or more results fails with an exception then that exception
 * will not be propagated to the FutureSynchronizer instance and it will still succeed.
 * 
 * @author Dean Povey
 *
 */
public class FutureSynchronizer extends FutureAction<Boolean> {
   
    private List<FutureResult<?>> resultsToSynchronizeWith;
    
    public FutureSynchronizer(FutureResult<?>...resultsToSynchronizeWith) {
        this.resultsToSynchronizeWith = asList(resultsToSynchronizeWith);
    }

    public FutureSynchronizer(
            Collection<FutureResult<Integer>> resultsToSynchronizeWith) {
        this.resultsToSynchronizeWith = new ArrayList<FutureResult<?>>(resultsToSynchronizeWith);
    }

    public void run() {
        for (FutureResult<?> result : resultsToSynchronizeWith) {
            result.eval();
        }
        
        for (FutureResult<?> result : resultsToSynchronizeWith) {                
            if (!result.isDone()) {
                try {
                    result.get();
                } catch(IncompleteResultException e) {
                    throw e;
                } catch(CancelledException e) {
                    throw e;
                } catch(Throwable t) {
                    assert result.isFailure();
                    // Squash
                }
            }
        }
        set(true);
    }

}
