package com.oleksiykovtun.picsontumblr.android.adapter;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.oleksiykovtun.picsontumblr.android.App;
import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.adapter.loader.AlbumCollectionLoadTask;
import com.oleksiykovtun.picsontumblr.android.adapter.loader.PictureAlbumLoadTask;
import com.oleksiykovtun.picsontumblr.android.model.AlbumCollection;
import com.oleksiykovtun.picsontumblr.android.model.PictureAlbum;
import com.oleksiykovtun.picsontumblr.android.view.LoadableRecyclerView;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;
import com.oleksiykovtun.picsontumblr.android.view.PagerManager;

import java.util.List;

/**
 * LoadableRecyclerAdapter for a blog collection
 */
public class AlbumCollectionAdapter extends LoadableRecyclerAdapter {

    private LoadableRecyclerView albumCollectionRecyclerView;
    private AlbumCollection albumCollection;
    private PictureAlbum myAlbumForStats;
    private static boolean batchLoading;
    private boolean statsOfLikesInsteadOfPosts;

    public AlbumCollection getAlbumCollection() {
        return albumCollection;
    }

    public AlbumCollectionAdapter(AlbumCollection albumCollection,
                                  LoadableRecyclerView albumCollectionRecyclerView) {
        super(albumCollection.getPictureAlbumList());
        List dataSet = albumCollection.getPictureAlbumList();
        this.dataSet = dataSet;
        this.albumCollection = albumCollection;
        this.albumCollectionRecyclerView = albumCollectionRecyclerView;
        this.albumCollectionRecyclerView.setLoadableRecyclerAdapter(this);
        myAlbumForStats = new PictureAlbum("");
    }

    @Override
    public void loadMore() {
        new AlbumCollectionLoadTask(this).execute();
    }

    public static void stopBatchLoading() {
        batchLoading = false;
        Log.d("", "batch loading stopped");
    }

    public void resetStats(boolean statsOfLikesInsteadOfPosts) {
        this.statsOfLikesInsteadOfPosts = statsOfLikesInsteadOfPosts;
        myAlbumForStats = new PictureAlbum("");
        myAlbumForStats = myAlbumForStats.likesMode(statsOfLikesInsteadOfPosts);
    }

    private void updateStatistics() {
        // counting reblogs/likes per blog
        double[] statsCountsPerBlog = new double[albumCollection.getPictureAlbumList().size()];
        for (int i = 0; i < myAlbumForStats.getPictureList().size(); ++i) {
            String pictureOriginName = myAlbumForStats.getPictureList().get(i).getCurrentBlogUrl();
            for (int j = 0; j < albumCollection.getPictureAlbumList().size(); ++j) {
                if (albumCollection.getPictureAlbumList().get(j).getUrl().equals(pictureOriginName)) {
                    ++statsCountsPerBlog[j];
                    break;
                }
            }
        }
        // saving
        for (int j = 0; j < albumCollection.getPictureAlbumList().size(); ++j) {
            if (statsOfLikesInsteadOfPosts) {
                albumCollection.getPictureAlbumList().get(j).
                        setLikesStatsValue(statsCountsPerBlog[j]);
            } else {
                albumCollection.getPictureAlbumList().get(j).
                        setReblogStatsValue(statsCountsPerBlog[j]);
            }
        }
    }

    public void loadStatistics() {
        // will load all "my" blog and count post origins in it
        PictureAlbumLoadTask pictureAlbumLoadTask = new PictureAlbumLoadTask(myAlbumForStats);
        pictureAlbumLoadTask.setOnPictureAlbumLoadListener(new PictureAlbumLoadTask.PictureAlbumLoadListener() {
            @Override
            public void onPictureAlbumPartLoaded(String albumName) {
                // update statistics
                Log.d("", "Statistics: got " + myAlbumForStats.getCurrentMaxPosts() + " of " +
                        myAlbumForStats.getPostsLimit());
                updateStatistics();
                notifyDataSetChanged();

                // load more until end
                if (myAlbumForStats.getCurrentMaxPosts() < myAlbumForStats.getPostsLimit() &&
                        batchLoading) {
                    loadStatistics();
                }
            }
        });
        pictureAlbumLoadTask.execute();
        batchLoading = true;
    }

    public class ViewHolder extends LoadableRecyclerAdapter.ViewHolder {
        public TextView nameTextView;
        public TextView visitTimeTextView;
        public TextView reblogStatsTextView;
        public TextView likesStatsTextView;

        public ViewHolder(View view) {
            super(view);
            nameTextView = (TextView) view.findViewById(R.id.name);
            visitTimeTextView = (TextView) view.findViewById(R.id.visit_time);
            reblogStatsTextView = (TextView) view.findViewById(R.id.reblog_stats);
            likesStatsTextView = (TextView) view.findViewById(R.id.likes_stats);
        }

    }

