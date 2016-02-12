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
import com.oleksiykovtun.picsontumblr.android.presenter.PictureAlbumAdapter;
import com.oleksiykovtun.picsontumblr.android.tasks.PictureAlbumFollowTask;
import com.oleksiykovtun.picsontumblr.android.presenter.SessionPresenter;

/**
 * OnMenuItemClickListener for the app's main Toolbar
 */
public class ToolbarMenuItemClickListener implements Toolbar.OnMenuItemClickListener {

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_go_to_post) {
            LayoutInflater inflater = (LayoutInflater) App.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View goToPostMenuLayout =
                    inflater.inflate(R.layout.linear_layout_menu_go_to_post, null);

            AlertDialog dialog = new AlertDialog.Builder(
                    new ContextThemeWrapper(MainActivity.get(), R.style.myDialog))
                    .setTitle("Enter post number:")
                    .setView(goToPostMenuLayout)
                    .setPositiveButton("Go", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.dismiss();
                            openGoToPost(goToPostMenuLayout);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            dialog.cancel();
                        }
                    })
                    .create();
            dialog.getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
            return true;
        } else if (id == R.id.action_go_to_blog) {
            LayoutInflater inflater = (LayoutInflater) App.getContext()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View openBlogMenuLayout =
                    inflater.inflate(R.layout.linear_layout_menu_open_blog, null);

            AlertDialog dialog = new AlertDialog.Builder(
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
            dialog.getWindow()
                    .setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
            dialog.show();
            return true;
        } else if (id == R.id.action_follow) {
            new PictureAlbumFollowTask(true, MainActivity.get().getToolbarTitle()).execute();
        } else if (id == R.id.action_unfollow) {
            new PictureAlbumFollowTask(false, MainActivity.get().getToolbarTitle()).execute();
        } else if (id == R.id.action_more_columns) {
            SessionPresenter.getInstance().increaseColumnCountOnPage();
        } else if (id == R.id.action_less_columns) {
            SessionPresenter.getInstance().decreaseColumnCountOnPage();
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
        // Allowed tags are groups of word characters separated by single hyphens
        String searchTags = ((EditText) searchMenuLayout.findViewById(
                R.id.edit_text_search)).getText().toString()
                .replaceAll("\\W+", "-")
                .replaceAll("^\\W+|\\W+$", "");
        boolean inNewPage = ((CheckBox) searchMenuLayout.findViewById(
                R.id.checkbox_new_page)).isChecked() || SessionPresenter.getInstance().getPageCount() == 0;

        SessionPresenter.getInstance().addPagePresenter(
                new PictureAlbumAdapter(searchTags, false, false, true),
                inNewPage ? SessionPresenter.Position.NEW_PAGE_NEXT : SessionPresenter.Position.ON_TOP);
    }

    private void openNewBlog(View openBlogMenuLayout, boolean likesMode) {
        String blogUrl = "" + ((EditText) openBlogMenuLayout.findViewById(
                R.id.edit_text_search)).getText();
        boolean randomMode = ((CheckBox) openBlogMenuLayout.findViewById(
                R.id.checkbox_randomly)).isChecked();
        boolean inNewPage = ((CheckBox) openBlogMenuLayout.findViewById(
                R.id.checkbox_new_page)).isChecked() || SessionPresenter.getInstance().getPageCount() == 0;

        SessionPresenter.getInstance().addPagePresenter(
                new PictureAlbumAdapter(blogUrl, likesMode, randomMode, false),
                inNewPage ? SessionPresenter.Position.NEW_PAGE_NEXT : SessionPresenter.Position.ON_TOP);
    }

    private void openGoToPost(View goToPostMenuLayout) {
        String blogUrl = "" + SessionPresenter.getInstance().getCurrentPageTitle();
        int targetPostNumber = 0;
        try {
            targetPostNumber = Integer.parseInt(((EditText) goToPostMenuLayout.findViewById(
                    R.id.edit_text_post_number)).getText().toString()) - 1;
        } catch (Throwable e) {
            // TODO process error
        }

        SessionPresenter.getInstance().addPagePresenter(
                new PictureAlbumAdapter(blogUrl, targetPostNumber),
                SessionPresenter.Position.ON_TOP);
    }

}
