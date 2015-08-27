package com.oleksiykovtun.picsontumblr.android.adapter.loader;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.adapter.LoadableRecyclerAdapter;
import com.oleksiykovtun.picsontumblr.android.model.AccountManager;
import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;

/**
 * The AsyncTask for reblogging a post which contains the picture
 */
public class PictureReblogTask extends AsyncTask<Void, String, String> {

    private LoadableRecyclerAdapter recyclerAdapter;
    private Picture picture;

    public PictureReblogTask(LoadableRecyclerAdapter recyclerAdapter, Picture picture) {
        this.recyclerAdapter = recyclerAdapter;
        this.picture = picture;
    }

    protected String doInBackground(Void... nothing) {
        try {
            picture.reblog(AccountManager.getAccountClient().user().getBlogs().get(0).
                    getName());
            picture.setIsReblogged(true);
            picture.setIsRemoved(false);
            // todo also add all post to my blog view
        } catch (Throwable e) {
            Log.e("", "Picture reblogging failed", e);
            cancel(true);
        }
        return "";
    }

    protected void onCancelled() {
        Snackbar snackbar = Snackbar.make(MainActivity.get().findViewById(R.id.dynamic_view_pager),
                "Error when reblogging",
                Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PictureReblogTask(recyclerAdapter, picture).execute();
            }
        }).setActionTextColor(MainActivity.get().getResources().
                getColor(R.color.accent_material_dark));
        snackbar.getView().setBackgroundColor(MainActivity.get().getResources().
                getColor(R.color.teal_dark));
        snackbar.show();
        onPostExecute("");
    }

    protected void onPostExecute(String result) {
        recyclerAdapter.notifyDataSetChanged();
    }
}
