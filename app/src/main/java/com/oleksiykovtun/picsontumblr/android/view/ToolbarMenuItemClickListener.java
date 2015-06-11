package com.oleksiykovtun.picsontumblr.android.view;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;

import com.oleksiykovtun.picsontumblr.android.App;
import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.adapter.PictureAlbumAdapter;
import com.oleksiykovtun.picsontumblr.android.adapter.PictureAlbumFollowTask;
import com.oleksiykovtun.picsontumblr.android.model.PictureAlbum;

/**
 * OnMenuItemClickListener for the app's main Toolbar
 */
public class ToolbarMenuItemClickListener implements Toolbar.OnMenuItemClickListener {

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_custom_blog) {
            LayoutInflater inflater = (LayoutInflater) App.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View openBlogMenuLayout =
                    inflater.inflate(R.layout.linear_layout_menu_open_blog, null);

            AlertDialog commentEditDialog = new AlertDialog.Builder(
                    new ContextThemeWrapper(MainActivity.get(), R.style.myDialog))
                    .setTitle("Enter blog address:")
                    .setView(openBlogMenuLayout)
                    .setPositiveButton("Blog", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            openNewBlog(openBlogMenuLayout, false);
                        }
                    })
                    .setNeutralButton("Likes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            openNewBlog(openBlogMenuLayout, true);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    })
                    .create();
            commentEditDialog.getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            commentEditDialog.show();
            return true;
        } else if (id == R.id.action_follow) {
            new PictureAlbumFollowTask(true, MainActivity.get().getToolbarTitle()).execute();
        } else if (id == R.id.action_unfollow) {
            new PictureAlbumFollowTask(false, MainActivity.get().getToolbarTitle()).execute();
        } else if (id == R.id.action_search) {
            LayoutInflater inflater = (LayoutInflater) App.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View searchMenuLayout =
                    inflater.inflate(R.layout.linear_layout_menu_search, null);

            AlertDialog commentEditDialog = new AlertDialog.Builder(
                    new ContextThemeWrapper(MainActivity.get(), R.style.myDialog))
                    .setTitle("Search tagged:")
                    .setView(searchMenuLayout)
                    .setPositiveButton("Search", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            search(searchMenuLayout);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    })
                    .create();
            commentEditDialog.getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            commentEditDialog.show();
            return true;
        } else if (id == R.id.action_close_page) {
            MainActivity.get().goBack(true);
        }
        return false;
    }

    private void search(View searchMenuLayout) {
        String searchTags = "" + ((EditText) searchMenuLayout.findViewById(
                R.id.edit_text_search)).getText();
        boolean inNewPage = ((CheckBox) searchMenuLayout.findViewById(
                R.id.checkbox_new_page)).isChecked() || PagerManager.getPager().getPageCount() == 0;

        LayoutInflater inflater =
                (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View albumView = inflater.inflate(R.layout.linear_layout_picture_blog, null);
        final LoadableRecyclerView albumRecyclerView =
                (LoadableRecyclerView) albumView.findViewById(R.id.picture_holder);
        new PictureAlbumAdapter(new PictureAlbum(searchTags).searchMode(true), albumRecyclerView).
                loadMore();

        if (inNewPage) {
            int newPagePosition = PagerManager.getPager().getCurrentPageNumber() + 1;
            PagerManager.getPager().addPage(albumView, newPagePosition);
            PagerManager.getPager().goToPage(newPagePosition);
        } else {
            PagerManager.getPager().pushToPage(albumView,
                    PagerManager.getPager().getCurrentPageNumber());
        }
    }

    private void openNewBlog(View openBlogMenuLayout, boolean likesMode) {
        String blogUrl = "" + ((EditText) openBlogMenuLayout.findViewById(
                R.id.edit_text_search)).getText();
        boolean randomMode = ((CheckBox) openBlogMenuLayout.findViewById(
                R.id.checkbox_randomly)).isChecked();
        boolean inNewPage = ((CheckBox) openBlogMenuLayout.findViewById(
                R.id.checkbox_new_page)).isChecked() || PagerManager.getPager().getPageCount() == 0;

        LayoutInflater inflater =
                (LayoutInflater) App.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View albumView = inflater.inflate(R.layout.linear_layout_picture_blog, null);
        final LoadableRecyclerView albumRecyclerView =
                (LoadableRecyclerView) albumView.findViewById(R.id.picture_holder);
        new PictureAlbumAdapter(new PictureAlbum(blogUrl).
                likesMode(likesMode).randomMode(randomMode), albumRecyclerView).loadMore();

        if (inNewPage) {
            int newPagePosition = PagerManager.getPager().getCurrentPageNumber() + 1;
            PagerManager.getPager().addPage(albumView, newPagePosition);
            PagerManager.getPager().goToPage(newPagePosition);
        } else {
            PagerManager.getPager().pushToPage(albumView,
                    PagerManager.getPager().getCurrentPageNumber());
        }
    }


}
