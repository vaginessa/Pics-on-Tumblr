package com.oleksiykovtun.picsontumblr.android.model;

import android.util.Log;

import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Picture in a blog
 */
public class Picture implements Serializable {

    private String url;
    private long timestamp;
    private String postUrl;
    private String originalBlogUrl;
    private String currentBlogUrl;
    private boolean isLiked = false;
    private boolean isReblogged = false;
    private boolean isRemoved = false;
    private PhotoPost photoPost;
    private List<PhotoSize> photoSizes = new ArrayList<>();
    private int width;
    private int height; // todo multiple sizes

    public boolean isLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public void reblog(String blogName) {
        try {
            photoPost.reblog(blogName);
        } catch (Throwable e) {
            Log.e("Instantr", "Reblogging failed", e);
        }
    }

    public List<PhotoSize> getPhotoSizes() {
        return photoSizes;
    }

    public void setPhotoSizes(List<PhotoSize> photoSizes) {
        this.photoSizes = photoSizes;
    }

    public PhotoPost getPhotoPost() {
        return photoPost;
    }

    public void setPhotoPost(PhotoPost photoPost) {
        this.photoPost = photoPost;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getPostUrl() {
        return postUrl;
    }

    public void setPostUrl(String postUrl) {
        this.postUrl = postUrl;
    }

    public String getOriginalBlogUrl() {
        return originalBlogUrl;
    }

    public void setOriginalBlogUrl(String originalBlogUrl) {
        this.originalBlogUrl = originalBlogUrl;
    }

    public String getCurrentBlogUrl() {
        return currentBlogUrl;
    }

    public void setCurrentBlogUrl(String currentBlogUrl) {
        this.currentBlogUrl = currentBlogUrl;
    }

    public boolean isReblogged() {
        return isReblogged;
    }

    public void setIsReblogged(boolean isReblogged) {
        this.isReblogged = isReblogged;
    }

    public boolean isRemoved() {
        return isRemoved;
    }

    public void setIsRemoved(boolean isRemoved) {
        this.isRemoved = isRemoved;
    }
}
