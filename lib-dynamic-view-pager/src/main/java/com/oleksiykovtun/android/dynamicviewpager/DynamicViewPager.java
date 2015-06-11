package com.oleksiykovtun.android.dynamicviewpager;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

/**
 * ViewPager with dynamic addition and removal of views
 */
public class DynamicViewPager extends ViewPager {

    private DynamicPagerAdapter dynamicPagerAdapter = null;
    private boolean isPageChanged = false;
    private int pageScheduledToRemovalAfterAnimation = -1;

    public DynamicViewPager(Context context) {
        super(context);
        setup();
    }

    public DynamicViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        setup();
    }

    public PagerAdapter getPagerAdapter() {
        return dynamicPagerAdapter;
    }

    public void addPage(View view) {
        dynamicPagerAdapter.addView(view);
        dynamicPagerAdapter.notifyDataSetChanged();
        onAction();
    }

    public void addPage(View view, int position) {
        dynamicPagerAdapter.addView(view, position);
        dynamicPagerAdapter.notifyDataSetChanged();
        onAction();
    }

    public void pushToPage(View view, int position) {
        dynamicPagerAdapter.pushView(view, position);
        dynamicPagerAdapter.notifyDataSetChanged();
        onAction();
    }

    public void popFromPage(int position) {
        if (getStackSizeAtPage(position) < 2) {
            removePage(position);
        } else {
            dynamicPagerAdapter.popView(position);
        }
        dynamicPagerAdapter.notifyDataSetChanged();
        onAction();
    }

    public int getStackSizeAtPage(int position) {
        return dynamicPagerAdapter.getViewStackSizeAt(position);
    }

    public void removePage(int position) {
        int newShowingPageNumber = (getCurrentPageNumber() < getPageCount() - 1) ?
                (getCurrentPageNumber() + 1) : (getCurrentPageNumber() - 1);
        goToPage(newShowingPageNumber);

        if (getPageCount() == 1) {
            removePageNow(position);
        } else {
            pageScheduledToRemovalAfterAnimation = position;
        }
    }

    public void removeAll() {
        while (getPageCount() > 0) {
            dynamicPagerAdapter.removeView(this, 0);
        }
        onAction();
    }

    public int getPageCount() {
        return dynamicPagerAdapter.getCount();
    }

    public int getCurrentPageNumber() {
        return getCurrentItem();
    }

    public View getCurrentView() {
        return dynamicPagerAdapter.getView(getCurrentItem());
    }

    public void goToPage(int position) {
        if (position < 0) {
            position = 0;
        }
        if (position >= getPageCount()) {
            position = getPageCount() - 1;
        }
        if (getPageCount() > 0) {
            setCurrentItem(position);
        }
        onAction();
    }

    public void onAction() {

    }

    private void setup() {
        dynamicPagerAdapter = new DynamicPagerAdapter();
        setAdapter(dynamicPagerAdapter);
        setOnPageChangeListener(new OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {
                isPageChanged = true;
                onAction();
            }

            @Override
            public void onPageSelected(int i) {
                onAction();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:
                        if (isPageChanged) {
                            if (pageScheduledToRemovalAfterAnimation >= 0) {
                                removePageNow(pageScheduledToRemovalAfterAnimation);
                                pageScheduledToRemovalAfterAnimation = -1;
                            }
                            isPageChanged = false;
                        }
                        break;
                    case ViewPager.SCROLL_STATE_DRAGGING:
                        break;
                    case ViewPager.SCROLL_STATE_SETTLING:
                        break;
                }
                onAction();
            }
        });
    }

    private void removePageNow(int positionToDelete) {
        int currentPageNumberBeforePageRemoval = getCurrentPageNumber();

        dynamicPagerAdapter.removeView(this, positionToDelete);

        if (currentPageNumberBeforePageRemoval < positionToDelete) {
            goToPage(currentPageNumberBeforePageRemoval);
        } else {
            goToPage(currentPageNumberBeforePageRemoval - 1);
        }
        onAction();
    }

}
