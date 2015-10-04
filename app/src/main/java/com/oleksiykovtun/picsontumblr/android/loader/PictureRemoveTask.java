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
 * The AsyncTask for removing a post which contains the picture
 */
public class PictureRemoveTask extends AsyncTask<Void, String, String> {

    private LoadableRecyclerAdapter recyclerAdapter;
    private Picture picture;

    public PictureRemoveTask(LoadableRecyclerAdapter recyclerAdapter, Picture picture) {
        this.recyclerAdapter = recyclerAdapter;
        this.picture = picture;
    }

    protected String doInBackground(Void... nothing) {
        try {
            // todo check top notes, if I was there and reblogged then remove
            String myBlogName =
                    AccountManager.getAccountClient().user().getBlogs().get(0).getName();
            AccountManager.getAccountClient().postDelete(myBlogName, picture.getPostId());
            picture.setIsRemoved(true);
            picture.setIsReblogged(false);
            // todo also remove all post from my blog view, and from other views if I was the author
        } catch (Throwable e) {
            Log.e("", "Picture removing failed", e);
            cancel(true);
        }
        return "";
    }

    protected void onCancelled() {
        Snackbar snackbar = Snackbar.make(MainActivity.get().findViewById(R.id.dynamic_view_pager),
                "Error when removing. Is this picture in my blog?",
                Snackbar.LENGTH_LONG).setAction("My blog", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.get().loadPictureAlbumInNewPage("", false);
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
