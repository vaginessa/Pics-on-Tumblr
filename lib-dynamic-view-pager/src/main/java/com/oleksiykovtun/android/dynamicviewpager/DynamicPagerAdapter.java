package com.oleksiykovtun.android.dynamicviewpager;

import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Stack;

/**
 * PagerAdapter incorporated into DynamicPageViewer
 */
class DynamicPagerAdapter extends PagerAdapter {

    // This holds all the currently displayable view backstacks, in order from left to right.
    private ArrayList<Stack<View>> views = new ArrayList<>();

    @Override
    public int getItemPosition(Object object) {
        int index = -1;
        for (int i = 0; i < views.size(); ++i) {
            if (views.get(i).peek() == object) {
                index = i;
                break;
            }
        }
        if (index == -1) {
            return POSITION_NONE;
        } else {
            return index;
        }
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View v = views.get(position).peek();
        container.addView(v);
        return v;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView(views.get(position).peek());
    }

    @Override
    public int getCount () {
        return views.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return (view == object);
    }

    public int addView(View v) {
        return addView(v, views.size());
    }

    public int addView(View v, int position) {
        Stack<View> viewBackStack = new Stack<>();
        viewBackStack.push(v);
        if (position > getCount()) {
            position = getCount();
        }
        views.add(position, viewBackStack);
        return position;
    }

    public int pushView(View v, int position) {
        views.get(position).push(v);
        return position;
    }

    public int popView(int position) {
         views.get(position).pop();
        return position;
    }

    public int removeView(ViewPager pager, int position) {
        pager.setAdapter(null);
        views.remove(position);
        pager.setAdapter(this);

        return position;
    }

    public View getView(int position) {
        return views.get(position).peek();
    }

    public int getViewStackSizeAt(int position) {
        return views.get(position).size();
    }

}
