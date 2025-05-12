package com.example.ui;

import android.media.Image;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ImageRepo {
    private final Map<String, Image> images = new HashMap<>();

    public String saveImage(Image image) {
        String location = UUID.randomUUID().toString();
        images.put(location, image);
        System.out.println("Saved image at " + location);
        return location;
    }

    public void deleteImage(String location) {
        images.remove(location);
        System.out.println("Deleted image at " + location);
    }

    public Image getImage(String location) {
        return images.get(location);
    }
}
