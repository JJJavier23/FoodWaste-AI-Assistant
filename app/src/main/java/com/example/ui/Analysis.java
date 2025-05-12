package com.example.ui;

import android.media.Image;

public class Analysis {
    public String saveImage(Image image) {
        System.out.println("Image saved for analysis");
        return "analysis_location";
    }

    public void deleteImage(String location) {
        System.out.println("Deleted image from analysis location: " + location);
    }

    public com.example.ui.Image getImage(String location) {
        System.out.println("Retrieving image from analysis location: " + location);
        return new com.example.ui.Image("analyzed_image_data");
    }
}
