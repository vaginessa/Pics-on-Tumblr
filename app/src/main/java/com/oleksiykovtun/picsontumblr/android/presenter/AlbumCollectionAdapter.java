package com.oleksiykovtun.picsontumblr.android.presenter;

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
import com.oleksiykovtun.picsontumblr.android.tasks.AlbumCollectionLoadTask;
import com.oleksiykovtun.picsontumblr.android.tasks.PictureAlbumLoadTask;
import com.oleksiykovtun.picsontumblr.android.model.AlbumCollection;
import com.oleksiykovtun.picsontumblr.android.model.PictureAlbum;
import com.oleksiykovtun.picsontumblr.android.view.LoadableRecyclerView;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;


/**
 * LoadableRecyclerAdapter for a blog collection
 */
public class AlbumCollectionAdapter extends LoadableRecyclerAdapter implements LoadablePagePresenter {

    private LoadableRecyclerView albumCollectionRecyclerView;
    private AlbumCollection albumCollection;
    private PictureAlbum myAlbumForStats;
    private static boolean batchLoading;
    private boolean statsOfLikesInsteadOfPosts;

    @Override
    public AlbumCollection getModel() {
        return albumCollection;
    }

    public LoadableRecyclerView getView() {
        return albumCollectionRecyclerView;
    }

    public AlbumCollectionAdapter(String albumCollectionName) {
        this.albumCollection = new AlbumCollection(albumCollectionName);
        setDataSet(albumCollection.getPictureAlbumList());

        LayoutInflater inflater =
                (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        albumCollectionRecyclerView =
                (LoadableRecyclerView) inflater.inflate(R.layout.loadable_recycler_view, null);
        albumCollectionRecyclerView.setLoadableRecyclerAdapter(this);

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
        PictureAlbumLoadTask pictureAlbumLoadTask = new PictureAlbumLoadTask(myAlbumForStats,
                new PictureAlbumLoadTask.PictureAlbumLoadListener() {
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

                String text = ((PictureAlbum) dataSet.get(getPosition(viewHolder))).getUrl();
                SessionPresenter.getInstance().addPagePresenter(
                        new PictureAlbumAdapter(text),
                        SessionPresenter.Position.ON_TOP);
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

                followerMenuDialog.findViewById(R.id.button_poster_blog).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                followerMenuDialog.dismiss();
                                SessionPresenter.getInstance().addPagePresenter(
                                        new PictureAlbumAdapter(albumUrl),
                                        SessionPresenter.Position.ON_TOP);
                            }
                        });
                followerMenuDialog.findViewById(R.id.button_poster_blog_new_page).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                followerMenuDialog.dismiss();

                                SessionPresenter.getInstance().addPagePresenter(
                                        new PictureAlbumAdapter(albumUrl),
                                        SessionPresenter.Position.NEW_PAGE_NEXT);
                            }
                        });
                followerMenuDialog.findViewById(R.id.button_poster_blog_background_page).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                followerMenuDialog.dismiss();

                                SessionPresenter.getInstance().addPagePresenter(
                                        new PictureAlbumAdapter(albumUrl),
                                        SessionPresenter.Position.NEW_PAGE_NEXT,
                                        SessionPresenter.Navigation.STAY);
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
