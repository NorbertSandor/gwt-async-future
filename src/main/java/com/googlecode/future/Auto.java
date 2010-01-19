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

    public T get() throws IncompleteResultException, ExecutionException {
        return future.get();
    }

    public void getAsync(AsyncCallback<T> callback) {
        future.getAsync(callback);
    }

    public Throwable getException() {
        return future.getException();
    }

    public boolean isCancelled() {
        return future.isCancelled();
    }

    public boolean isDone() {
        return future.isDone();
    }

    public boolean isFailure() {
        return future.isFailure();
    }

    public boolean isSuccessful() {
        return future.isSuccessful();
    }

    public void set(T value) {
        future.set(value);
    }

    public void setEmpty() {
        future.setEmpty();
    }
    
    public void setException(Throwable t) {
        future.setException(t);
    }

    
}
