package com.oleksiykovtun.picsontumblr.android.adapter;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.oleksiykovtun.picsontumblr.android.App;
import com.oleksiykovtun.picsontumblr.android.R;
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
    private AlbumCollection albumCollectionModel;

    public AlbumCollection getAlbumCollectionModel() {
        return albumCollectionModel;
    }

    public AlbumCollectionAdapter(AlbumCollection albumCollectionModel,
                                  LoadableRecyclerView albumCollectionRecyclerView) {
        super(albumCollectionModel.getPictureAlbumList());
        List dataSet = albumCollectionModel.getPictureAlbumList();
        this.dataSet = dataSet;
        this.albumCollectionModel = albumCollectionModel;
        this.albumCollectionRecyclerView = albumCollectionRecyclerView;
        this.albumCollectionRecyclerView.setLoadableRecyclerAdapter(this);
    }

    @Override
    public void loadMore() {
        new AlbumCollectionLoadTask(this).execute();
    }

    public class ViewHolder extends LoadableRecyclerAdapter.ViewHolder {
        public TextView textView;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.text);
        }

    }

    private int getPosition(ViewHolder viewHolder) {
        return Integer.parseInt("" + viewHolder.textView.getTag());
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, int viewType) {
        final ViewHolder viewHolder = new ViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.view_following_list_item, parent, false));

        viewHolder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity.get().hideFollowersPopupWindow();

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

        viewHolder.textView.setOnLongClickListener(new View.OnLongClickListener() {
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
                                MainActivity.get().hideFollowersPopupWindow();
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
                                MainActivity.get().hideFollowersPopupWindow();
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
        ((ViewHolder) holder).textView.setTag(position);
        ((ViewHolder) holder).textView.setText(((PictureAlbum) dataSet.get(position)).getUrl());
    }

}
