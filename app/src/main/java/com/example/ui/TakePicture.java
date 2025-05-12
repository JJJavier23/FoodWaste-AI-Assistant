package com.example.ui;

public class TakePicture implements ICommand{
    private CameraService camera;
    private com.example.ui.Image takenImage;

    public TakePicture(CameraService camera) {
        this.camera = camera;
    }

    @Override
    public void execute() {
        takenImage = camera.captureImage();
    }

    @Override
    public void undo() {
        System.out.println("Undo TakePicture: Not supported");
    }

    public Image getTakenImage() {
        return takenImage;
    }

}
