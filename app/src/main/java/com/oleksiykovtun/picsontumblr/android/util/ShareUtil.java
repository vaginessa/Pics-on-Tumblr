package com.oleksiykovtun.picsontumblr.android.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

/**
 * Opens Share/View popups
 */
public class ShareUtil {

    public static void share(Context context, String title, String url) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, url);
        Intent chooserIntent = Intent.createChooser(shareIntent, title);
        chooserIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(chooserIntent);
    }

    public static void viewExternal(Context context, String url) {
        Intent viewIntent = new Intent(Intent.ACTION_VIEW);
        viewIntent.setData(Uri.parse(url));
        viewIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(viewIntent);
    }

}
