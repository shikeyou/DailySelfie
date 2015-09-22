package com.ksl.dailyselfie;

import java.io.File;

public class Selfie {

    //link to thumbnail file (not the Bitmap itself so that it is faster to save and load from prefs)
    private File thumbnailFile;

    //link to the full size image file (not the Bitmap itself)
    private File imageFile;

    public Selfie(File thumbnailFile, File imageFile) {
        this.thumbnailFile = thumbnailFile;
        this.imageFile = imageFile;
    }

    public File getThumbnailFile() {
        return thumbnailFile;
    }

    public void setThumbnailFile(File thumbnailFile) {
        this.thumbnailFile = thumbnailFile;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void setImageFile(File imageFile) {
        this.imageFile = imageFile;
    }
}
