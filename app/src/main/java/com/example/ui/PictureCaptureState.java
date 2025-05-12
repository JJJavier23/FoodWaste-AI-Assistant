package com.example.ui;

public class PictureCaptureState extends AppActivityView {
    private Image currentImage;

    @Override
    public void enter() {
        System.out.println("Entered Picture Capture State");
    }

    public void onExit() {
        System.out.println("Exiting Picture Capture State");
    }

    public void takePicture() {
        currentImage = new Image("CapturedImage");
        System.out.println("Picture Taken");
    }

    public void confirmPicture() {
        System.out.println("Picture Confirmed");
    }

    public void retakePicture() {
        takePicture();
    }

    public void userActionHandle() {
        takePicture();
    }

    @Override
    public void start() {}

    @Override
    public PictureCaptureState nextState() {
        return state3.nextState();
    }

    @Override
    public void processing() {}

    @Override
    public void processEvent(String event) {
        userActionHandle();
    }

}
