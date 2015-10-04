package com.oleksiykovtun.picsontumblr.android.loader;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.manager.AccountManager;
import com.oleksiykovtun.picsontumblr.android.presenter.LoadableRecyclerAdapter;
import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;

/**
 * The AsyncTask for liking a post which contains the picture
 */
public class PictureLikeTask extends AsyncTask<Void, String, String> {

    private LoadableRecyclerAdapter recyclerAdapter;
    private Picture picture;

    public PictureLikeTask(LoadableRecyclerAdapter recyclerAdapter, Picture picture) {
        this.recyclerAdapter = recyclerAdapter;
        this.picture = picture;
    }

    protected String doInBackground(Void... nothing) {
        try {
            if (picture.isLiked()) {
                AccountManager.getAccountClient().unlike(picture.getPostId(), picture.getReblogKey());
                picture.setIsLiked(false);
            } else {
                AccountManager.getAccountClient().like(picture.getPostId(), picture.getReblogKey());
                picture.setIsLiked(true);
            }
            // todo also add/remove all post from my blog view likes, update in other blogs
        } catch (Throwable e) {
            Log.e("", "Picture liking failed", e);
            cancel(true);
        }
        return "";
    }

    protected void onCancelled() {
        Snackbar snackbar = Snackbar.make(MainActivity.get().findViewById(R.id.dynamic_view_pager),
                "Error when liking",
                Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PictureLikeTask(recyclerAdapter, picture).execute();
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
