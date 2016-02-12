package com.oleksiykovtun.picsontumblr.android.util;

/**
 * Provides multi column view settings
 */
public class MultiColumnViewUtil {

    private static final String TAG_COLUMN_COUNT = "COLUMN_COUNT";
    private static final int COLUMN_COUNT_DEFAULT = 2;
    private static final String TAG_REMEMBER_COLUMN_COUNT = "REMEMBER_COLUMN_COUNT";
    private static final boolean REMEMBER_COLUMN_COUNT_DEFAULT = true;

    public static void allowRememberingNumberOfColumns(boolean rememberNumberOfColumns) {
        SettingsUtil.writePreferences(TAG_REMEMBER_COLUMN_COUNT, rememberNumberOfColumns);
    }

    public static boolean allowsRememberingNumberOfColumns() {
        return SettingsUtil.readPreferences(TAG_REMEMBER_COLUMN_COUNT,
                REMEMBER_COLUMN_COUNT_DEFAULT);
    }

    public static void rememberNumberOfColumns(int numberOfColumns) {
        SettingsUtil.writePreferences(TAG_COLUMN_COUNT, numberOfColumns);
    }

    public static int getInitialColumnCount() {
        return allowsRememberingNumberOfColumns()
                ? (int) SettingsUtil.readPreferences(TAG_COLUMN_COUNT, COLUMN_COUNT_DEFAULT)
                : COLUMN_COUNT_DEFAULT;
    }

}
