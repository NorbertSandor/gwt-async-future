package com.googlecode.future;


public abstract class AutoFutureAction<T> extends FutureAction<T> implements AutoFuture<T> {
    public AutoFutureAction() {
        eval();
    }
}
