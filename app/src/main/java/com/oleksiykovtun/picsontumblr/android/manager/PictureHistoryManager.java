package com.oleksiykovtun.picsontumblr.android.manager;

import android.util.Log;

import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.oleksiykovtun.picsontumblr.android.util.SettingsUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * Loader and saver for PictureHistory
 */
public class PictureHistoryManager {

    private static final String TAG = "PictureHistoryManager";
    private static final String TAG_HISTORY = "PictureHistory";

    private static Set<String> shownPictureUrls = new HashSet<>();

    public static void loadHistory() {
        shownPictureUrls.clear();
        shownPictureUrls.addAll(SettingsUtil.readPreferences(TAG_HISTORY));
        Log.d(TAG, "Picture history loaded: " + getShownPictureCount() + " pics");
    }

    public static void saveHistory() {
        SettingsUtil.writePreferences(TAG_HISTORY, shownPictureUrls);
        Log.d(TAG, "Picture history saved: " + getShownPictureCount() + " pics");
    }

    public static void markShown(Picture picture) {
        shownPictureUrls.add(picture.getUrl());
    }

    public static boolean wasShown(Picture picture) {
        return shownPictureUrls.contains(picture.getUrl());
    }

    private static int getShownPictureCount() {
        return shownPictureUrls.size();
    }

}
