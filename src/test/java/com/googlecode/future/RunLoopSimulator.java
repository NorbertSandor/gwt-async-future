package com.googlecode.future;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class RunLoopSimulator implements Runnable {
    List<AsyncCallback<Object>> callbacks = new ArrayList<AsyncCallback<Object>>();
    List<Object> values = new ArrayList<Object>();
    
    @SuppressWarnings("unchecked")
    public <T> void setValueLater(T value, AsyncCallback<T> callback) {
        assert !(value instanceof Throwable) : "Cannot specify Throwable as value. Use failLater"; 
        callbacks.add((AsyncCallback<Object>) callback);
        values.add(value);
        assert callbacks.size() == values.size();
    }
    
    @SuppressWarnings("unchecked")
    public <T> void failLater(Throwable t, AsyncCallback<T> callback) {        
        callbacks.add((AsyncCallback<Object>) callback);
        values.add(t);
        assert callbacks.size() == values.size();
    }

    public void run() {
        assert callbacks.size() == values.size();
        for (int i=0; i<callbacks.size(); i++) {
            Object v = values.get(i);
            if (v instanceof Throwable) callbacks.get(i).onFailure((Throwable)v);
            else callbacks.get(i).onSuccess(v);
        }
    }


}
