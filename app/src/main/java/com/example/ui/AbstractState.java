package com.example.ui;

public abstract class   AbstractState {
    protected static HomeState state1 = new HomeState();
    protected static PictureCaptureState state2 = new PictureCaptureState();
    protected static AnalyzerState state3 = new AnalyzerState();
    protected static ResultsState state4 = new ResultsState();

    public abstract void start();
    public abstract void enter();
    public abstract PictureCaptureState nextState();
    public abstract void processing();
    public abstract void processEvent(String event);




}
