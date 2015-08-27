package com.oleksiykovtun.picsontumblr.android.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * The LoadableRecyclerView with scroll locking support
 */
public class ScrollLockRecyclerView extends LoadableRecyclerView {
    public ScrollLockRecyclerView(Context context) {
        super(context);
    }

    public ScrollLockRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ScrollLockRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    private boolean isPagingEnabled = true;

    public void setScrollable(boolean scrollable) {
        this.isPagingEnabled = scrollable;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.isPagingEnabled && super.onInterceptTouchEvent(event);
    }

}
