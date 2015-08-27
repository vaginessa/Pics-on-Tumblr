package com.oleksiykovtun.picsontumblr.android.view;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.AttributeSet;

import com.oleksiykovtun.picsontumblr.android.adapter.LoadableRecyclerAdapter;

import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Collections;

/**
 * The "load more"-supporting RecyclerView
 */
public class LoadableRecyclerView extends RecyclerView {

    private static final int ONE_COLUMN = 1;
    private static final int MAX_COLUMNS = 4;
    private static final int COLUMN_COUNT_DEFAULT = 1;

    private int previousTotal = 0;
    private boolean loading = true;
    private static final int COLUMN_LOADING_RANGE = 20;
    private int visibleItemCount, totalItemCount;
    private LoadableRecyclerAdapter loadableRecyclerAdapter;

    public void setLoadableRecyclerAdapter(LoadableRecyclerAdapter loadableRecyclerAdapter) {
        this.loadableRecyclerAdapter = loadableRecyclerAdapter;
        setAdapter(loadableRecyclerAdapter);
        setColumnCount(COLUMN_COUNT_DEFAULT);
    }

    public void setColumnCount(int newColumnCount) {
        if (newColumnCount >= ONE_COLUMN && newColumnCount <= MAX_COLUMNS) {
            StaggeredGridLayoutManager layoutManager =
                    (StaggeredGridLayoutManager) getLayoutManager();
            if (layoutManager != null) {
                layoutManager.setSpanCount(newColumnCount);
                loadableRecyclerAdapter.notifyDataSetChanged();
            } else {
                layoutManager = new StaggeredGridLayoutManager(newColumnCount,
                        StaggeredGridLayoutManager.VERTICAL);
            }
            setLayoutManager(layoutManager);
        }
    }

    public int getColumnWidth() {
        return getWidth() / getColumnCount();
    }

    public int getColumnCount() {
        if (getLayoutManager() != null && getLayoutManager() instanceof StaggeredGridLayoutManager) {
            int spanCount = ((StaggeredGridLayoutManager) getLayoutManager()).getSpanCount();
            if (spanCount >= ONE_COLUMN) {
                return spanCount;
            } else {
                return ONE_COLUMN;
            }
        } else {
            return ONE_COLUMN;
        }
    }

    public int getFirstCompletelyVisibleItemPosition() {
        int[] firstCompletelyVisibleItemPositions = ((StaggeredGridLayoutManager)
                (getLayoutManager())).findFirstCompletelyVisibleItemPositions(null);
        return Collections.min(Arrays.asList(ArrayUtils.toObject(firstCompletelyVisibleItemPositions)));
    }

    @Override
    public void onScrolled(int dx, int dy) {
        super.onScrolled(dx, dy);
        // loading more if needed
        visibleItemCount = getLayoutManager().getChildCount();
        totalItemCount = getLayoutManager().getItemCount();

        if (loading) {
            if (totalItemCount > previousTotal) {
                loading = false;
                previousTotal = totalItemCount;
            }
        }
        if (!loading && (totalItemCount - visibleItemCount)
                <= (getFirstCompletelyVisibleItemPosition() + COLUMN_LOADING_RANGE * getColumnCount())) {
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
