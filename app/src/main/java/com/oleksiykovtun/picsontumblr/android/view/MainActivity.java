package com.oleksiykovtun.picsontumblr.android.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.view.ContextThemeWrapper;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.oleksiykovtun.picsontumblr.android.App;
import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.presenter.AlbumCollectionAdapter;
import com.oleksiykovtun.picsontumblr.android.presenter.PictureAlbumAdapter;
import com.oleksiykovtun.picsontumblr.android.manager.PictureHistoryManager;
import com.oleksiykovtun.picsontumblr.android.manager.AccountManager;
import com.oleksiykovtun.picsontumblr.android.presenter.SessionPresenter;
import com.oleksiykovtun.picsontumblr.android.util.AppDownloadsUtil;
import com.oleksiykovtun.picsontumblr.android.util.MultiColumnViewUtil;
import com.oleksiykovtun.picsontumblr.android.util.SettingsUtil;
import com.pnikosis.materialishprogress.ProgressWheel;

import java.net.URL;

/**
 * The main activity of the app
 */
public class MainActivity extends AppCompatActivity {

    private static MainActivity thisActivity = null;
    private Toolbar toolbar = null;
    private ProgressWheel progressWheel;
    boolean aboutToExit = false;
    private SessionPresenter sessionPresenter;

    public static MainActivity get() {
        return thisActivity;
    }

    public void goBack(boolean closeWholePage) {
        if (isDrawerShowing()) {
            closeDrawer();
        } else if (SessionPresenter.getInstance().getPageCount() == 0) {
            finish();
        } else if (closeWholePage) {
            if (SessionPresenter.getInstance().getPageCount() == 1) {
                finish();
            } else {
                SessionPresenter.getInstance().closeCurrentPage();
            }
        } else {
            if (SessionPresenter.getInstance().getPageCount() == 1 &&
                    SessionPresenter.getInstance().getContentItemStackSizeOnPage(0) == 1) {
                if (aboutToExit) {
                    SessionPresenter.getInstance().closeAllPages();
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
                SessionPresenter.getInstance().removeContentItemFromTopOfCurrentPage();
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
        showProgressWheel();

        sessionPresenter =
                new SessionPresenter((ActionDynamicViewPager) findViewById(R.id.dynamic_view_pager),
                        toolbar);

        if (! AccountManager.isClientAuthorized()) {
            AccountManager.startAuthorization();
        } else {
            String albumName =
                    getBlogNameFromIntent().isEmpty() ? "dashboard" : getBlogNameFromIntent();
            loadPictureAlbumInNewPage(albumName, false);
        }

        setDrawerListeners();
        PictureHistoryManager.loadHistory();
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
        sessionPresenter.addPagePresenter(new PictureAlbumAdapter(url, likesMode, false, false));
    }

    private void openClearHistoryAndSettingsDialog() {
        new AlertDialog.Builder(new ContextThemeWrapper(
                MainActivity.get(), R.style.myDialog)).setTitle("Clear app data")
                .setMessage("History of picture viewings, app's downloads folder and settings " +
                        "will be cleared and you will be logged out. Are you sure?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SettingsUtil.clearAll();
                        AppDownloadsUtil.deleteAll();
                        dialog.dismiss();
                        logout();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create().show();
    }

    private void openSettings() {
        LayoutInflater inflater = (LayoutInflater) App.getContext()
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View settingsLayout = inflater.inflate(R.layout.linear_layout_settings, null);

        final AlertDialog settingsDialog = new AlertDialog.Builder(new ContextThemeWrapper(
                        MainActivity.get(), R.style.myDialog)).setView(settingsLayout)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create();
        settingsDialog.show();
        settingsDialog.findViewById(R.id.button_clear_app_data).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        settingsDialog.dismiss();
                        openClearHistoryAndSettingsDialog();
                    }
                });
        CheckBox checkBoxColumns
                = (CheckBox) settingsDialog.findViewById(R.id.checkbox_columns);
        checkBoxColumns.setChecked(MultiColumnViewUtil.allowsRememberingNumberOfColumns());
        checkBoxColumns.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MultiColumnViewUtil.allowRememberingNumberOfColumns(isChecked);
            }
        });
    }

    private void logout() {
        SessionPresenter.getInstance().closeAllPages();
        AccountManager.revokeAuthorization();
        AccountManager.startAuthorization();
    }

    private void setDrawerListeners() {
        findViewById(R.id.button_settings).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openSettings();
                closeDrawer();
            }
        });
        findViewById(R.id.button_fullscreen).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFullscreen(!isFullscreen());
                closeDrawer();
            }
        });
        findViewById(R.id.button_logout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logout();
                closeDrawer();
            }
        });
        findViewById(R.id.button_followers).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlbumCollection("Followers");
                closeDrawer();
            }
        });
        findViewById(R.id.button_following).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlbumCollection("Following");
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

    public void showAlbumCollection(String name) {
        SessionPresenter.getInstance().addPagePresenter(new AlbumCollectionAdapter(name),
                SessionPresenter.Position.ON_TOP);
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
        if (SessionPresenter.getInstance().getPageCount() == 0) {
            goBack(true);
        }
    }

    @Override
    public void onBackPressed() {
        goBack(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        AlbumCollectionAdapter.stopBatchLoading(); // heavy loading tasks should not continue
    }

    @Override
    public void onDestroy() {
        sessionPresenter.finishSession();
        PictureHistoryManager.saveHistory();
        super.onDestroy();
    }

}
