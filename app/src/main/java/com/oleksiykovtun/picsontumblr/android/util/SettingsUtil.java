package com.oleksiykovtun.picsontumblr.android.util;

import android.content.Context;

import com.oleksiykovtun.picsontumblr.android.App;

/**
 * Reads/writes app's shared preferences
 */
public class SettingsUtil {

    public static void writePreferences(String label, String message) {
        App.getContext().getSharedPreferences("", Context.MODE_PRIVATE).edit()
                .putString(label, message).commit();
    }

    public static String readPreferences(String label, String defaultValue) {
        return App.getContext().getSharedPreferences("", Context.MODE_PRIVATE)
                .getString(label, defaultValue);
    }

}
