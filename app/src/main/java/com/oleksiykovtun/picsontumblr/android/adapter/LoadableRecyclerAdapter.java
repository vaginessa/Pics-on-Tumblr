package com.oleksiykovtun.picsontumblr.android.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * A versatile adapter for RecyclerView with "load more" feature
 */
public abstract class LoadableRecyclerAdapter extends RecyclerView
        .Adapter<LoadableRecyclerAdapter.ViewHolder> {

    protected CoolClickListener itemClickListener;

    protected List<Object> dataSet;

    public interface CoolClickListener {

        void onClick(Object residingObject);

        void onClick(Object residingObject, View view);

        void onLongClick(Object residingObject);

    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            View.OnLongClickListener {

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            view.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (itemClickListener != null && dataSet != null && dataSet.size() > getPosition()) {
                itemClickListener.onClick(dataSet.get(getPosition()));
                itemClickListener.onClick(dataSet.get(getPosition()), v);
            }
        }

        @Override
        public boolean onLongClick(View v) {
            if (itemClickListener != null && dataSet != null && dataSet.size() > getPosition()) {
                itemClickListener.onLongClick(dataSet.get(getPosition()));
            }
            return true;
        }
    }

    public LoadableRecyclerAdapter(List dataSet) {
        this.dataSet = dataSet;
    }

    @Override
    public int getItemCount() {
        return (dataSet != null) ? dataSet.size() : 0;
    }

    public void setOnItemClickListener(CoolClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public void loadMore() {

    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

    }
}
