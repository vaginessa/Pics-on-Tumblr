package com.oleksiykovtun.picsontumblr.android.tasks;

import com.oleksiykovtun.picsontumblr.android.manager.AccountManager;
import com.tumblr.jumblr.types.Blog;

/**
 * The AsyncTask for following a blog
 */
public class PictureAlbumFollowTask extends RepeatableOnErrorAsyncTask {

    private String blogUrl;
    private boolean follow;

    public PictureAlbumFollowTask(boolean follow, String blogUrl) {
        this.follow = follow;
        this.blogUrl = blogUrl;
    }

    protected void doInBackground() {
        Blog blog = AccountManager.getAccountClient().blogInfo(blogUrl);
        if (follow) {
            blog.follow();
        } else {
            blog.unfollow();
        }
        // todo know is the user follows already
    }

    protected RepeatableOnErrorAsyncTask getCopy() {
        return new PictureAlbumFollowTask(follow, blogUrl);
    }

    protected String onError() {
        return "Error when following/unfollowing";
    }

    protected void onFinished() { }
}
