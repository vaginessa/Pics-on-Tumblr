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

    private long timestamp;
    private String postUrl;
    private String originalBlogUrl;
    private String currentBlogUrl;
    private boolean isLiked = false;
    private boolean isReblogged = false;
    private boolean isRemoved = false;
    private PhotoPost photoPost;
    private List<PhotoSize> photoSizes = new ArrayList<>();
    private int postNumber;

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

    public int getPostNumber() {
        return postNumber;
    }

    public void setPostNumber(int postNumber) {
        this.postNumber = postNumber;
    }

    public String getUrl() {
        // back compatibility for picture history
        if (!getPhotoSizes().isEmpty()) {
            PhotoSize sizeToGetUrl = getPhotoSizes().get(Math.max(0, getPhotoSizes().size() - 5));
            return sizeToGetUrl.getUrl();
        } else {
            return "";
        }
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

    public void setCurrentBlogUrl(String currentBlogUrl) {
        this.currentBlogUrl = currentBlogUrl;
    }

    public String getCurrentBlogUrl() {
        return currentBlogUrl;
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
