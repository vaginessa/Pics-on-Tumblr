package com.oleksiykovtun.picsontumblr.android.adapter;

import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;

import com.oleksiykovtun.picsontumblr.android.App;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;
import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.model.AccountManager;
import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.oleksiykovtun.picsontumblr.android.view.PagerManager;
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

    PictureAlbumAdapter pictureAlbumAdapter;
    private Blog blog = null;

    public PictureAlbumLoadTask(PictureAlbumAdapter pictureAlbumAdapter) {
        this.pictureAlbumAdapter = pictureAlbumAdapter;
    }

    protected String doInBackground(Void... nothing) {
        try {
            if (pictureAlbumAdapter.getPictureAlbumModel().getUrl().isEmpty()) {
                pictureAlbumAdapter.getPictureAlbumModel().
                        setUrl(AccountManager.getAccountClient().user().getBlogs().get(0).getName());
            }
            Log.d("", "More of album: URL " + pictureAlbumAdapter.getPictureAlbumModel().getUrl());
            blog = AccountManager.getAccountClient().blogInfo(pictureAlbumAdapter.
                    getPictureAlbumModel().getUrl());
            Map<String, Integer> options = new HashMap<>();
            int limit;
            if (pictureAlbumAdapter.getPictureAlbumModel().isSearch()) {
                limit = 20;
            } else if (pictureAlbumAdapter.getPictureAlbumModel().isShowRandomly()) {
                limit = 2; // todo fix, should work when 1
            } else {
                int postsLimit = pictureAlbumAdapter.getPictureAlbumModel().getPostsLimit();
                int currentPostsCount =
                        pictureAlbumAdapter.getPictureAlbumModel().getCurrentMaxPosts();
                int postsLoadLimit = pictureAlbumAdapter.getPictureAlbumModel().getLoadPostsStep();
                limit = Math.max(0, Math.min(postsLoadLimit, postsLimit - currentPostsCount));
            }
            options.put("limit", limit);
            int blogItemCount = 0;
            if (! pictureAlbumAdapter.getPictureAlbumModel().getUrl().equals("dashboard")) {
                blogItemCount = pictureAlbumAdapter.getPictureAlbumModel().
                        isShowLikesInsteadOfPosts() ? blog.getLikeCount() : blog.getPostCount();
                Log.d("", "Likes/posts: " + blogItemCount);
            }
            // offset is set when the album is not search results
            if (! pictureAlbumAdapter.getPictureAlbumModel().isSearch()) {
                int offset = pictureAlbumAdapter.getPictureAlbumModel().isShowRandomly() ?
                        new Random().nextInt(blogItemCount) :
                        pictureAlbumAdapter.getPictureAlbumModel().getCurrentMaxPosts();
                options.put("offset", offset);
            }
            List<Post> posts;
            if (limit == 0) {
                // no posts of the limit is 0
                posts = new ArrayList<>();
            } else if (pictureAlbumAdapter.getPictureAlbumModel().getUrl().equals("dashboard")) {
                // likes don't apply to the dashboard
                posts = AccountManager.getAccountClient().userDashboard(options);
            } else if (pictureAlbumAdapter.getPictureAlbumModel().isSearch()) {
                posts = AccountManager.getAccountClient().
                        tagged(pictureAlbumAdapter.getPictureAlbumModel().getUrl(), options);
            } else if (pictureAlbumAdapter.getPictureAlbumModel().isShowLikesInsteadOfPosts()) {
                posts = blog.likedPosts(options);
            } else {
                posts = blog.posts(options);
            }
            pictureAlbumAdapter.getPictureAlbumModel().increaseCurrentMaxPosts(limit);
            for (Post post : posts) {
                if (post.getClass().equals(PhotoPost.class)) {
                    PhotoPost photoPost = (PhotoPost) post;
                    pictureAlbumAdapter.getPictureAlbumModel().increaseCurrentPhotoPostCount(1);
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
                        picture.setCurrentBlogUrl(pictureAlbumAdapter.getPictureAlbumModel().getUrl());
                        picture.setPhotoPost(photoPost);
                        picture.setPostNumber(pictureAlbumAdapter.getPictureAlbumModel().
                                getCurrentPhotoPostCount());
                        Picasso.with(App.getContext()).load(picture.getUrl()).fetch(); // caching
                        pictureAlbumAdapter.getPictureAlbumModel().addPicture(picture);
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
                "Error when showing " + pictureAlbumAdapter.getPictureAlbumModel().getUrl(),
                Snackbar.LENGTH_LONG).setAction("Repeat", new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new PictureAlbumLoadTask(pictureAlbumAdapter).execute();
            }
        }).setActionTextColor(MainActivity.get().getResources().
                getColor(R.color.accent_material_dark));
        snackbar.getView().setBackgroundColor(MainActivity.get().getResources().
                getColor(R.color.teal_dark));
        snackbar.show();
        pictureAlbumAdapter.getPictureAlbumModel().setLoading(false);
        onPostExecute("");
    }

    protected void onPostExecute(String result) {
        if (pictureAlbumAdapter.getPictureAlbumRecyclerView() != null) {
            ((View) pictureAlbumAdapter.getPictureAlbumRecyclerView().getParent()).setTag(
                    blog == null ? pictureAlbumAdapter.getPictureAlbumModel().getUrl() : blog.getName());
            MainActivity.get().hideProgressWheel();
            PagerManager.getPager().onAction();
        }
        pictureAlbumAdapter.notifyDataSetChanged();
    }
}
