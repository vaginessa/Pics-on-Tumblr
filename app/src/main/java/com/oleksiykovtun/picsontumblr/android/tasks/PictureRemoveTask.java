package com.oleksiykovtun.picsontumblr.android.tasks;

import com.oleksiykovtun.picsontumblr.android.manager.AccountManager;
import com.oleksiykovtun.picsontumblr.android.presenter.LoadableRecyclerAdapter;
import com.oleksiykovtun.picsontumblr.android.model.Picture;

/**
 * The AsyncTask for removing a post which contains the picture
 */
public class PictureRemoveTask extends RepeatableOnErrorAsyncTask {

    private LoadableRecyclerAdapter recyclerAdapter;
    private Picture picture;

    public PictureRemoveTask(LoadableRecyclerAdapter recyclerAdapter, Picture picture) {
        this.recyclerAdapter = recyclerAdapter;
        this.picture = picture;
    }

    protected void doInBackground() {
        // todo check top notes, if I was there and reblogged then remove
        String myBlogName =
                AccountManager.getAccountClient().user().getBlogs().get(0).getName();
        AccountManager.getAccountClient().postDelete(myBlogName, picture.getPostId());
        picture.setIsRemoved(true);
        picture.setIsReblogged(false);
        // todo also remove all post from my blog view, and from other views if I was the author
    }

    protected RepeatableOnErrorAsyncTask getCopy() {
        return new PictureRemoveTask(recyclerAdapter, picture);
    }

    protected String onError() {
        return "Error when removing. Check if the current blog is yours";
    }

    protected void onFinished() {
        recyclerAdapter.notifyDataSetChanged();
    }
}
