package com.googlecode.future;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * An action that will complete at some time in the future.
 * 
 * @author Dean Povey
 *
 * @param <T> Type of result
 */
public abstract class FutureAction<T> extends FutureResult<T> implements Runnable {
    
    private Set<FutureResult<?>> dependencies = new HashSet<FutureResult<?>>();
    
    private boolean hasStarted = false;
    
    @Override
    public void getAsync(AsyncCallback<T> callback) {        
        super.getAsync(callback);
        if (!isDone() && !isRunning()) {
            tryRunningTaskButIgnoreExceptions();
        }
    }

    @Override
    public T get() {
        if (isDone()) return super.get();
        if (isRunning()) throw new IncompleteResultException(this);
        if (hasUnresolvedDependencies()) {
            throw new IncompleteResultException(this, 
                    new IncompleteResultException(dependencies.iterator().next()));
        }
        try {            
            run();
            setRunning(true);
        } catch(IncompleteResultException e) {
            final FutureResult<?> dependency = e.getFuture();
            addDependency(dependency);
            throw new IncompleteResultException(this, e);
        } catch(Throwable t) {
            setException(t);
        }
        return super.get();
    }
    
    @Override
    public void set(T value) {
        setRunning(false);
        super.set(value);
    }
    
    @Override
    public void setException(Throwable t) {
        setRunning(false);
        super.setException(t);
    }
    
    @Override
    public void cancel() {
        setRunning(false);
        super.cancel();
    }

    private void setRunning(boolean b) {
        hasStarted = b;
    }

    private boolean hasUnresolvedDependencies() {
        return dependencies.size() > 0;
    }

    private boolean isRunning() {
        return hasStarted && !isDone();
    }

    @SuppressWarnings("unchecked")
    private void addDependency(final FutureResult<?> dependency) {
        if (!dependencies.contains(dependency)) {
            dependencies.add(dependency);
            dependency.getAsync(new AsyncCallback() {

                public void onFailure(Throwable t) {
                    if (t instanceof CancelledException) FutureAction.this.cancel();
                    else FutureAction.this.onFailure(
                            new ExecutionException("Failure resolving dependency", t));
                }

                public void onSuccess(Object result) {
                    dependencies.remove(dependency);                    
                    tryRunningTaskButIgnoreExceptions();                    
                }
                
            });
        }
    }
    
    private void tryRunningTaskButIgnoreExceptions() {        
        try {
            get();
        } catch(Throwable t) {
            // Squash.  This is a little dangerous however any exceptions should be
            // caught in the get() method and then set in the result.
        }
    }

}
