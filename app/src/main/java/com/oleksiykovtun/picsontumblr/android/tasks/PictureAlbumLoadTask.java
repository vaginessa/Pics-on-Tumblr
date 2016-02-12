package com.oleksiykovtun.picsontumblr.android.tasks;

import android.util.Log;

import com.oleksiykovtun.picsontumblr.android.model.PictureAlbum;
import com.oleksiykovtun.picsontumblr.android.model.SizedPicture;
import com.oleksiykovtun.picsontumblr.android.manager.AccountManager;
import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.oleksiykovtun.picsontumblr.android.presenter.SessionPresenter;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import com.tumblr.jumblr.types.Post;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * The AsyncTask for loading a blog with pictures
 */
public class PictureAlbumLoadTask extends RepeatableOnErrorAsyncTask {

    private PictureAlbumLoadListener pictureAlbumLoadListener;
    private PictureAlbum pictureAlbum;
    private Blog blog = null;

    public interface PictureAlbumLoadListener {

        void onPictureAlbumPartLoaded(String albumName);
    }

    public PictureAlbumLoadTask(PictureAlbum pictureAlbum,
                                PictureAlbumLoadListener pictureAlbumLoadListener) {
        this.pictureAlbum = pictureAlbum;
        this.pictureAlbumLoadListener = pictureAlbumLoadListener;
    }

    protected void doInBackground() {
        if (!isActual()) {
            cancel(true);
            return;
        }
        if (pictureAlbum.getUrl().isEmpty()) {
            String myBlogName =
                    AccountManager.getAccountClient().user().getBlogs().get(0).getName();
            pictureAlbum.setUrl(myBlogName);
        }
        blog = AccountManager.getAccountClient().blogInfo(pictureAlbum.getUrl());

        int blogItemCount = pictureAlbum.getUrl().equalsIgnoreCase(PictureAlbum.DASHBOARD)
                ? PictureAlbum.DASHBOARD_LIMIT // todo limit if needed
                : pictureAlbum.isShowLikesInsteadOfPosts()
                    ? Math.min(PictureAlbum.LIKES_LIMIT, blog.getLikeCount())
                    : blog.getPostCount();
        int offset = pictureAlbum.isSearch()
                ? 0
                : pictureAlbum.isShowRandomly()
                    ? new Random().nextInt(blogItemCount)
                    : pictureAlbum.getCurrentMaxPosts();
        int limit = pictureAlbum.isSearch()
                ? pictureAlbum.getLoadPostsStep()
                : pictureAlbum.isShowRandomly()
                    ? 2 // todo fix loading and make 1
                    : Math.max(0, Math.min(pictureAlbum.getLoadPostsStep(),
                            pictureAlbum.getPostsLimit() - pictureAlbum.getCurrentMaxPosts()));

        Log.d("PictureAlbumLoadTask", "Picture album " + pictureAlbum.getUrl()
                + ": size = " + blogItemCount + ", offset = " + offset + ", limit = " + limit);

        Map<String, Integer> options = new HashMap<>();
        options.put("limit", limit);
        options.put("reblog_info", 1);
        options.put("offset", offset);

        List<Post> posts;
        if (limit == 0) {
            posts = new ArrayList<>();
        } else if (pictureAlbum.getUrl().equalsIgnoreCase(PictureAlbum.DASHBOARD)) {
            // likes don't apply to the dashboard
            posts = AccountManager.getAccountClient().userDashboard(options);
            // posts limit is set automatically
        } else if (pictureAlbum.isSearch()) {
            posts = AccountManager.getAccountClient().
                    tagged(pictureAlbum.getUrl(), options);
            // no posts limit here, it's search
        } else if (pictureAlbum.isShowLikesInsteadOfPosts()) {
            posts = blog.likedPosts(options);
            pictureAlbum.setPostsLimit(blog.getLikeCount()); // limiting posts to actual likes count
        } else {
            posts = blog.posts(options);
            pictureAlbum.setPostsLimit(blog.getPostCount()); // limiting posts to actual posts count
        }
        pictureAlbum.increaseCurrentMaxPosts(limit);

        for (Post post : posts) {
            if (!isActual()) {
                cancel(true);
                return;
            }
            if (post.getClass().equals(PhotoPost.class)) {
                PhotoPost photoPost = (PhotoPost) post;
                pictureAlbum.increaseCurrentPhotoPostCount(1);
                for (int i = 0; i < photoPost.getPhotos().size(); ++i) {
                    Photo photo = photoPost.getPhotos().get(i);
                    Picture picture = new Picture();
                    picture.setTitle(photoPost.getBlogName());
                    picture.setPostId(photoPost.getId());
                    picture.setReblogKey(photoPost.getReblogKey());
                    picture.setIsLiked(photoPost.isLiked());
                    picture.setTimestamp(photoPost.getTimestamp() * 1000); // from seconds to millis
                    List<SizedPicture> sizedPictures = new ArrayList<>();
                    for (PhotoSize photoSize : photo.getSizes()) {
                        sizedPictures.add(new SizedPicture(photoSize.getUrl(), photoSize.getWidth(),
                                photoSize.getHeight()));
                    }
                    picture.setSizedPictures(sizedPictures);
                    picture.setPostUrl(photoPost.getPostUrl());
                    picture.setNumberInPost(i);

                    // setting blog and source of this picture
                    String rebloggedFromUrl = photoPost.getRebloggedFromName();
                    if (rebloggedFromUrl == null || pictureAlbum.isShowLikesInsteadOfPosts()) {
                        // this means, the picture was originally in the current blog
                        rebloggedFromUrl = photoPost.getBlogName();
                    }
                    picture.setCurrentBlogUrl(rebloggedFromUrl);

                    String sourceUrl = photoPost.getSourceUrl();
                    if (sourceUrl != null) {
                        try {
                            sourceUrl = new URL(sourceUrl).getHost().replace(".tumblr.com", "");
                        } catch (Throwable e) {
                            // the URL will be not formatted in this case
                        }
                    }
                    if (sourceUrl == null) {
                        // this means, the picture was originally in that blog
                        sourceUrl = rebloggedFromUrl;
                    }
                    picture.setOriginalBlogUrl(sourceUrl);

                    picture.setRebloggedFromName(photoPost.getRebloggedFromName());
                    picture.setPostNumber(pictureAlbum.getCurrentPhotoPostCount());

                    pictureAlbum.addPicture(picture);
                }
            }
        }
    }

    protected RepeatableOnErrorAsyncTask getCopy() {
        return new PictureAlbumLoadTask(pictureAlbum, pictureAlbumLoadListener);
    }

    protected String onError() {
        pictureAlbum.setLoading(false);
        return "Error when showing " + pictureAlbum.getUrl();
    }

    protected void onFinished() {
        if (isActual()) {
            String albumName = (blog == null) ? pictureAlbum.getUrl() : blog.getName();
            pictureAlbumLoadListener.onPictureAlbumPartLoaded(albumName);
        }
    }

    private boolean isActual() {
        return SessionPresenter.getInstance() != null && pictureAlbumLoadListener != null
                && SessionPresenter.getInstance().containsPresenterOfModel(pictureAlbum);
    }
}
