package com.oleksiykovtun.picsontumblr.android.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Blog collection. Can be a set of following blogs, followed blogs, etc.
 */
public class AlbumCollection {

    private static final String FOLLOWING = "following";
    private static final String FOLLOWERS = "followers";

    private String name;
    private List<PictureAlbum> pictureAlbumList = new ArrayList<>();
    private boolean isFollowers;
    private boolean isFollowing;

    public AlbumCollection(String name) {
        this.name = name;
        if (name != null) {
            if (name.toLowerCase().equals(FOLLOWING)) {
                isFollowing = true;
            } else if (name.toLowerCase().equals(FOLLOWERS)) {
                isFollowers = true;
            }
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isFollowers() {
        return isFollowers;
    }

    public boolean isFollowing() {
        return isFollowing;
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

}
