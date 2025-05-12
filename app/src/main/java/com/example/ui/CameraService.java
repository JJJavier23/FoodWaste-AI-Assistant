package com.example.ui;

public class CameraService {
    public Image captureImage() {
        System.out.println("Capturing image...");
        return new Image("captured_image_data");
    }
}
