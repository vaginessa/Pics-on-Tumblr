package com.oleksiykovtun.picsontumblr.android.manager;


import android.os.AsyncTask;

import com.oleksiykovtun.picsontumblr.android.util.SettingsUtil;

/**
 * History of visited blogs
 */
public class AlbumHistoryManager {

    private static final String TIMESTAMP_PREFIX = "TIMESTAMP_";

    private static AsyncTask savingTask = null;

    public static void markVisited(final String pictureAlbumName) {
        // will save settings with a delay to prevent excessive usage
        if (savingTask == null || savingTask.getStatus() == AsyncTask.Status.FINISHED) {
            savingTask = new AsyncTask<Void, Void, Void>() {

                @Override
                protected Void doInBackground(Void... params) {
                    try {
                        Thread.sleep(500);
                        SettingsUtil.writePreferences(TIMESTAMP_PREFIX + pictureAlbumName,
                                System.currentTimeMillis());
                    } catch (Throwable e) { }
                    return null;
                }

                @Override
                protected void onPostExecute(Void result) {
                }
            }.execute();
        }
    }

    public static long getLastVisitTime(String pictureAlbumName) {
        return SettingsUtil.readPreferences(TIMESTAMP_PREFIX + pictureAlbumName, 0);
    }
}
