package com.oleksiykovtun.picsontumblr.android.presenter;

import android.view.View;

import com.oleksiykovtun.picsontumblr.android.model.ContentItem;

/**
 * General page presenter
 */
public interface PagePresenter {

    ContentItem getModel();

    View getView();

}
