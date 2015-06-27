package com.oleksiykovtun.picsontumblr.android.model;


import java.util.Set;
import java.util.HashSet;

/**
 * History of already shown picture with unique URLs
 */
public class PictureHistory {

    private static Set<String> shownPictureUrls = new HashSet<>();

    public static void markShown(Picture picture) {
        shownPictureUrls.add(picture.getUrl());
    }

    public static boolean containsShown(Picture picture) {
        return shownPictureUrls.contains(picture.getUrl());
    }

    public static void setAll(Set<String> shownPictureUrls) {
        PictureHistory.shownPictureUrls.clear();
        PictureHistory.shownPictureUrls.addAll(shownPictureUrls);
    }

    public static Set<String> getAll() {
        return shownPictureUrls;
    }

    public static void clear() {
        shownPictureUrls.clear();
    }

    public static int getSize() {
        return shownPictureUrls.size();
    }

}
