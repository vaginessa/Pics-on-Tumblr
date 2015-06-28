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
import com.tumblr.jumblr.types.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The AsyncTask for loading a blog collection
 */
public class AlbumCollectionLoadTask extends AsyncTask<Void, String, String> {

    private AlbumCollectionAdapter followerRecyclerAdapter;
    private int loadingStep = 20;

    public AlbumCollectionLoadTask(AlbumCollectionAdapter followerRecyclerAdapter) {
        this.followerRecyclerAdapter = followerRecyclerAdapter;
    }

    protected String doInBackground(Void... nothing) {
        try {
            Map<String, Integer> options = new HashMap<>();
            options.put("limit", loadingStep);
            options.put("offset",
                    followerRecyclerAdapter.getAlbumCollection().getPictureAlbumList().size());

            if (followerRecyclerAdapter.getAlbumCollection().isFollowing()) {
                List<Blog> blogs = AccountManager.getAccountClient().userFollowing(options);
                for (Blog blog : blogs) {
                    PictureAlbum pictureAlbum = new PictureAlbum(blog.getName());
                    if (!followerRecyclerAdapter.getAlbumCollection().getPictureAlbumList().
                            contains(pictureAlbum)) {
                        followerRecyclerAdapter.getAlbumCollection().addPictureAlbum(pictureAlbum);
                    }
                }

            } else if (followerRecyclerAdapter.getAlbumCollection().isFollowers()) {
                String myBlogName =
                        AccountManager.getAccountClient().user().getBlogs().get(0).getName();
                List<User> followingUsers =
                        AccountManager.getAccountClient().blogFollowers(myBlogName, options);
                for (User user : followingUsers) {
                    PictureAlbum pictureAlbum = new PictureAlbum(user.getName());
                    if (!followerRecyclerAdapter.getAlbumCollection().getPictureAlbumList().
                            contains(pictureAlbum)) {
                        followerRecyclerAdapter.getAlbumCollection().addPictureAlbum(pictureAlbum);
                    }
                }
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
