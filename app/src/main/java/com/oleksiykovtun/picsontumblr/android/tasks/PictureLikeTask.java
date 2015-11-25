package com.oleksiykovtun.picsontumblr.android.tasks;

import com.oleksiykovtun.picsontumblr.android.manager.AccountManager;
import com.oleksiykovtun.picsontumblr.android.presenter.LoadableRecyclerAdapter;
import com.oleksiykovtun.picsontumblr.android.model.Picture;

/**
 * The AsyncTask for liking a post which contains the picture
 */
public class PictureLikeTask extends RepeatableOnErrorAsyncTask {

    private LoadableRecyclerAdapter recyclerAdapter;
    private Picture picture;

    public PictureLikeTask(LoadableRecyclerAdapter recyclerAdapter, Picture picture) {
        this.recyclerAdapter = recyclerAdapter;
        this.picture = picture;
    }

    protected void doInBackground() {
        if (picture.isLiked()) {
            AccountManager.getAccountClient().unlike(picture.getPostId(), picture.getReblogKey());
            picture.setIsLiked(false);
        } else {
            AccountManager.getAccountClient().like(picture.getPostId(), picture.getReblogKey());
            picture.setIsLiked(true);
        }
        // todo also add/remove all post from my blog view likes, update in other blogs
    }

    protected RepeatableOnErrorAsyncTask getCopy() {
        return new PictureLikeTask(recyclerAdapter, picture);
    }

    protected String onError() {
        return "Error when liking";
    }

    protected void onFinished() {
        recyclerAdapter.notifyDataSetChanged();
    }
}