    private int getPosition(ViewHolder viewHolder) {
        return Integer.parseInt("" + viewHolder.nameTextView.getTag());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_collection_list_item, parent, false));

        viewHolder.nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.get().hidePopupWindow();

                LayoutInflater inflater = (LayoutInflater) App.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final View albumView = inflater.inflate(R.layout.linear_layout_picture_blog, null);
                final LoadableRecyclerView albumRecyclerView =
                        (LoadableRecyclerView) albumView.findViewById(R.id.picture_holder);

                String text = ((PictureAlbum) dataSet.get(getPosition(viewHolder))).getUrl();
                new PictureAlbumAdapter(new PictureAlbum(text), albumRecyclerView).loadMore();
                PagerManager.getPager().pushToPage(albumView,
                        PagerManager.getPager().getCurrentPageNumber());
            }
        });

        viewHolder.nameTextView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final String albumUrl = ((PictureAlbum) dataSet.get(getPosition(viewHolder))).
                        getUrl();
                LayoutInflater inflater = (LayoutInflater) App.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View pictureMenuLayout =
                        inflater.inflate(R.layout.linear_layout_buttons_poster_blog, null);
                ((TextView) pictureMenuLayout.findViewById(R.id.label_poster_blog)).
                        append(" " + albumUrl);
                final AlertDialog followerMenuDialog =
                        new AlertDialog.Builder(new ContextThemeWrapper(MainActivity.get(),
                                R.style.myDialog)).setView(pictureMenuLayout).create();
                followerMenuDialog.getWindow()
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                followerMenuDialog.show();

                final View albumView = inflater.inflate(R.layout.linear_layout_picture_blog, null);
                final LoadableRecyclerView albumRecyclerView =
                        (LoadableRecyclerView) albumView.findViewById(R.id.picture_holder);

                followerMenuDialog.findViewById(R.id.button_poster_blog).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                followerMenuDialog.dismiss();
                                MainActivity.get().hidePopupWindow();
                                new PictureAlbumAdapter(new PictureAlbum(albumUrl),
                                        albumRecyclerView).loadMore();
                                PagerManager.getPager().pushToPage(albumView,
                                        PagerManager.getPager().getCurrentPageNumber());
                            }
                        });
                followerMenuDialog.findViewById(R.id.button_poster_blog_new_page).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                followerMenuDialog.dismiss();
                                MainActivity.get().hidePopupWindow();
                                int newPagePosition = PagerManager.getPager().getCurrentPageNumber() + 1;
                                new PictureAlbumAdapter(new PictureAlbum(albumUrl),
                                        albumRecyclerView).loadMore();
                                PagerManager.getPager().addPage(albumView, newPagePosition);
                                PagerManager.getPager().goToPage(newPagePosition);
                            }
                        });
                followerMenuDialog.findViewById(R.id.button_poster_blog_background_page).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                followerMenuDialog.dismiss();
                                int newPagePosition = PagerManager.getPager().getPageCount();
                                new PictureAlbumAdapter(new PictureAlbum(albumUrl),
                                        albumRecyclerView).loadMore();
                                PagerManager.getPager().addPage(albumView, newPagePosition);
                            }
                        });
                return false;
            }
        });

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LoadableRecyclerAdapter.ViewHolder holder, int position) {
        PictureAlbum pictureAlbum = (PictureAlbum) dataSet.get(position);
                ((ViewHolder) holder).nameTextView.setTag(position);
        ((ViewHolder) holder).nameTextView.setText(pictureAlbum.getUrl());
        if (pictureAlbum.getReblogStatsValue() > 0) {
            ((ViewHolder) holder).reblogStatsTextView.
                    setText((int) pictureAlbum.getReblogStatsValue() + " reblogged");
        } else {
            ((ViewHolder) holder).reblogStatsTextView.setText("");
        }
        if (pictureAlbum.getLikesStatsValue() > 0) {
            ((ViewHolder) holder).likesStatsTextView.
                    setText((int) pictureAlbum.getLikesStatsValue() + " liked");
        } else {
            ((ViewHolder) holder).likesStatsTextView.setText("");
        }
        String visitTimeText = "";
        long millisSinceLastVisit = System.currentTimeMillis() - pictureAlbum.getLastVisitTime();
        long minutesSinceLastVisit = millisSinceLastVisit / 1000 / 60;
        if (minutesSinceLastVisit == 0) {
            visitTimeText = "just now";
        } else {
            long hoursSinceLastVisit = minutesSinceLastVisit / 60;
            if (hoursSinceLastVisit == 0) {
                visitTimeText = minutesSinceLastVisit + " min ago";
            } else {
                long daysSinceLastVisit = hoursSinceLastVisit / 24;
                if (daysSinceLastVisit == 0) {
                    visitTimeText = hoursSinceLastVisit + " hrs ago";
                } else if (daysSinceLastVisit < 10000) { // more is considered never
                    visitTimeText = daysSinceLastVisit + " days ago";
                }
            }
        }
        ((ViewHolder) holder).visitTimeTextView.setText(visitTimeText);
    }

}
