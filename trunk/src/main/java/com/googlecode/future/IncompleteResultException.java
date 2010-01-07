package com.googlecode.future;

/**
 * Exception thrown when calling get on a {@link FutureResult} or {@link FutureAction} that is not
 * complete.  The FutureResult which is not complete is included in the exception. If the result is
 * not available due to some unresolved dependency on another FutureResult then the nested cause
 * may contain another IncompleteResultException.
 * 
 * @author Dean Povey
 *
 */
public class IncompleteResultException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    
    private final FutureResult<?> future;

    public IncompleteResultException(FutureResult<?> future) {
        this.future = future;
    }

    public IncompleteResultException(FutureResult<?> future, Throwable cause) {
        this(future);
        initCause(cause);
    }

    public FutureResult<?> getFuture() {
        return future;
    }

}
