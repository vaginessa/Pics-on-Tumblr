package com.oleksiykovtun.picsontumblr.android.adapter;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.oleksiykovtun.picsontumblr.android.App;
import com.oleksiykovtun.picsontumblr.android.model.PictureAlbum;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;
import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.model.AccountManager;
import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.squareup.picasso.Picasso;
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
public class PictureAlbumLoadTask extends AsyncTask<Void, String, String> {

    private PictureAlbumLoadListener pictureAlbumLoadListener;
    private PictureAlbum pictureAlbum;
    private Blog blog = null;

    public interface PictureAlbumLoadListener {

        void onPictureAlbumPartLoaded(String albumName);
    }

    public PictureAlbumLoadTask(PictureAlbum pictureAlbum) {
        this.pictureAlbum = pictureAlbum;
    }

    public void setOnPictureAlbumLoadListener(PictureAlbumLoadListener pictureAlbumLoadListener) {
        this.pictureAlbumLoadListener = pictureAlbumLoadListener;
    }

    protected String doInBackground(Void... nothing) {
        try {
            if (pictureAlbum.getUrl().isEmpty()) {
                pictureAlbum.
                        setUrl(AccountManager.getAccountClient().user().getBlogs().get(0).getName());
            }
            Log.d("", "More of album: URL " + pictureAlbum.getUrl());
            blog = AccountManager.getAccountClient().blogInfo(pictureAlbum.getUrl());
            Map<String, Integer> options = new HashMap<>();
            int limit;
            if (pictureAlbum.isSearch()) {
                limit = 20;
            } else if (pictureAlbum.isShowRandomly()) {
                limit = 2; // todo fix, should work when 1
            } else {
                int postsLimit = pictureAlbum.getPostsLimit();
                int currentPostsCount =
                        pictureAlbum.getCurrentMaxPosts();
                int postsLoadLimit = pictureAlbum.getLoadPostsStep();
                limit = Math.max(0, Math.min(postsLoadLimit, postsLimit - currentPostsCount));
            }
            options.put("limit", limit);
            int blogItemCount = 0;
            if (! pictureAlbum.getUrl().equals("dashboard")) {
                blogItemCount = pictureAlbum.
                        isShowLikesInsteadOfPosts() ? blog.getLikeCount() : blog.getPostCount();
                Log.d("", "Likes/posts: " + blogItemCount);
            }
            // offset is set when the album is not search results
            if (! pictureAlbum.isSearch()) {
                int offset = pictureAlbum.isShowRandomly() ?
                        new Random().nextInt(blogItemCount) :
                        pictureAlbum.getCurrentMaxPosts();
                options.put("offset", offset);
            }
            List<Post> posts;
            if (limit == 0) {
                // no posts of the limit is 0
                posts = new ArrayList<>();
            } else if (pictureAlbum.getUrl().equals("dashboard")) {
                // likes don't apply to the dashboard
                posts = AccountManager.getAccountClient().userDashboard(options);
            } else if (pictureAlbum.isSearch()) {
                posts = AccountManager.getAccountClient().
                        tagged(pictureAlbum.getUrl(), options);
            } else if (pictureAlbum.isShowLikesInsteadOfPosts()) {
                posts = blog.likedPosts(options);
            } else {
                posts = blog.posts(options);
            }
            pictureAlbum.increaseCurrentMaxPosts(limit);
            for (Post post : posts) {
                if (post.getClass().equals(PhotoPost.class)) {
                    PhotoPost photoPost = (PhotoPost) post;
                    pictureAlbum.increaseCurrentPhotoPostCount(1);
                    for (Photo photo : photoPost.getPhotos()) {
                        Picture picture = new Picture();
                        picture.setIsLiked(photoPost.isLiked());
                        picture.setTimestamp(photoPost.getTimestamp());
                        PhotoSize size = photo.getSizes().get(Math.max(0, photo.getSizes().size() - 5));
                        picture.setPhotoSizes(photo.getSizes());
                        picture.setUrl(size.getUrl());
                        picture.setWidth(size.getWidth());
                        picture.setHeight(size.getHeight());
                        picture.setPostUrl(photoPost.getPostUrl());
                        String sourceUrl = "(none)";
                        try {
                            sourceUrl = photoPost.getSourceUrl();
                            sourceUrl = new URL(sourceUrl).getHost();
                        } catch (Throwable e) {
                            Log.e("", "failed to parse source url " + sourceUrl, e);
                        }
                        if (sourceUrl == null) {
                            sourceUrl = "(none)";
                        }
                        picture.setOriginalBlogUrl(sourceUrl);
                        picture.setCurrentBlogUrl(pictureAlbum.getUrl());
                        picture.setPhotoPost(photoPost);
                        picture.setPostNumber(pictureAlbum.
                                getCurrentPhotoPostCount());
                        Picasso.with(App.getContext()).load(picture.getUrl()).fetch(); // caching
                        pictureAlbum.addPicture(picture);
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
                "Error when showing " + pictureAlbum.getUrl(),
                Snackbar.LENGTH_LONG).setAction("Repeat", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PictureAlbumLoadTask(pictureAlbum).execute();
            }
        }).setActionTextColor(MainActivity.get().getResources().
                getColor(R.color.accent_material_dark));
        snackbar.getView().setBackgroundColor(MainActivity.get().getResources().
                getColor(R.color.teal_dark));
        snackbar.show();
        pictureAlbum.setLoading(false);
        onPostExecute("");
    }

    protected void onPostExecute(String result) {
        if (pictureAlbumLoadListener != null) {
            String albumName = (blog == null) ? pictureAlbum.getUrl() : blog.getName();
            pictureAlbumLoadListener.onPictureAlbumPartLoaded(albumName);
        }
    }
}
