package com.oleksiykovtun.picsontumblr.android.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.PopupWindow;

import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.adapter.AlbumCollectionAdapter;
import com.oleksiykovtun.picsontumblr.android.adapter.PictureAlbumAdapter;
import com.oleksiykovtun.picsontumblr.android.adapter.PictureHistoryManager;
import com.oleksiykovtun.picsontumblr.android.model.AccountManager;
import com.oleksiykovtun.picsontumblr.android.model.AlbumCollection;
import com.oleksiykovtun.picsontumblr.android.model.PictureAlbum;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.net.URL;

/**
 * The main activity of the app
 */
public class MainActivity extends AppCompatActivity {

    private static MainActivity thisActivity = null;
    private Toolbar toolbar = null;
    private PopupWindow popupWindow = null;
    private ProgressWheel progressWheel;
    boolean aboutToExit = false;

    public static MainActivity get() {
        return thisActivity;
    }

    public void goBack(boolean closeWholePage) {
        if (popupWindow != null && popupWindow.isShowing()) {
            Log.d("", "showing");
            hidePopupWindow();
        } else if (isDrawerShowing()) {
            closeDrawer();
        } else if (PagerManager.getPager().getPageCount() == 0) {
            finish();
        } else if (closeWholePage) {
            if (PagerManager.getPager().getPageCount() == 1) {
                finish();
            } else {
                PagerManager.getPager().removePage(PagerManager.getPager().getCurrentPageNumber());
            }
        } else {
            if (PagerManager.getPager().getPageCount() == 1 &&
                    PagerManager.getPager().getStackSizeAtPage(0) == 1) {
                if (aboutToExit) {
                    finish();
                } else {
                    Snackbar snackbar = Snackbar.make(MainActivity.get().
                                    findViewById(R.id.dynamic_view_pager),
                            "Press once more to exit", Snackbar.LENGTH_SHORT).
                            setActionTextColor(MainActivity.get().getResources().
                            getColor(R.color.accent_material_dark));
                    snackbar.getView().setBackgroundColor(MainActivity.get().getResources().
                            getColor(R.color.teal_dark));
                    snackbar.show();
                    aboutToExit = true;
                }
            } else {
                PagerManager.getPager().popFromPage(PagerManager.getPager().getCurrentPageNumber());
                aboutToExit = false;
            }
        }
    }

    int windowedOptions;
    int fullscreenOptions;

