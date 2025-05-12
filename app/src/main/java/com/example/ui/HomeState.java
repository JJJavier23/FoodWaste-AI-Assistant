package com.example.ui;

public class HomeState extends AppActivityView {
    @Override
    public void start() {
        System.out.println("HomeState: Starting app");
    }

    @Override
    public void enter() {
        System.out.println("Entered Home State");
    }

    public void onExit() {
        System.out.println("Exiting Home State");
    }

    public void navigateToCamera() {
        System.out.println("Navigating to Camera");
    }

    public void userActionHandle() {
        navigateToCamera();
    }

    @Override
    public PictureCaptureState nextState() {
        return state2;
    }

    @Override
    public void processing() {}

    @Override
    public void processEvent(String event) {
        userActionHandle();
    }
}
