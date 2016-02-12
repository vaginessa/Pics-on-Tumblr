package com.oleksiykovtun.picsontumblr.android.util;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

/**
 * Manages downloads of this app (in a dedicated folder)
 */
public class AppDownloadsUtil {

    private static final String DOWNLOADS_DIRECTORY = "/Pics-on-Tumblr";

    private static File getAppDownloadsDirectory() {
        return new File(Environment.getExternalStorageDirectory() + DOWNLOADS_DIRECTORY + "/");
    }

    public static void addImage(final Context context, final String url, final String fileName) {
        if (!getAppDownloadsDirectory().exists()) {
            getAppDownloadsDirectory().mkdirs();
        }

        DownloadManager downloadManager
                = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setAllowedNetworkTypes(
                DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                .setAllowedOverRoaming(true)
                .setTitle("Pics-on-Tumblr")
                .setDescription("Pics-on-Tumblr is saving picture to your device")
                .setDestinationInExternalPublicDir(DOWNLOADS_DIRECTORY, fileName)
                .setVisibleInDownloadsUi(false)
                .setNotificationVisibility(DownloadManager.Request
                        .VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        downloadManager.enqueue(request);
    }

    public static void deleteAll() {
        if (getAppDownloadsDirectory().exists()) {
            for (String imagePath : getAppDownloadsDirectory().list()) {
                new File(getAppDownloadsDirectory(), imagePath).delete();
            }
        }
        getAppDownloadsDirectory().delete();
    }

}
