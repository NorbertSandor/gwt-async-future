package com.googlecode.future;

import java.util.ArrayList;
import java.util.LinkedHashSet;

import com.google.gwt.user.client.rpc.AsyncCallback;

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
    
    private boolean isDone = false;
    
    private LinkedHashSet<AsyncCallback<T>> callbacks = new LinkedHashSet<AsyncCallback<T>>();

    /**
     * Get the result if available.
     * 
     * @return result
     * @throws IncompleteResultException If result is not yet available
     * @throws ExecutionException If operation failed
     */
    public T get() throws IncompleteResultException, ExecutionException {        
        if (!isDone) throw new IncompleteResultException(this);
        if (exception != null) throw new ExecutionException(exception);
        return value;
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
       callbacks.add(callback);
    }

    /**
     * Whether a result is available.
     * 
     * @return true if result is available or an exception occurred, false otherwise.
     */
    public boolean isDone() {
        return isDone;
    }

    /**
     * Set 
     * 
     * @param t
     */
    public void setException(Throwable t) {
        if (isDone()) {            
            throw new IllegalStateException("Cannot set Future state twice");
        }
        isDone = true;
        this.exception = t;
        done();       
        for (AsyncCallback<T> callback : copyOfCallbacks()) {
            callback.onFailure(t);
        }
    }

    private ArrayList<AsyncCallback<T>> copyOfCallbacks() {
        return new ArrayList<AsyncCallback<T>>(callbacks);
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
        isDone = true;
        this.value = value;
        done();
        for (AsyncCallback<T> callback : copyOfCallbacks()) {
            callback.onSuccess(value);
        }        
    }

    public final void onFailure(Throwable t) {
        setException(t);
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

}
