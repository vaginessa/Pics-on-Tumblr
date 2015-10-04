package com.oleksiykovtun.picsontumblr.android.presenter;

import com.oleksiykovtun.picsontumblr.android.view.LoadableRecyclerView;

/**
 * Loadable page presenter
 */
public interface LoadablePagePresenter extends PagePresenter {

    @Override
    LoadableRecyclerView getView();

    void loadMore();

}
