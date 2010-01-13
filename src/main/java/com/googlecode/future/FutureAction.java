package com.googlecode.future;

import java.util.HashSet;
import java.util.Set;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * A FutureAction represents an action that may be run at some future point
 * (possibly never). It implements {@link Runnable} and the {@link #run()}
 * method is used to specify the action to perform when run. There are some
 * constraints on the run() method, either it must be side-effect free (ie.
 * there must be no method calls which perform an action that cannot be
 * performed multiple times, e.g displaying a result or calling a service), or
 * it must ensure that any references to other {@link Future} instances happen
 * before any side-effects. This quirk comes about because of the way a
 * FutureAction evaluates its run method. If a dependent Future is incomplete,
 * accessing it via {@link #get()} will throw an IncompleteResultException
 * including a reference to the Future which is incomplete. The
 * FutureAction adds a callback so that when this value becomes available then
 * it will re-call the run method. If there are multiple dependencies this
 * continues until all are satisfied and then the side effects can be run. A
 * Future can only be assigned to once so this ensures that once a result
 * is available a call to get() is idempotent, and once the run method() completes
 * successfully (or fails) then it will not be run again.
 * 
 * <p>
 * It is a very important to note that creating a FutureAction does not cause
 * the action to be run. This will only happen if either the {@link #get()}, 
 * {@link #getAsync()}
 * or the {@link #eval()} is called. The effect of this is that
 * calling of chained asynchronous actions optimized so that they are only run
 * if they are needed and this approach automatically handles for example
 * boolean shortcutting while resolving dependencies. e.g.
 * 
 * <code><pre>
 * FutureAction<Boolean> succeeds = new FutureAction<Boolean>() {
 *    public void run() {
 *        set(true);
 *   }
 * }
 * 
 * FutureAction<Boolean> neverCalled = new FutureAction<Boolean>() {
 *    public void run() {
 *        throw new AssertionError("Should never be called");
 *    }
 * }
 * 
 * FutureAction<Boolean> result = new FutureAction<Boolean>() {
 *    public void run() {
 *        set(succeeds.get() || fails.get()); // Note: fails will never be run!
 *    }
 * }
 * </pre></code>
 * 
 * <p>
 * In addition, a given action may call a dependent Future as many times
 * as it likes but it will only be evaluated once and the same result returned
 * thereafter.
 * 
 * <p>
 * It should be noted that the FutureAction must either call {@link #set()} or pass itself
 * to a method which will call its onSuccess method on a successful result. If
 * this does not happen then the action will never complete successfully.
 * 
 * <p>
 * To set an exception the action can either just throw the exception if it is
 * unchecked or else it can call the {@link #setException(Throwable)} method.
 * 
 * @author Dean Povey
 * 
 * @see {@link FutureResult}
 * 
 * @param <T> Type of result
 */
public abstract class FutureAction<T> extends FutureResult<T> implements Runnable {
    
    private Set<Future<?>> dependencies = new HashSet<Future<?>>();
    
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
            final Future<?> dependency = e.getFuture();
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
    private void addDependency(final Future<?> dependency) {
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
            // Squash.  This is a little, dangerous however any exceptions should be
            // caught in the get() method and then set in the result.
        }
    }

}
