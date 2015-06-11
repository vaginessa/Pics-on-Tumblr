package com.oleksiykovtun.picsontumblr.android.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

import com.oleksiykovtun.picsontumblr.android.adapter.LoadableRecyclerAdapter;

/**
 * The "load more"-supporting RecyclerView
 */
public class LoadableRecyclerView extends RecyclerView {

    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 10;
    private int firstVisibleItem, visibleItemCount, totalItemCount;
    private LoadableRecyclerAdapter loadableRecyclerAdapter;

    public void setLoadableRecyclerAdapter(LoadableRecyclerAdapter loadableRecyclerAdapter) {
        this.loadableRecyclerAdapter = loadableRecyclerAdapter;
        setLayoutManager(new LinearLayoutManager(getContext()));
        setAdapter(loadableRecyclerAdapter);
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        // loading more if needed
        visibleItemCount = getLayoutManager().getChildCount();
        totalItemCount = getLayoutManager().getItemCount();
        firstVisibleItem = ((LinearLayoutManager) (getLayoutManager())).
                findFirstVisibleItemPosition();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount)
                <= (firstVisibleItem + visibleThreshold)) {
            // need to load more
            loadableRecyclerAdapter.loadMore();
            loading = true;
        }
    }

    public LoadableRecyclerView(Context context) {
        super(context);
    }

    public LoadableRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public LoadableRecyclerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
}
