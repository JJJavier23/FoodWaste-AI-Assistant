package com.example.ui;

import androidx.xr.scenecore.Image;


public class SaveImage implements ICommand{
    private ImageRepo imageRepo;
    private Image image;
    private String location;

    public SaveImage(ImageRepo repo, Image image) {
        this.imageRepo = repo;
        this.image = image;
    }

    @Override
    public void execute() {
        location = imageRepo.saveImage((android.media.Image) image);
    }

    @Override
    public void undo() {
        if (location != null) {
            imageRepo.deleteImage(location);
        }
    }
}
