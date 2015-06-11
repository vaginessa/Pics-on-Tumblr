package com.oleksiykovtun.picsontumblr.android.model;

import android.view.View;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Blog collection. Can be a set of following blogs, followed blogs, etc.
 */
public class AlbumCollection {

    private String name;
    private List<PictureAlbum> pictureAlbumList = new ArrayList<>();
    private int currentMaxPosts = 0;
    private boolean showLikesInsteadOfPosts = false;
    private boolean showRandomly = false;
    private static Random random = new Random();
    private int loadPostsStep = 10;
    private boolean loading = true;
    private View pictureBlogView = null;

    public AlbumCollection() {
    }

    public AlbumCollection(String name) {
        this.name = name;
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
        AlbumCollection.random = random;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PictureAlbum> getPictureAlbumList() {
        return pictureAlbumList;
    }

    public void setPictureAlbumList(List<PictureAlbum> pictureAlbumList) {
        this.pictureAlbumList = pictureAlbumList;
    }

    public void addPictureAlbum(PictureAlbum pictureAlbum) {
        this.pictureAlbumList.add(pictureAlbum);
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

    public View getPictureBlogView() {
        return pictureBlogView;
    }

    public void setPictureBlogView(View pictureBlogView) {
        this.pictureBlogView = pictureBlogView;
    }
}
