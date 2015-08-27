package com.oleksiykovtun.picsontumblr.android.adapter.manager;


import android.content.Context;
import android.os.AsyncTask;

import com.oleksiykovtun.picsontumblr.android.App;

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
                        App.getContext().getSharedPreferences("", Context.MODE_PRIVATE).edit().
                                putLong(TIMESTAMP_PREFIX + pictureAlbumName, System.currentTimeMillis()).
                                commit();
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
        return App.getContext().getSharedPreferences("", Context.MODE_PRIVATE).
                getLong(TIMESTAMP_PREFIX + pictureAlbumName, 0);
    }
}
