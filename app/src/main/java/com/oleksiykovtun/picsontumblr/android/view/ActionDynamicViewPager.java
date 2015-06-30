package com.oleksiykovtun.picsontumblr.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.oleksiykovtun.android.dynamicviewpager.DynamicViewPager;
import com.oleksiykovtun.picsontumblr.android.R;
import com.oleksiykovtun.picsontumblr.android.adapter.AlbumHistoryManager;

/**
 * DynamicViewPager with action event processing support
 */
public class ActionDynamicViewPager extends DynamicViewPager {

    public ActionDynamicViewPager(Context context) {
        super(context);
    }

    public ActionDynamicViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private boolean scrollable = false;

    public void setScrollable(boolean scrollable) {
        this.scrollable = scrollable;
    }

    @Override
    protected boolean canScroll(View v, boolean checkV, int dx, int x, int y) {
        return scrollable && super.canScroll(v, checkV, dx, x, y);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return scrollable && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return scrollable && super.onInterceptTouchEvent(event); // todo catch crashes
    }

    @Override
    public void onAction() {
        scrollable = true;
        if (PagerManager.getPager().getPageCount() > 0 &&
                PagerManager.getPager().getCurrentView().getTag() != null) {
            String currentAlbumName = "" + PagerManager.getPager().getCurrentView().getTag();
            MainActivity.get().setToolbarTitle(currentAlbumName);
            AlbumHistoryManager.markVisited(currentAlbumName);
        }
        if (PagerManager.getPager().getPageCount() == 0) {
            MainActivity.get().setToolbarTitle(MainActivity.get().getResources().
                    getString(R.string.app_name));
        }
    }

}
