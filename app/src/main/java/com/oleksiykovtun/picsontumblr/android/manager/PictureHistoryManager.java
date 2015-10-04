package com.oleksiykovtun.picsontumblr.android.manager;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.oleksiykovtun.picsontumblr.android.model.PictureHistory;

import java.util.HashSet;

/**
 * Loader and saver for PictureHistory
 */
public class PictureHistoryManager {

    private static final String TAG = "PictureHistory";
    private static final String HISTORY = "PictureHistory";

    public static void loadHistory(Activity activity) {
        PictureHistory.setAll(activity.getSharedPreferences("", Context.MODE_PRIVATE).
                getStringSet(HISTORY, new HashSet<String>()));
        Log.d(TAG, "Picture history loaded: " + PictureHistory.getSize() + " pics");
    }

    public static void saveHistory(Activity activity) {
        activity.getSharedPreferences("", Context.MODE_PRIVATE).edit().
                putStringSet(HISTORY, PictureHistory.getAll()).commit();
        Log.d(TAG, "Picture history saved: " + PictureHistory.getSize() + " pics");
    }

}
