package com.googlecode.future;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;
import static com.googlecode.future.ExecutionException.returnIfCheckedThrowIfUnchecked;

/**
 * Represents the future results of some operation which may either succeed or fail.
 * 
 * <p>{@link FutureResult} provides an alternative way to process asynchronous operations.  Rather
 * than providing a callback, a FutureResult is either passed in or returned from an asynchronous
 * method.  When a result is available it may be obtained by using the {@link get} method.  If 
 * get is called and no results are available then an {@link IncompleteResultException} will be
 * thrown.  If the operation threw an exception then an {@link ExecutionException} will be thrown
 * with the underlying cause.
 * 
 *  <p>FutureResult may be used as a replacement for an {@link AsyncCallback}, for example:
 *  
 *  <code><pre>
 *  RemoteInterfaceAsync remote = GWT.create(RemoteInterface.class);
 *  FutureResult<Boolean> result = new FutureResult<Boolean>();
 *  remote.callRemoteMethod("parameter", result);
 *  ....
 *  // At Some later time
 *  if (result.isDone()) {
 *      boolean success = result.getValue();
 *  } 
 *  <pre></code>
 *  
 *  <p>In general a FutureResult will be used to collect a result from 
 *  one or more {@link FutureAction} instances.
 * 
 * @author Dean Povey
 *
 * @param <T> Type of result
 */
public class FutureResult<T> implements AsyncCallback<T> {
    
    private T value = null;
    
    private Throwable exception = null;
    
    private LinkedHashSet<AsyncCallback<T>> listeners = new LinkedHashSet<AsyncCallback<T>>();
    
    private enum State { SUCCEEDED, FAILED, INCOMPLETE, CANCELLED }
    
    private State state = State.INCOMPLETE;

    /**
     * Get the result if available.
     * 
     * @return result
     * @throws IncompleteResultException If result is not yet available
     * @throws ExecutionException If operation failed
     */
    public T get() throws IncompleteResultException, ExecutionException {
        switch(state) {
        case INCOMPLETE: throw new IncompleteResultException(this);
        case FAILED: throw new ExecutionException(returnIfCheckedThrowIfUnchecked(exception));
        case CANCELLED: throw new CancelledException();
        case SUCCEEDED: return value;
         
        }
        throw new IllegalStateException();
    }

    /**
     * Invoke the callback when a result becomes available.  This method may be called
     * multiple times with different callbacks.  If the same callback is used multiple times
     * it will only be called once when a result becomes available, otherwise each callback
     * is invoked in the order in which this method was called.
     * 
     * @param callback Callback to invoke
     */
    public void getAsync(AsyncCallback<T> callback) {
        if (isDone()) {
            if (isSuccessful()) callback.onSuccess(value);
            else callback.onFailure(this.exception);
            return;
        }
       listeners.add(callback);
    }

    /**
     * Whether a result is available.
     * 
     * @return true if result is available, was cancelled, or an exception
     *         occurred, false otherwise.
     */
    public boolean isDone() {
        return state != State.INCOMPLETE;
    }
    
    /**
     * Whether the operation was successful.
     * 
     *  @return true if operation succeeded, false if operation failed or is 
     *      incomplete. 
     */
    public boolean isSuccessful() {
        return state == State.SUCCEEDED;
    }
    
    /**
     * Whether the operation was successful.
     * 
     * @return true if operation failed with an exception, false if operation succeeded or is 
     *  incomplete. 
     */
    public boolean isFailure() {
        return state == State.FAILED;
    }
    
    /**
     * Get the exception which was previously set.
     * 
     *  @return the exception or null if no exception was set.
     */
    public Throwable getException() {
        return this.exception;
    }

    /**
     * Set exception.
     * 
     * @param t Exception to set.
     */
    public void setException(Throwable t) {
        if (isDone()) {            
            throw new IllegalStateException("Cannot set Future state twice");
        }
        state = State.FAILED;
        this.exception = t;
        done();        
        notifyListenersOnFailure();
    }

    /**
     * Used to set a FutureResult value.  In general this should not be called directly.
     * 
     * @param value Value to set.
     */
    public void set(T value) {
        if (isDone()) {            
            throw new IllegalStateException("Cannot set Future state twice");
        }
        state = State.SUCCEEDED;
        this.value = value;
        done();
        notifyListenersOnSuccess(value);        
    }

    private void notifyListenersOnSuccess(T value) {
        for (AsyncCallback<T> callback : copyCallbacksThenClear()) {
            callback.onSuccess(value);
        }
    }
    
    private List<AsyncCallback<T>> copyCallbacksThenClear() {
        List<AsyncCallback<T>> callbacks = new ArrayList<AsyncCallback<T>>(this.listeners);
        this.listeners.clear();
        return callbacks;
    }
    

    public final void onFailure(Throwable t) {
        if (t instanceof CancelledException) cancel();
        else setException(t);
    }

    public final void onSuccess(T value) {
        set(value);
    }
    
    /**
     * Method called when either a result becomes available or an exception is set. Subclasses
     * may override this to provide custom processing.
     */
    protected void done() {        
    }

    public void cancel() {
        if (isDone()) return;
        state = State.CANCELLED;
        this.exception = new CancelledException();
        done();        
        notifyListenersOnFailure();
        
    }

    private void notifyListenersOnFailure() {
        for (AsyncCallback<T> callback : copyCallbacksThenClear()) {
            callback.onFailure(this.exception);
        }
    }

    public boolean isCancelled() {
        return state == State.CANCELLED;
    }

}
