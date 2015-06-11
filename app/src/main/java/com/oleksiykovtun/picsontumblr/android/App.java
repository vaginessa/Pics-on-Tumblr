package com.oleksiykovtun.picsontumblr.android;

import android.app.Application;
import android.content.Context;

/**
 * Context-providing app
 */
public class App extends Application {

    private static Context context;

    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
    }

    public static Context getContext() {
        return context;
    }

}
