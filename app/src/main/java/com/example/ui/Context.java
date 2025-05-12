package com.example.ui;

public class Context {
    private AppActivityView currentState;

    public void setState(AppActivityView state) {
        this.currentState = state;
    }

    public void processEvent(String event) {
        currentState.processEvent(event);
    }
}
