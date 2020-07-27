package com.example.fingerprint_protection;

import android.graphics.Bitmap;

public class detailsAdapter {
    private int id;
    private String title;
    private String shortdesc;
    private Bitmap image;

    public detailsAdapter(int id, String title, String shortdesc, Bitmap image) {
        this.id = id;
        this.title = title;
        this.shortdesc = shortdesc;
        this.image = image;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getShortdesc() {
        return shortdesc;
    }

    public Bitmap getImage() {
        return image;
    }
}