    private void obtainWindowModeOptions() {
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(
                new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int i) {
                        ((Button) findViewById(R.id.button_fullscreen)).
                                setText(isFullscreen() ? "Exit fullscreen" : "Fullscreen");
                    }
                });
        windowedOptions = getWindow().getDecorView().getSystemUiVisibility();
        fullscreenOptions = windowedOptions;
        if (Build.VERSION.SDK_INT >= 14) {
            fullscreenOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }
        if (Build.VERSION.SDK_INT >= 16) {
            fullscreenOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }
        if (Build.VERSION.SDK_INT >= 19) {
            fullscreenOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }
    }

    public void setFullscreen(boolean fullscreen) {
        getWindow().getDecorView().
                setSystemUiVisibility(fullscreen ? fullscreenOptions : windowedOptions);
    }

    public boolean isFullscreen() {
        return getWindow().getDecorView().getSystemUiVisibility() == fullscreenOptions;
    }

    public void showProgressWheel() {
        progressWheel.spin();
    }

    public void hideProgressWheel() {
        progressWheel.setProgress(0);
    }

    public void setToolbarTitle(String titleText) {
        toolbar.setTitle(titleText);
    }

    public String getToolbarTitle() {
        return "" + ((toolbar != null) ? toolbar.getTitle() : "");
    }

    public boolean isToolbarVisible() {
        Rect rect = new Rect();
        toolbar.getLocalVisibleRect(rect);
        int marginCompensation = rect.bottom;
        return marginCompensation > 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        thisActivity = this;
        setContentView(R.layout.activity_main);
        obtainWindowModeOptions();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new ToolbarMenuItemClickListener());
        toolbar.setNavigationIcon(R.mipmap.ic_menu_white_24dp);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
        });
        toolbar.setTitle(R.string.app_name);

        progressWheel = (ProgressWheel) findViewById(R.id.progress_wheel);
        progressWheel.spin();

        PagerManager.setPager((ActionDynamicViewPager) findViewById(R.id.dynamic_view_pager));

        if (! AccountManager.isClientAuthorized()) {
            AccountManager.startAuthorization();
        } else {
            String albumName =
                    getBlogNameFromIntent().isEmpty() ? "dashboard" : getBlogNameFromIntent();
            loadPictureAlbumInNewPage(albumName, false);
        }

        setDrawerListeners();
        PictureHistoryManager.loadHistory(this);
    }

    private String getBlogNameFromIntent() {
        if (getIntent() != null) {
            try {
                String url = getIntent().getDataString();
                Log.d("", "Intent URL is " + url);
                // todo go to the existing activity
                return new URL(url).getHost();
            } catch (Throwable e) {
                Log.e("", "failed to parse intent URL", e);
            }
        }
        return "";
    }

    public void loadPictureAlbumInNewPage(String url, boolean likesMode) {
        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View albumView = inflater.inflate(R.layout.linear_layout_picture_blog, null);
        final LoadableRecyclerView albumRecyclerView =
                (LoadableRecyclerView) albumView.findViewById(R.id.picture_holder);
        new PictureAlbumAdapter(new PictureAlbum(url).likesMode(likesMode), albumRecyclerView).
                loadMore();
        int currentPageNumber = (PagerManager.getPager().getPageCount() == 0) ? 0 :
                PagerManager.getPager().getCurrentPageNumber();
        PagerManager.getPager().addPage(albumView, currentPageNumber + 1);
        PagerManager.getPager().goToPage(currentPageNumber + 1);
    }

    private void setDrawerListeners() {
        findViewById(R.id.button_fullscreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFullscreen(!isFullscreen());
                closeDrawer();
            }
        });
        findViewById(R.id.button_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // todo implement
                closeDrawer();
            }
        });
        findViewById(R.id.button_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PagerManager.getPager().removeAll();
                AccountManager.revokeAuthorization();
                AccountManager.startAuthorization();
                closeDrawer();
            }
        });
        findViewById(R.id.button_followers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlbumCollectionPopupWindow("Followers");
                closeDrawer();
            }
        });
        findViewById(R.id.button_following).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlbumCollectionPopupWindow("Following");
                closeDrawer();
            }
        });
        findViewById(R.id.button_dashboard).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPictureAlbumInNewPage("dashboard", false);
                closeDrawer();
            }
        });
        findViewById(R.id.button_my_blog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPictureAlbumInNewPage("", false);
                closeDrawer();
            }
        });
        findViewById(R.id.button_my_likes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadPictureAlbumInNewPage("", true);
                closeDrawer();
            }
        });
    }

    public void showAlbumCollectionPopupWindow(String name) {
        LayoutInflater inflater =
                (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View albumCollectionView = inflater.inflate(R.layout.linear_layout_popup_window, null);

        Toolbar toolbar = (Toolbar) albumCollectionView.findViewById(R.id.toolbar_popup);
        toolbar.setTitle(name);

        LoadableRecyclerView loadableRecyclerView =
                (LoadableRecyclerView) albumCollectionView.findViewById(R.id.album_collection_holder);
        new AlbumCollectionAdapter(new AlbumCollection(name), loadableRecyclerView).loadMore();

        // resetting
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        // showing new window
        popupWindow = new PopupWindow(albumCollectionView,
                findViewById(R.id.dynamic_view_pager).getWidth(),
                findViewById(R.id.dynamic_view_pager).getHeight(), true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupWindow.showAtLocation(findViewById(R.id.dynamic_view_pager), Gravity.CENTER, 0, 0);
    }

    public void hidePopupWindow() {
        if (popupWindow != null) {
            popupWindow.dismiss();
        }
        closeDrawer();
    }

    public boolean isDrawerShowing() {
        return ((DrawerLayout) findViewById(R.id.drawer_layout)).
                isDrawerOpen(findViewById(R.id.drawer_linear_layout));
    }

    public void closeDrawer() {
        ((DrawerLayout) findViewById(R.id.drawer_layout)).closeDrawers();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AuthorizationActivity.CODE) {
            if (resultCode == RESULT_OK) {
                AccountManager.finishAuthorization(data.getStringExtra(AccountManager.TAG_VERIFIER));
                return;
            }
        }
        if (PagerManager.getPager().getPageCount() == 0) {
            goBack(true);
        }
    }

    @Override
    public void onBackPressed() {
        goBack(false);
    }

    public void writePreferences(String label, String message) {
        getSharedPreferences("", Context.MODE_PRIVATE).edit().putString(label, message).commit();
    }

    public String readPreferences(String label, String defaultValue) {
        return getSharedPreferences("", Context.MODE_PRIVATE).getString(label, defaultValue);
    }

    @Override
    public void onDestroy() {
        PictureHistoryManager.saveHistory(this);
        super.onDestroy();
    }

}
