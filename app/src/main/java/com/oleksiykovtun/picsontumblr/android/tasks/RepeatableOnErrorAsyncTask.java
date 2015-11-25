package com.oleksiykovtun.picsontumblr.android.tasks;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;

/**
 * Universal AsyncTask with automatic on failure repeat feature
 */
abstract class RepeatableOnErrorAsyncTask extends AsyncTask<Void, Void, Void> {

    private boolean applyDelay;
    private static final int DELAY_MILLIS = 4000;


    protected abstract void doInBackground() throws Throwable;

    protected abstract RepeatableOnErrorAsyncTask getCopy();

    protected abstract String onError();

    protected abstract void onFinished();


    public final void execute() {
        if (getStatus() == Status.PENDING) {
            executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {
            Log.wtf("Error", "Task is already being executed");
        }
    }

    @Override
    protected final Void doInBackground(Void... nothing) {
        try {
            if (applyDelay) {
                Thread.sleep(DELAY_MILLIS);
            }
            doInBackground();
        } catch (Throwable e) {
            String errorMessage = onError();
            Log.e("Error", errorMessage, e);
            showError(errorMessage);
            cancel(true);
        }
        return null;
    }

    @Override
    protected final void onPostExecute(Void nothing) {
        onFinished();
    }

    private RepeatableOnErrorAsyncTask withDelay() {
        applyDelay = true;
        return this;
    }

    private void showError(String errorMessage) {
        final RepeatableOnErrorAsyncTask copyToExecuteRepeatedly = getCopy();
        Snackbar snackbar = Snackbar.make(MainActivity.get().findViewById(R.id.dynamic_view_pager),
                errorMessage + " (Retrying...)", Snackbar.LENGTH_LONG)
                .setAction("Stop", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) { }
                }).setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        if (event == DISMISS_EVENT_TIMEOUT || event == DISMISS_EVENT_CONSECUTIVE) {
                            copyToExecuteRepeatedly.withDelay().execute();
                        } else if (event == DISMISS_EVENT_ACTION || event == DISMISS_EVENT_SWIPE) {
                            onFinished();
                        }
                    }

                    @Override
                    public void onShown(Snackbar snackbar) { }
                }).setActionTextColor(MainActivity.get().getResources().
                        getColor(R.color.accent_material_dark));
        snackbar.getView().setBackgroundColor(MainActivity.get().getResources().
                getColor(R.color.teal_dark));
        snackbar.show();
    }
}
