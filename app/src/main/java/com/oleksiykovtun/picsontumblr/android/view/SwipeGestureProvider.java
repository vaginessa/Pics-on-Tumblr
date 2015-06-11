package com.oleksiykovtun.picsontumblr.android.view;

import android.app.Instrumentation;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.view.MotionEvent;

/**
 * Programmatic swipe gesture provider
 */
public class SwipeGestureProvider {

    public static void swipeDown(long delayMillis) {
        swipe(delayMillis, 200);
    }

    public static void swipeUp(long delayMillis) {
        swipe(delayMillis, -200);
    }

    private static void swipe(final long delayMillis, final int distanceY) {
        new AsyncTask<String, String, String>() {

            @Override
            protected String doInBackground(String... params) {
                try {
                    Thread.sleep(delayMillis);
                } catch (Throwable e) { }
                Instrumentation mInst = new Instrumentation();

                mInst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 200, 300, 0));
                mInst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis() + 200, MotionEvent.ACTION_MOVE, 0, distanceY, 0));
                mInst.sendPointerSync(MotionEvent.obtain(SystemClock.uptimeMillis(),
                        SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 200, 300 + distanceY, 0));
                return null;
            }
        }.execute();
    }

}
