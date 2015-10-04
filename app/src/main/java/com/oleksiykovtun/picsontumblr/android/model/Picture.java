package com.oleksiykovtun.picsontumblr.android.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Picture in a blog
 */
public class Picture extends ContentItem implements Serializable {

    private long timestamp;
    private String postUrl;
    private String originalBlogUrl;
    private String currentBlogUrl;
    private String rebloggedFromName;
    private boolean isLiked = false;
    private boolean isReblogged = false;
    private boolean isRemoved = false;
    private List<SizedPicture> sizedPictures = new ArrayList<>();
    private int postNumber;
    private long postId;
    private String reblogKey;

    public boolean isLiked() {
        return isLiked;
    }

    public void setIsLiked(boolean isLiked) {
        this.isLiked = isLiked;
    }

    public List<SizedPicture> getSizedPictures() {
        return sizedPictures;
    }

    public void setSizedPictures(List<SizedPicture> sizedPictures) {
        this.sizedPictures = sizedPictures;
    }

    public String getRebloggedFromName() {
        return rebloggedFromName;
    }

    public void setRebloggedFromName(String rebloggedFromName) {
        this.rebloggedFromName = rebloggedFromName;
    }

    public int getPostNumber() {
        return postNumber;
    }

    public void setPostNumber(int postNumber) {
        this.postNumber = postNumber;
    }

    @Override
    public String getUrl() {
        // back compatibility for picture history
        if (!getSizedPictures().isEmpty()) {
            SizedPicture sizeToGetUrl =
                    getSizedPictures().get(Math.max(0, getSizedPictures().size() - 5));
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

    public long getPostId() {
        return postId;
    }

    public void setPostId(long postId) {
        this.postId = postId;
    }

    public String getReblogKey() {
        return reblogKey;
    }

    public void setReblogKey(String reblogKey) {
        this.reblogKey = reblogKey;
    }
}
