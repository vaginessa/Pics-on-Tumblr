package com.oleksiykovtun.picsontumblr.android.presenter;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.oleksiykovtun.picsontumblr.android.App;
import com.oleksiykovtun.picsontumblr.android.loader.PictureAlbumLoadTask;
import com.oleksiykovtun.picsontumblr.android.loader.PictureLikeTask;
import com.oleksiykovtun.picsontumblr.android.loader.PictureReblogTask;
import com.oleksiykovtun.picsontumblr.android.loader.PictureRemoveTask;
import com.oleksiykovtun.picsontumblr.android.manager.PictureLoadManager;
import com.oleksiykovtun.picsontumblr.android.manager.PictureSizeManager;
import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.oleksiykovtun.picsontumblr.android.model.PictureAlbum;
import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.model.PictureHistory;
import com.oleksiykovtun.picsontumblr.android.view.LoadableRecyclerView;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;
import com.oleksiykovtun.picsontumblr.android.view.SwipeGestureProvider;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;

import pl.droidsonroids.gif.GifImageView;

/**
 * LoadableRecyclerAdapter for a blog with pictures
 */
public class PictureAlbumAdapter extends LoadableRecyclerAdapter implements LoadablePagePresenter,
        PictureAlbumLoadTask.PictureAlbumLoadListener {

    private static final int CACHING_DISTANCE_IN_COLUMN = 10;
    private static final int COLUMN_COUNT_DEFAULT = 2;
    private LoadableRecyclerView pictureAlbumRecyclerView;
    private PictureAlbum pictureAlbum;

    @Override
    public PictureAlbum getModel() {
        return pictureAlbum;
    }

    public LoadableRecyclerView getView() {
        return pictureAlbumRecyclerView;
    }

    @Override
    public void loadMore() {
        PictureAlbumLoadTask pictureAlbumLoadTask = new PictureAlbumLoadTask(pictureAlbum);
        pictureAlbumLoadTask.setOnPictureAlbumLoadListener(this);
        pictureAlbumLoadTask.execute();
    }

    public PictureAlbumAdapter(String pictureAlbumName) {
        pictureAlbum = new PictureAlbum(pictureAlbumName);
        continueConstructor();
    }

    public PictureAlbumAdapter(String pictureAlbumName, int postsOffset) {
        pictureAlbum = new PictureAlbum(pictureAlbumName).startingFromPost(postsOffset);
        continueConstructor();
    }

    public PictureAlbumAdapter(String pictureAlbumName, boolean likesMode, boolean randomMode,
                               boolean searchMode) {
        pictureAlbum = new PictureAlbum(pictureAlbumName).likesMode(likesMode)
                .searchMode(searchMode).randomMode(randomMode);
        continueConstructor();
    }

    private void continueConstructor() {
        setDataSet(pictureAlbum.getPictureList());

        LayoutInflater inflater =
                (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pictureAlbumRecyclerView =
                (LoadableRecyclerView) inflater.inflate(R.layout.loadable_recycler_view, null);
        this.pictureAlbumRecyclerView.setLoadableRecyclerAdapter(this);
        this.pictureAlbumRecyclerView.setItemAnimator(null);
        this.pictureAlbumRecyclerView.setColumnCount(COLUMN_COUNT_DEFAULT);
        this.pictureAlbumRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                putLastPictureToHistory();
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        prefetchImagesAfterPosition(0, 10 * pictureAlbumRecyclerView.getColumnCount());
    }

    private void prefetchImagesAfterPosition(int currentPosition, int halfRangePositionsCount) {
        int rangeStartInclusive = currentPosition + 1;
        int rangeEndExclusive = currentPosition + halfRangePositionsCount;
        if (rangeEndExclusive > dataSet.size()) {
            rangeEndExclusive = dataSet.size();
        }
        for (int i = rangeStartInclusive; i < rangeEndExclusive; ++i) {
            Picture picture = (Picture) dataSet.get(i);
            String pictureUrl =
                    PictureSizeManager.getImageUrlForWidth(picture, getDesiredPictureWidth());
            Picasso.with(App.getContext()).load(pictureUrl).fetch();
        }
    }

    private void putLastPictureToHistory() {
        int position = pictureAlbumRecyclerView.getFirstCompletelyVisibleItemPosition();
        if (position >= 0 && position < dataSet.size()) {
            Picture lastCompletelyVisiblePicture = (Picture) dataSet.get(position);
            PictureHistory.markShown(lastCompletelyVisiblePicture);
        }
    }

    @Override
    public void onPictureAlbumPartLoaded(String albumName) {
        if (getView() != null) {
            notifyDataSetChanged();
            MainActivity.get().hideProgressWheel();
            SessionPresenter.getInstance().onPagesChanged();
        }
    }

    public class ViewHolder extends LoadableRecyclerAdapter.ViewHolder {
        public TextView timestampTextView;
        public TextView postNumberTextView;
        public GifImageView imageView;

        public ViewHolder(View view) {
            super(view);
            timestampTextView = (TextView) view.findViewById(R.id.picture_timestamp);
            postNumberTextView = (TextView) view.findViewById(R.id.post_number);
            imageView = (GifImageView) view.findViewById(R.id.picture);
        }

    }

    private int getPosition(ViewHolder viewHolder) {
        return Integer.parseInt("" + viewHolder.imageView.getTag());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_picture_list_item, parent, false));

        viewHolder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Picture picture = (Picture) dataSet.get(getPosition(viewHolder));
                SessionPresenter.getInstance().addPagePresenter(
                        new PictureAdapter(picture, viewHolder.imageView.getDrawable()),
                        SessionPresenter.Position.ON_TOP);
                SwipeGestureProvider.swipeUp(0);
            }
        });

        viewHolder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Picture picture = (Picture) dataSet.get(getPosition(viewHolder));

                LayoutInflater inflater = (LayoutInflater) App.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View pictureMenuLayout =
                        inflater.inflate(R.layout.linear_layout_menu_blog_picture, null);

                TextView originTextView =
                        (TextView) pictureMenuLayout.findViewById(R.id.label_poster_blog);
                originTextView.setText("By " + picture.getOriginalBlogUrl());
                if (!picture.getOriginalBlogUrl().equals(picture.getCurrentBlogUrl())) {
                    originTextView.append(" (in " + picture.getCurrentBlogUrl() + "'s blog)");
                }

                final AlertDialog pictureMenuDialog =
                        new AlertDialog.Builder(new ContextThemeWrapper(
                                MainActivity.get(), R.style.myDialog)).setView(pictureMenuLayout).create();
                pictureMenuDialog.getWindow()
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                pictureMenuDialog.show();
                ((Button) pictureMenuDialog.findViewById(R.id.button_like)).setText(
                        picture.isLiked() ? "Unlike" : "Like");

                pictureMenuDialog.findViewById(R.id.button_poster_blog).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                SessionPresenter.getInstance().addPagePresenter(
                                        new PictureAlbumAdapter(picture.getOriginalBlogUrl()),
                                        SessionPresenter.Position.ON_TOP);
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_poster_blog_new_page).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                SessionPresenter.getInstance().addPagePresenter(
                                        new PictureAlbumAdapter(picture.getOriginalBlogUrl()),
                                        SessionPresenter.Position.NEW_PAGE_NEXT);
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_poster_blog_background_page).
                        setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        pictureMenuDialog.dismiss();
                                        SessionPresenter.getInstance().addPagePresenter(
                                                new PictureAlbumAdapter(picture.getOriginalBlogUrl()),
                                                SessionPresenter.Position.NEW_PAGE_LAST,
                                                SessionPresenter.Navigation.STAY);
                                    }
                                });

                pictureMenuDialog.findViewById(R.id.button_like).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                new PictureLikeTask(PictureAlbumAdapter.this, picture).execute();
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_remove).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                new PictureRemoveTask(PictureAlbumAdapter.this, picture).execute();
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_reblog).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                new PictureReblogTask(PictureAlbumAdapter.this, picture).execute();
                            }
                        });

                pictureMenuDialog.findViewById(R.id.button_photo).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                SessionPresenter.getInstance().addPagePresenter(
                                        new PictureAdapter(picture, viewHolder.imageView.getDrawable()),
                                        SessionPresenter.Position.ON_TOP);
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_photo_new_page).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                SessionPresenter.getInstance().addPagePresenter(
                                        new PictureAdapter(picture,
                                                viewHolder.imageView.getDrawable()),
                                        SessionPresenter.Position.NEW_PAGE_NEXT);
                                SwipeGestureProvider.swipeUp(200);
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_photo_background_page).
                        setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        pictureMenuDialog.dismiss();
                                        SessionPresenter.getInstance().addPagePresenter(
                                                new PictureAdapter(picture,
                                                        viewHolder.imageView.getDrawable()),
                                                SessionPresenter.Position.NEW_PAGE_LAST,
                                                SessionPresenter.Navigation.STAY);
                                    }
                                });
                return false;
            }
        });
        return viewHolder;
    }

    @Override
    public void onViewRecycled (final LoadableRecyclerAdapter.ViewHolder holder) {
        ((ViewHolder) holder).imageView.destroyDrawingCache();
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(final LoadableRecyclerAdapter.ViewHolder holder, final int position) {
        final Picture picture = (Picture) (dataSet.get(position));
        String label = new SimpleDateFormat("yyyy-MM-dd HH:mm")
                .format(picture.getTimestamp());
        if (PictureHistory.containsShown(picture)) {
            label += "     Viewed";
            if (picture.getTimestamp() < pictureAlbum.getLastVisitTime()) {
                label += " before";
            }
        }
        if (picture.isLiked()) {
            label += "     Liked";
        }
        if (picture.isReblogged()) {
            label += "     Reblogged from " + picture.getRebloggedFromName();
        }
        if (picture.isRemoved()) {
            label += "     Removed";
        }
        ((ViewHolder) holder).timestampTextView.setText(label);
        ((ViewHolder) holder).postNumberTextView.setText("" + picture.getPostNumber());

        loadPictureIntoPosition(((ViewHolder) holder).imageView, position);
    }

    private void loadPictureIntoPosition(ImageView imageView, int position) {
        Picture picture = (Picture) (dataSet.get(position));
        imageView.getLayoutParams().height =
                PictureSizeManager.getPlaceholderHeightForWidth(picture, getDesiredPictureWidth());

        PictureLoadManager.loadFromUrl(getPreviewPictureUrl(picture), imageView);

        imageView.setTag(position);
        prefetchImagesAfterPosition(position, 10 * pictureAlbumRecyclerView.getColumnCount());
    }

    private int getDesiredPictureWidth() {
        return (int) (0.92 * pictureAlbumRecyclerView.getColumnWidth());
    }

    private String getPreviewPictureUrl(Picture picture) {
        return PictureSizeManager.getImageUrlForWidth(picture, getDesiredPictureWidth());
    }

}
