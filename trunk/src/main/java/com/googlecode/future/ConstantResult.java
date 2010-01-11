package com.googlecode.future;

/**
 * Result of an operation that is constant but needs to be accessed as a FutureResult.
 * 
 * @author Dean Povey
 *
 * @param <T> Type of result
 */
public class ConstantResult<T> extends FutureResult<T> {
        
    /**
     * Create a PresentResult with the given value.
     * 
     * @param value value to set the result to.
     */
    public ConstantResult(T value) {
        set(value);
    }       
    
    /**
     * Convenience factory method that can be used to create a {@link ConstantResult}
     * 
     * @param <T> Type of result
     * @param value value to set the result to
     * @return a ConstantResult containing the specified value.
     */
    public static <T> ConstantResult<T> constant(T value) {
        return new ConstantResult<T>(value); 
    }    

}
