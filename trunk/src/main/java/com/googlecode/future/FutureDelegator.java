package com.googlecode.future;

import java.util.ArrayList;
import java.util.List;


/**
 * A future that evaluates a series of futures in sequence and returns when one
 * of them fufills the criteria of the {@link isFinished} method.  If this method
 * is not overriden then the default implementation checks for a non-null value.
 * 
 * @author Dean Povey
 *
 * @param <T> type to return
 */
public class FutureDelegator<T> extends FutureAction<T> {
    private final List<Future<T>> futures = new ArrayList<Future<T>>();
    private int nextFuture = 0;
    
    public FutureDelegator(Future<T>...futures) {
        for (Future<T> future : futures) {
            this.futures.add(future);
        }
    }
    
    public FutureDelegator(Iterable<Future<T>> futures) {
        for (Future<T> future : futures) {
            this.futures.add(future);
        } 
    }
    
    public void run() {
        for (int i=nextFuture; i < futures.size(); i++) {
            T result = futures.get(i).get();
            this.nextFuture++;
            if (isFinished(result)) {
                set(result);
                return;
            }
        }
        set(null);
    }

    public boolean isFinished(T result) {
        return result != null;
    }
}