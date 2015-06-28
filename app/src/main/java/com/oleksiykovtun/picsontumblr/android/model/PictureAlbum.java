package com.oleksiykovtun.picsontumblr.android.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * A list of pictures. Can be a blog or a search result with pictures
 */
public class PictureAlbum {

    private static final String DASHBOARD = "dashboard";
    private static final int DASHBOARD_LIMIT = 250;
    private static final int LIKES_LIMIT = 1000;

    private String url;
    private List<Picture> pictureList = new ArrayList<>();
    private int currentMaxPosts = 0; // currently loaded posts within the limit
    private int currentPhotoPostCount = 0;
    private int postsLimit = Integer.MAX_VALUE;
    private boolean showLikesInsteadOfPosts = false;
    private boolean search = false;
    private boolean showRandomly = false;
    private static Random random = new Random();
    private int loadPostsStep = 10;
    private boolean loading = true;

    public PictureAlbum(String url) {
        this.url = url;
        if (url != null && url.equals(DASHBOARD)) {
            setPostsLimit(DASHBOARD_LIMIT);
        }
    }

    public PictureAlbum likesMode(boolean showLikesInsteadOfPosts) {
        this.showLikesInsteadOfPosts = showLikesInsteadOfPosts;
        if (showLikesInsteadOfPosts) {
            setPostsLimit(LIKES_LIMIT);
        }
        return this;
    }

    public PictureAlbum randomMode(boolean showRandomly) {
        this.showRandomly = showRandomly;
        return this;
    }

    public PictureAlbum searchMode(boolean search) {
        this.search = search;
        return this;
    }

    public boolean isSearch() {
        return search;
    }

    public void setSearch(boolean search) {
        this.search = search;
    }

    public boolean isShowLikesInsteadOfPosts() {
        return showLikesInsteadOfPosts;
    }

    public void setShowLikesInsteadOfPosts(boolean showLikesInsteadOfPosts) {
        this.showLikesInsteadOfPosts = showLikesInsteadOfPosts;
    }

    public boolean isShowRandomly() {
        return showRandomly;
    }

    public void setShowRandomly(boolean showRandomly) {
        this.showRandomly = showRandomly;
    }

    public static Random getRandom() {
        return random;
    }

    public static void setRandom(Random random) {
        PictureAlbum.random = random;
    }

    public int getLoadPostsStep() {
        return loadPostsStep;
    }

    public boolean isLoading() {
        return loading;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public int getPostsLimit() {
        return postsLimit;
    }

    public void setPostsLimit(int postsLimit) {
        this.postsLimit = postsLimit;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Picture> getPictureList() {
        return pictureList;
    }

    public void setPictureList(List<Picture> pictureList) {
        this.pictureList = pictureList;
    }

    public void addPicture(Picture picture) {
        this.pictureList.add(picture);
    }

    public int getCurrentMaxPosts() {
        return currentMaxPosts;
    }

    public void setCurrentMaxPosts(int currentMaxPosts) {
        this.currentMaxPosts = currentMaxPosts;
    }

    public void increaseCurrentMaxPosts(int delta) {
        this.currentMaxPosts += delta;
    }

    public int getCurrentPhotoPostCount() {
        return currentPhotoPostCount;
    }

    public void setCurrentPhotoPostCount(int currentPhotoPostCount) {
        this.currentPhotoPostCount = currentPhotoPostCount;
    }

    public void increaseCurrentPhotoPostCount(int delta) {
        this.currentPhotoPostCount += delta;
    }
}
