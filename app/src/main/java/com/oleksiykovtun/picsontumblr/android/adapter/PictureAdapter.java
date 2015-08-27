package com.oleksiykovtun.picsontumblr.android.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.oleksiykovtun.picsontumblr.android.App;
import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.adapter.loader.PictureLikeTask;
import com.oleksiykovtun.picsontumblr.android.adapter.loader.PictureReblogTask;
import com.oleksiykovtun.picsontumblr.android.adapter.loader.PictureRemoveTask;
import com.oleksiykovtun.picsontumblr.android.adapter.manager.PictureLoadManager;
import com.oleksiykovtun.picsontumblr.android.adapter.manager.PictureSizeManager;
import com.oleksiykovtun.picsontumblr.android.model.Picture;
import com.oleksiykovtun.picsontumblr.android.model.PictureAlbum;
import com.oleksiykovtun.picsontumblr.android.view.LoadableRecyclerView;
import com.oleksiykovtun.picsontumblr.android.view.MainActivity;
import com.oleksiykovtun.picsontumblr.android.view.PagerManager;
import com.oleksiykovtun.picsontumblr.android.view.ScrollLockRecyclerView;
import com.oleksiykovtun.picsontumblr.android.view.SwipeGestureProvider;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.List;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase;

/**
 * LoadableRecyclerAdapter for a picture (single item in the list for toolbar hiding support)
 */
public class PictureAdapter extends LoadableRecyclerAdapter {

    private LoadableRecyclerView pictureRecyclerView;
    private Picture pictureModel;
    private Drawable previewDrawable;

    public Picture getPictureModel() {
        return pictureModel;
    }

    public LoadableRecyclerView getPictureRecyclerView() {
        return pictureRecyclerView;
    }

    public PictureAdapter(Picture pictureModel,
                          LoadableRecyclerView pictureRecyclerView, Drawable previewDrawable) {
        super(Arrays.asList(pictureModel));
        List dataSet = Arrays.asList(pictureModel);
        this.dataSet = dataSet;
        this.pictureModel = pictureModel;
        this.previewDrawable = previewDrawable;
        this.pictureRecyclerView = pictureRecyclerView;
        this.pictureRecyclerView.setLoadableRecyclerAdapter(this);
        notifyDataSetChanged();
    }

    public class ViewHolder extends LoadableRecyclerAdapter.ViewHolder {
        public ImageViewTouch imageView;

        public ViewHolder(View view) {
            super(view);
            imageView = (ImageViewTouch) view.findViewById(R.id.picture);
        }

    }

    private int getPosition(ViewHolder viewHolder) {
        return Integer.parseInt("" + viewHolder.imageView.getTag());
    }

    private void setScrollability(ImageViewTouch imageView, int pointerCount) {
        boolean scrollable = (pointerCount < 2) && (imageView.getScale() <= 1);
        ((ScrollLockRecyclerView) imageView.getParent().getParent()).
                setScrollable(scrollable);
        PagerManager.getPager().setScrollable(scrollable);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_picture_single, parent, false));

        viewHolder.imageView.getLayoutParams().height = PagerManager.getPager().getHeight();
        viewHolder.imageView.requestLayout();

        viewHolder.imageView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, final MotionEvent event) {
                setScrollability(viewHolder.imageView, event.getPointerCount());
                // also redoing after animation ends
                new AsyncTask<Void, Void, Void>() {

                    @Override
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(300);
                        } catch (Exception e) {
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        setScrollability(viewHolder.imageView, event.getPointerCount());
                    }
                }.execute();
                return false;
            }
        });

        viewHolder.imageView.setSingleTapListener(new ImageViewTouch.
                OnImageViewTouchSingleTapListener() {
            @Override
            public void onSingleTapConfirmed() {
                if (MainActivity.get().isToolbarVisible() && viewHolder.imageView.getScale() <= 1) {
                    SwipeGestureProvider.swipeUp(0);
                }
            }
        });

        viewHolder.imageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                final Picture picture = (Picture) dataSet.get(getPosition(viewHolder));
                LayoutInflater inflater = (LayoutInflater) App.getContext()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View pictureMenuLayout = inflater.inflate(R.layout.linear_layout_menu_picture, null);
                ((TextView) pictureMenuLayout.findViewById(R.id.label_poster_blog)).append(
                        " " + picture.getOriginalBlogUrl());
                final AlertDialog pictureMenuDialog = new AlertDialog.Builder(new ContextThemeWrapper(
                        MainActivity.get(), R.style.myDialog)).setView(pictureMenuLayout).create();
                pictureMenuDialog.getWindow()
                        .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
                pictureMenuDialog.show();
                ((Button) pictureMenuDialog.findViewById(R.id.button_like)).setText(
                        picture.isLiked() ? "Unlike" : "Like");

                final View albumView = inflater.inflate(R.layout.linear_layout_picture_full, null);
                final LoadableRecyclerView albumRecyclerView =
                        (LoadableRecyclerView) albumView.findViewById(R.id.picture_holder);

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
                pictureMenuDialog.findViewById(R.id.button_poster_blog_background_page).setOnClickListener(
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
                                new PictureLikeTask(PictureAdapter.this, picture).execute();
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_remove).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                new PictureRemoveTask(PictureAdapter.this, picture).execute();
                            }
                        });
                pictureMenuDialog.findViewById(R.id.button_reblog).setOnClickListener(
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                pictureMenuDialog.dismiss();
                                new PictureReblogTask(PictureAdapter.this, picture).execute();
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
    public void onBindViewHolder(LoadableRecyclerAdapter.ViewHolder holder, int position) {
        ((ViewHolder) holder).imageView.setTag(position);
        loadPictureIntoPosition(((ViewHolder) holder).imageView, position);
    }

    private void loadPictureIntoPosition(ImageViewTouch imageView, int position) {
        Picture picture = (Picture) (dataSet.get(position));
        imageView.setDisplayType(ImageViewTouchBase.DisplayType.FIT_TO_SCREEN);

        PictureLoadManager.loadFromUrl(getPictureUrl(picture), imageView, previewDrawable);

        imageView.setTag(position);
    }

    private int getDesiredPictureWidth() {
        return 2 * pictureRecyclerView.getColumnWidth();
    }

    private String getPictureUrl(Picture picture) {
        return PictureSizeManager.getImageUrlForWidth(picture, getDesiredPictureWidth());
    }

}
