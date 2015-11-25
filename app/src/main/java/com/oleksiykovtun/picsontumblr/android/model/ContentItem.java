package com.oleksiykovtun.picsontumblr.android.model;

import java.io.Serializable;

/**
 * Item of content of some kind
 */
public abstract class ContentItem implements Serializable {

    private String url;
    private String title;
    private long openingTime;
    private long previousVisitTime;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public long getOpeningTime() {
        return openingTime;
    }

    public void setOpeningTime(long openingTime) {
        this.openingTime = openingTime;
    }

    public long getPreviousVisitTime() {
        return previousVisitTime;
    }

    public void setPreviousVisitTime(long previousVisitTime) {
        this.previousVisitTime = previousVisitTime;
    }
}
