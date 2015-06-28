package com.oleksiykovtun.picsontumblr.android.adapter;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.oleksiykovtun.picsontumblr.android.App;
import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.oleksiykovtun.picsontumblr.android.model.PictureAlbum;
import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.model.PictureHistory;
import com.oleksiykovtun.picsontumblr.android.view.LoadableRecyclerView;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;
import com.oleksiykovtun.picsontumblr.android.view.PagerManager;
import com.oleksiykovtun.picsontumblr.android.view.SwipeGestureProvider;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.List;

import pl.droidsonroids.gif.GifImageView;

/**
 * LoadableRecyclerAdapter for a blog with pictures
 */
public class PictureAlbumAdapter extends LoadableRecyclerAdapter
        implements PictureAlbumLoadTask.PictureAlbumLoadListener {

    private LoadableRecyclerView pictureAlbumRecyclerView;
    private PictureAlbum pictureAlbum;

    public LoadableRecyclerView getPictureAlbumRecyclerView() {
        return pictureAlbumRecyclerView;
    }

    @Override
    public void loadMore() {
        PictureAlbumLoadTask pictureAlbumLoadTask = new PictureAlbumLoadTask(pictureAlbum);
        pictureAlbumLoadTask.setOnPictureAlbumLoadListener(this);
        pictureAlbumLoadTask.execute();
    }

    public PictureAlbumAdapter(PictureAlbum pictureAlbum,
                                  LoadableRecyclerView pictureAlbumRecyclerView) {
        super(pictureAlbum.getPictureList());
        List dataSet = pictureAlbum.getPictureList();
        this.dataSet = dataSet;
        this.pictureAlbum = pictureAlbum;
        this.pictureAlbumRecyclerView = pictureAlbumRecyclerView;
        this.pictureAlbumRecyclerView.setLoadableRecyclerAdapter(this);
        this.pictureAlbumRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                putLastPictureToHistory();
                super.onScrolled(recyclerView, dx, dy);
            }
        });
    }

    private void putLastPictureToHistory() {
        int position = ((LinearLayoutManager) (pictureAlbumRecyclerView.getLayoutManager())).
                findFirstVisibleItemPosition();
        if (position >= 0 && position < dataSet.size()) {
            Picture lastCompletelyVisiblePicture = (Picture) dataSet.get(position);
            PictureHistory.markShown(lastCompletelyVisiblePicture);
        }
    }

    @Override
    public void onPictureAlbumPartLoaded(String albumName) {
        if (getPictureAlbumRecyclerView() != null) {
            notifyDataSetChanged();
            ((View) getPictureAlbumRecyclerView().getParent()).setTag(albumName);
            MainActivity.get().hideProgressWheel();
            PagerManager.getPager().onAction();
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
                LayoutInflater inflater = (LayoutInflater) App.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                final View pictureView = inflater.inflate(R.layout.linear_layout_picture_full, null);
                final LoadableRecyclerView pictureRecyclerView =
                        (LoadableRecyclerView) pictureView.findViewById(R.id.picture_holder);

                new PictureAdapter(picture, pictureRecyclerView);
                PagerManager.getPager().pushToPage(pictureView,
                        PagerManager.getPager().getCurrentPageNumber());
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

                final View albumView = inflater.inflate(R.layout.linear_layout_picture_blog, null);
                final LoadableRecyclerView albumRecyclerView =
                        (LoadableRecyclerView) albumView.findViewById(R.id.picture_holder);
                final View pictureView = inflater.inflate(R.layout.linear_layout_picture_full, null);
                final LoadableRecyclerView pictureRecyclerView =
                        (LoadableRecyclerView) pictureView.findViewById(R.id.picture_holder);

                pictureMenuDialog.findViewById(R.id.button_poster_blog).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                new PictureAlbumAdapter(new PictureAlbum(
                                        picture.getOriginalBlogUrl()), albumRecyclerView).loadMore();
                                PagerManager.getPager().pushToPage(albumView,
                                        PagerManager.getPager().getCurrentPageNumber());
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_poster_blog_new_page).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                int newPagePosition =
                                        PagerManager.getPager().getCurrentPageNumber() + 1;
                                new PictureAlbumAdapter(new PictureAlbum(
                                        picture.getOriginalBlogUrl()), albumRecyclerView).loadMore();
                                PagerManager.getPager().addPage(albumView, newPagePosition);
                                PagerManager.getPager().goToPage(newPagePosition);
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_poster_blog_background_page).
                        setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        pictureMenuDialog.dismiss();
                                        int newPagePosition = PagerManager.getPager().getPageCount();
                                        new PictureAlbumAdapter(new PictureAlbum(
                                                picture.getOriginalBlogUrl()), albumRecyclerView).loadMore();
                                        PagerManager.getPager().addPage(albumView, newPagePosition);
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
                                new PictureAdapter(picture, pictureRecyclerView);
                                PagerManager.getPager().pushToPage(pictureView,
                                        PagerManager.getPager().getCurrentPageNumber());
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_photo_new_page).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                new PictureAdapter(picture, pictureRecyclerView);
                                int newPagePosition =
                                        PagerManager.getPager().getCurrentPageNumber() + 1;
                                PagerManager.getPager().addPage(pictureView, newPagePosition);
                                PagerManager.getPager().goToPage(newPagePosition);
                                SwipeGestureProvider.swipeUp(200);
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_photo_background_page).
                        setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        pictureMenuDialog.dismiss();
                                        new PictureAdapter(picture, pictureRecyclerView);
                                        int newPagePosition = PagerManager.getPager().getPageCount();
                                        PagerManager.getPager().addPage(pictureView, newPagePosition);
                                    }
                                });
                return false;
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(LoadableRecyclerAdapter.ViewHolder holder, int position) {
        final Picture picture = (Picture) (dataSet.get(position));
        String label = new SimpleDateFormat("yyyy-MM-dd HH:mm")
                .format(picture.getTimestamp() * 1000);
        if (PictureHistory.containsShown(picture)) {
            label += "     Viewed";
        }
        if (picture.isLiked()) {
            label += "     Liked";
        }
        if (picture.isReblogged()) {
            label += "     Reblogged from " + picture.getPhotoPost().getRebloggedFromName();
        }
        if (picture.isRemoved()) {
            label += "     Removed";
        }
        ((ViewHolder) holder).timestampTextView.setText(label);
        ((ViewHolder) holder).postNumberTextView.setText("" + picture.getPostNumber());
        ((ViewHolder) holder).imageView.getLayoutParams().height = picture.getHeight();
        ((ViewHolder) holder).imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        Picasso.with(App.getContext()).load(picture.getUrl())
                .into(((ViewHolder) holder).imageView);
        ((ViewHolder) holder).imageView.setTag(position);
    }

}
