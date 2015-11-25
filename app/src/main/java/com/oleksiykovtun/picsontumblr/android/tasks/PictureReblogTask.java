package com.oleksiykovtun.picsontumblr.android.tasks;

import com.oleksiykovtun.picsontumblr.android.presenter.LoadableRecyclerAdapter;
import com.oleksiykovtun.picsontumblr.android.manager.AccountManager;
import com.oleksiykovtun.picsontumblr.android.model.Picture;

/**
 * The AsyncTask for reblogging a post which contains the picture
 */
public class PictureReblogTask extends RepeatableOnErrorAsyncTask {

    private LoadableRecyclerAdapter recyclerAdapter;
    private Picture picture;

    public PictureReblogTask(LoadableRecyclerAdapter recyclerAdapter, Picture picture) {
        this.recyclerAdapter = recyclerAdapter;
        this.picture = picture;
    }

    protected void doInBackground() {
        String myBlogName =
                AccountManager.getAccountClient().user().getBlogs().get(0).getName();
        AccountManager.getAccountClient().
                postReblog(myBlogName + ".tumblr.com", picture.getPostId(), picture.getReblogKey());
        picture.setIsReblogged(true);
        picture.setIsRemoved(false);
        // todo also add all post to my blog view
    }

    protected RepeatableOnErrorAsyncTask getCopy() {
        return new PictureReblogTask(recyclerAdapter, picture);
    }

    protected String onError() {
        return "Error when reblogging";
    }

    protected void onFinished() {
        recyclerAdapter.notifyDataSetChanged();
    }
}
