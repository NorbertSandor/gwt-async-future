package com.googlecode.future;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 *  Represents the future results of some operation which may either succeed or fail.
 * 
 * <p>A {@link Future} provides an alternative way to process asynchronous operations.  Rather
 * than providing a callback, a Future is either passed in or returned from an asynchronous
 * method.  When a result is available it may be obtained by using the {@link get} method.  If 
 * get is called and no results are available then an {@link IncompleteResultException} should be
 * thrown.  If the operation threw an exception then an {@link ExecutionException} should be thrown
 * with the underlying cause.
 * 
 * @author Dean Povey
 * 
 * @see {@link FutureResult}
 * @see {@link FutureAction}
 *
 * @param <T> Type of the result.
 */
public interface Future<T> {

    /**
     * Get the result if available.
     * 
     * @return result
     * @throws IncompleteResultException If result is not yet available
     * @throws ExecutionException If operation failed
     */
    public abstract T get() throws IncompleteResultException,
            ExecutionException;

    /**
     * Invoke the callback when a result becomes available.  This method may be called
     * multiple times with different callbacks.  If the same callback is used multiple times
     * it will only be called once when a result becomes available, otherwise each callback
     * is invoked in the order in which this method was called.
     * 
     * @param callback Callback to invoke
     */
    public abstract void getAsync(AsyncCallback<T> callback);

    /**
     * Whether a result is available.
     * 
     * @return true if result is available, was cancelled, or an exception
     *         occurred, false otherwise.
     */
    public abstract boolean isDone();

    /**
     * Whether the operation was successful.
     * 
     *  @return true if operation succeeded, false if operation failed or is 
     *      incomplete. 
     */
    public abstract boolean isSuccessful();

    /**
     * Whether the operation was successful.
     * 
     * @return true if operation failed with an exception, false if operation succeeded or is 
     *  incomplete. 
     */
    public abstract boolean isFailure();

    /**
     * Get the exception which was previously set.
     * 
     *  @return the exception or null if no exception was set.
     */
    public abstract Throwable getException();

    /**
     * Set exception.
     * 
     * @param t Exception to set.
     */
    public abstract void setException(Throwable t);

    /**
     * Used to set a FutureResult value.  In general this should not be called directly.
     * 
     * @param value Value to set.
     */
    public abstract void set(T value);
    
    /**
     * Set the result to an empty value (e.g. null).
     */
    public abstract void setEmpty();

    public abstract void cancel();

    public abstract boolean isCancelled();

    /**
     * Evaluate the result but do not register a callback to be notified when complete.
     * This should be used to evaluate a chain of actions where the eventual final
     * result is not needed.
     */
    public abstract void eval();

}