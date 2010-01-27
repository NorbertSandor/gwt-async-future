package com.googlecode.future;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * Converts any future to an AutoFuture, ie. a future that is always evaluated.
 * 
 * @author Dean Povey
 *
 * @param <T>
 */
public class Auto<T> implements AutoFuture<T> {
    
    private Future<T> future;

    public static <T> AutoFuture<T> auto(Future<T> future) {
        return new Auto<T>(future);
    }
    
    private Auto(Future<T> future) {
        this.future = future;
        future.eval();
    }

    public void cancel() {
        future.cancel();
    }

    public void eval() {
        future.eval();
    }

    public T result() throws IncompleteResultException, ExecutionException {
        return future.result();
    }

    public void addCallback(AsyncCallback<T> callback) {
        future.addCallback(callback);
    }

    public Throwable exception() {
        return future.exception();
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isComplete() {
        return future.isComplete();
    }

    public boolean isFailure() {
        return future.isFailure();
    }

    public boolean isSuccessful() {
        return future.isSuccessful();
    }

    public void returnResult(T value) {
        future.returnResult(value);
    }

    public void returnEmpty() {
        future.returnEmpty();
    }
    
    public void failWithException(Throwable t) {
        future.failWithException(t);
    }

    public CancellableAsyncCallback<T> callback() {
        return future.callback();
    }

    
}
