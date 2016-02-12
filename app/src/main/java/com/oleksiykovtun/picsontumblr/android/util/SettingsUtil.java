package com.oleksiykovtun.picsontumblr.android.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.oleksiykovtun.picsontumblr.android.App;

import java.util.HashSet;
import java.util.Set;

/**
 * Reads/writes app's shared preferences
 */
public class SettingsUtil {

    private static SharedPreferences getSharedPreferences() {
        return App.getContext().getSharedPreferences("", Context.MODE_PRIVATE);
    }

    public static void writePreferences(String label, String message) {
        getSharedPreferences().edit().putString(label, message).commit();
    }

    public static String readPreferences(String label, String defaultValue) {
        return getSharedPreferences().getString(label, defaultValue);
    }

    public static void writePreferences(String label, long value) {
        getSharedPreferences().edit().putLong(label, value).commit();
    }

    public static long readPreferences(String label, long defaultValue) {
        return getSharedPreferences().getLong(label, defaultValue);
    }

    public static void writePreferences(String label, boolean value) {
        getSharedPreferences().edit().putBoolean(label, value).commit();
    }

    public static boolean readPreferences(String label, boolean defaultValue) {
        return getSharedPreferences().getBoolean(label, defaultValue);
    }

    public static void writePreferences(String label, Set<String> values) {
        getSharedPreferences().edit().putStringSet(label, values).commit();
    }

    public static Set<String> readPreferences(String label) {
        return getSharedPreferences().getStringSet(label, new HashSet<String>());
    }

    public static void clearAll() {
        getSharedPreferences().edit().clear().commit();
    }

}
