package com.oleksiykovtun.picsontumblr.android.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 * Represents a set of open pages of content
 */
public class Session implements Serializable {

    private List<Stack<ContentItem>> contentItemTabs = new ArrayList<>();
    private int viewingTabNumber;

    public ContentItem getViewingContentItem() {
        return getTopContentItemOnTab(viewingTabNumber);
    }

    public ContentItem getTopContentItemOnTab(int tabNumber) {
        return contentItemTabs.get(tabNumber).peek();
    }

    public int getTabCount() {
        return contentItemTabs.size();
    }

    public int getTotalContentItemCount() {
        int count = 0;
        for (Stack contentItemTab : contentItemTabs) {
            count += contentItemTab.size();
        }
        return count;
    }

    public Stack<ContentItem> removeTab(int tabNumber) {
        Stack<ContentItem> contentItemStackFromTab = contentItemTabs.remove(tabNumber);
        if (viewingTabNumber >= contentItemTabs.size()) {
            --viewingTabNumber;
        }
        return contentItemStackFromTab;
    }

    public void removeAllTabs() {
        contentItemTabs.clear();
        viewingTabNumber = -1;
    }

    public ContentItem removeTopPageFromTab(int tabNumber) {
        ContentItem removedItem = contentItemTabs.get(tabNumber).pop();
        if (contentItemTabs.get(tabNumber).isEmpty()) {
            removeTab(tabNumber);
        }
        return removedItem;
    }

    public int getContentItemCountOnTab(int tabNumber) {
        return contentItemTabs.get(tabNumber).size();
    }

    public void addContentItemToNewTab(ContentItem contentItem, int newTabPosition) {
        Stack<ContentItem> contentItemStack = new Stack<>();
        contentItemStack.push(contentItem);
        contentItemTabs.add(newTabPosition, contentItemStack);
    }

    public void addContentItemToTab(ContentItem contentItem, int tabPosition) {
        contentItemTabs.get(tabPosition).push(contentItem);
    }

    public int getViewingTabNumber() {
        return viewingTabNumber;
    }

    public void setViewingTabNumber(int viewingItemNumber) {
        this.viewingTabNumber = viewingItemNumber;
    }
}
