package com.oleksiykovtun.picsontumblr.android.model;

import java.io.Serializable;

/**
 * Picture defined by URL, width and height
 */
public class SizedPicture implements Serializable {

    private int width, height;
    private String url;

    public SizedPicture(String url, int width, int height) {
        this.width = width;
        this.height = height;
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

}
