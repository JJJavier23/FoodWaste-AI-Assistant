package com.example.ui;

public class AnalyzerState extends AppActivityView {
    public void processingState() {
        System.out.println("Processing image");
    }

    @Override
    public void enter() {
        System.out.println("Entered Analyzer State");
    }

    public void onExit() {
        System.out.println("Exiting Analyzer State");
    }

    public void startAnalysis(Image image) {
        System.out.println("Starting analysis on image: " + image.getData());
    }

    public void userActionHandle() {
        processingState();
    }

    @Override
    public void start() {}

    @Override
    public PictureCaptureState nextState() {
        return state4;
    }

    @Override
    public void processing() {}

    @Override
    public void processEvent(String event) {
        userActionHandle();
    }

}
