package com.oleksiykovtun.picsontumblr.android.adapter;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.model.AccountManager;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;
import com.tumblr.jumblr.types.Blog;

/**
 * The AsyncTask for following a blog
 */
public class PictureAlbumFollowTask extends AsyncTask<Void, String, String> {

    private String blogUrl;
    private boolean follow;

    public PictureAlbumFollowTask(boolean follow, String blogUrl) {
        this.follow = follow;
        this.blogUrl = blogUrl;
    }

    protected String doInBackground(Void... nothing) {
        try {
            Blog blog = AccountManager.getAccountClient().blogInfo(blogUrl);
            if (follow) {
                blog.follow();
            } else {
                blog.unfollow();
            }
            // todo know is the user follows already
        } catch (Throwable e) {
            Log.e("", "Following/unfollowing failed", e);
            cancel(true);
        }
        return "";
    }

    protected void onCancelled() {
        Snackbar snackbar = Snackbar.make(MainActivity.get().findViewById(R.id.dynamic_view_pager),
                "Error when following/unfollowing",
                Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PictureAlbumFollowTask(follow, blogUrl).execute();
            }
        }).setActionTextColor(MainActivity.get().getResources().
                getColor(R.color.accent_material_dark));
        snackbar.getView().setBackgroundColor(MainActivity.get().getResources().
                getColor(R.color.teal_dark));
        snackbar.show();
    }
}
