package com.oleksiykovtun.picsontumblr.android.view;

/**
 * Created by alx on 2015-06-03.
 */
public class PagerManager {

    private static ActionDynamicViewPager viewPager = null;

    public static void setPager(ActionDynamicViewPager viewPager) {
        PagerManager.viewPager = viewPager;
    }

    public static ActionDynamicViewPager getPager() {
        return viewPager;
    }

}
