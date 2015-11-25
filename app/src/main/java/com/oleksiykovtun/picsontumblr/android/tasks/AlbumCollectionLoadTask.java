package com.oleksiykovtun.picsontumblr.android.tasks;

import com.oleksiykovtun.picsontumblr.android.presenter.AlbumCollectionAdapter;
import com.oleksiykovtun.picsontumblr.android.model.PictureAlbum;
import com.oleksiykovtun.picsontumblr.android.manager.AccountManager;
import com.oleksiykovtun.picsontumblr.android.presenter.SessionPresenter;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.User;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The AsyncTask for loading a blog collection
 */
public class AlbumCollectionLoadTask extends RepeatableOnErrorAsyncTask {

    private AlbumCollectionAdapter followerRecyclerAdapter;
    private int loadingStep = 20;

    public AlbumCollectionLoadTask(AlbumCollectionAdapter followerRecyclerAdapter) {
        this.followerRecyclerAdapter = followerRecyclerAdapter;
    }

    protected void doInBackground() throws Throwable {
        if (!isActual()) {
            cancel(true);
            return;
        }
        Map<String, Integer> options = new HashMap<>();
        options.put("limit", loadingStep);
        options.put("offset",
                followerRecyclerAdapter.getModel().getPictureAlbumList().size());

        if (followerRecyclerAdapter.getModel().isFollowing()) {
            List<Blog> blogs = AccountManager.getAccountClient().userFollowing(options);
            for (Blog blog : blogs) {
                PictureAlbum pictureAlbum = new PictureAlbum(blog.getName());
                if (!followerRecyclerAdapter.getModel().getPictureAlbumList().
                        contains(pictureAlbum)) {
                    followerRecyclerAdapter.getModel().addPictureAlbum(pictureAlbum);
                }
            }

        } else if (followerRecyclerAdapter.getModel().isFollowers()) {
            String myBlogName =
                    AccountManager.getAccountClient().user().getBlogs().get(0).getName();
            List<User> followingUsers =
                    AccountManager.getAccountClient().blogFollowers(myBlogName, options);
            for (User user : followingUsers) {
                PictureAlbum pictureAlbum = new PictureAlbum(user.getName());
                if (!followerRecyclerAdapter.getModel().getPictureAlbumList().
                        contains(pictureAlbum)) {
                    followerRecyclerAdapter.getModel().addPictureAlbum(pictureAlbum);
                }
            }
        }
    }

    protected RepeatableOnErrorAsyncTask getCopy() {
        return new AlbumCollectionLoadTask(followerRecyclerAdapter);
    }

    protected String onError() {
        return "Error when loading followers";
    }

    protected void onFinished() {
        if (isActual()) {
            followerRecyclerAdapter.notifyDataSetChanged();
        }
    }

    private boolean isActual() {
        return SessionPresenter.getInstance() != null && followerRecyclerAdapter != null
                && SessionPresenter.getInstance()
                .containsPresenterOfModel(followerRecyclerAdapter.getModel());
    }
}
