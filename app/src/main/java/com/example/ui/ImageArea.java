package com.example.ui;

public class ImageArea {
    public void render() {
        System.out.println("Rendering image area...");
    }

    public void displayImage(Image img) {
        System.out.println("Displaying image with data: " + (img != null ? img.getData() : "null"));
    }
}
