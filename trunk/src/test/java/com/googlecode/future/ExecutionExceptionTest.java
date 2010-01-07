package com.googlecode.future;

import java.io.IOException;

import org.junit.Test;

public class ExecutionExceptionTest {
    @SuppressWarnings("unchecked")
    @Test(expected=IOException.class)
    public void shouldRethrowCheckedException() throws IOException {
        ExecutionException e = new ExecutionException(new IOException());
        throw (IOException) e.getCheckedCauseOrRethrow(IOException.class);
    }
}
