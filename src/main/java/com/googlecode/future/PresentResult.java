package com.googlecode.future;

/**
 * Result of an operation that is available at declaration time, but needs to be accessed
 * as a FutureResult.
 * 
 * @author Dean Povey
 *
 * @param <T> Type of result
 */
public class PresentResult<T> extends FutureResult<T> {
        
    /**
     * Create a PresentResult with the given value.
     * 
     * @param value value to set the result to.
     */
    public PresentResult(T value) {
        set(value);
    }    
    
    
    /**
     * Convenience factory method that can be used to create a PresentResult.
     * 
     * @param <T> Type of result
     * @param value value to set the result to
     * @return a PresentResult containing the specified value.
     */
    public static <T> PresentResult<T> presentResult(T value) {
        return new PresentResult<T>(value); 
    }    

}
