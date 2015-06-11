package com.oleksiykovtun.picsontumblr.android.adapter;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.oleksiykovtun.picsontumblr.android.model.PictureAlbum;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;
import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.model.AccountManager;
import com.tumblr.jumblr.types.Blog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The AsyncTask for loading a blog collection
 */
public class AlbumCollectionLoadTask extends AsyncTask<Void, String, String> {

    private AlbumCollectionAdapter followerRecyclerAdapter;
    private int loadingStep = 25;

    public AlbumCollectionLoadTask(AlbumCollectionAdapter followerRecyclerAdapter) {
        this.followerRecyclerAdapter = followerRecyclerAdapter;
    }

    protected String doInBackground(Void... nothing) {
        try {
            Map<String, Integer> options = new HashMap<>();
            options.put("limit", loadingStep);
            options.put("offset",
                    followerRecyclerAdapter.getAlbumCollectionModel().getPictureAlbumList().size());
            List<Blog> blogs = AccountManager.getAccountClient().userFollowing(options);
            for (Blog blog : blogs) {
                followerRecyclerAdapter.getAlbumCollectionModel().
                        addPictureAlbum(new PictureAlbum(blog.getName()));
            }
        } catch (Throwable e) {
            Log.e("Instantr", "Something was wrong, cancelling load", e);
            cancel(true);
        }
        return "";
    }

    protected void onCancelled() {
        Snackbar snackbar = Snackbar.make(MainActivity.get().findViewById(R.id.dynamic_view_pager),
                "Error when loading followers",
                Snackbar.LENGTH_LONG).setAction("Retry", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlbumCollectionLoadTask(followerRecyclerAdapter).execute();
            }
        }).setActionTextColor(MainActivity.get().getResources().
                getColor(R.color.accent_material_dark));
        snackbar.getView().setBackgroundColor(MainActivity.get().getResources().
                getColor(R.color.teal_dark));
        snackbar.show();
        onPostExecute("");
    }

    protected void onPostExecute(String result) {
        followerRecyclerAdapter.notifyDataSetChanged();
    }
}
