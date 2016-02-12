package com.oleksiykovtun.picsontumblr.android.presenter;

import android.support.v7.widget.Toolbar;

import com.oleksiykovtun.picsontumblr.android.manager.AlbumHistoryManager;
import com.oleksiykovtun.picsontumblr.android.model.ContentItem;
import com.oleksiykovtun.picsontumblr.android.model.Session;
import com.oleksiykovtun.picsontumblr.android.util.MultiColumnViewUtil;
import com.oleksiykovtun.picsontumblr.android.view.ActionDynamicViewPager;
import com.oleksiykovtun.picsontumblr.android.view.LoadableRecyclerView;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Binds a session to ActionDynamicViewPager view
 */
public class SessionPresenter {

    private static final String TAG = "SessionPresenter";
    private Session session;
    private ActionDynamicViewPager viewPager;
    private Toolbar toolbar;

    private static SessionPresenter instance = null;

    public static SessionPresenter getInstance() {
        return instance;
    }

    private Set<PagePresenter> pagePresenters = new HashSet<>();

    public SessionPresenter(ActionDynamicViewPager viewPager, Toolbar toolbar) {
        this.viewPager = viewPager;
        this.toolbar = toolbar;
        session = new Session();
        instance = this;
    }

    public void finishSession() {
        // todo implement
    }

    public class Position {
        public static final int ON_TOP = 0;
        public static final int NEW_PAGE_NEXT = 1;
        public static final int NEW_PAGE_LAST = 2;
    }

    public class Navigation {
        public static final int STAY = 0;
        public static final int GO = 1;
    }

    public void setToolbarTitle(String title) {
        toolbar.setTitle(title);
    }

    public void addPagePresenter(PagePresenter pagePresenter, int position, int navigation) {
        if (pagePresenter == null) {
            return;
        }
        // overriding page position for first page to add
        if (session.getTabCount() == 0) {
            position = Position.NEW_PAGE_LAST;
        }
        int targetTabNumber = 0;
        if (position == Position.ON_TOP) {
            targetTabNumber = session.getViewingTabNumber();
            session.addContentItemToTab(pagePresenter.getModel(), targetTabNumber);
        } else if (position == Position.NEW_PAGE_NEXT) {
            targetTabNumber = session.getViewingTabNumber() + 1;
            session.addContentItemToNewTab(pagePresenter.getModel(), targetTabNumber);
        } else if (position == Position.NEW_PAGE_LAST) {
            targetTabNumber = session.getTabCount();
            session.addContentItemToNewTab(pagePresenter.getModel(), targetTabNumber);
        }
        if (navigation == Navigation.GO) {
            session.setViewingTabNumber(targetTabNumber);
        }
        pagePresenters.add(pagePresenter);
        if (pagePresenter instanceof LoadablePagePresenter) {
            ((LoadablePagePresenter) pagePresenter).loadMore();
        }
        updateView();
    }

    public void addPagePresenter(PagePresenter pagePresenter, int position) {
        addPagePresenter(pagePresenter, position, Navigation.GO);
    }

    public void addPagePresenter(PagePresenter pagePresenter) {
        addPagePresenter(pagePresenter, Position.NEW_PAGE_LAST, Navigation.GO);
    }

    public void goToPage(int targetPageNumber) {
        session.setViewingTabNumber(targetPageNumber);
        updateView();
    }

    public void onPagesChanged() {
        String currentAlbumName = getCurrentPageTitle();
        setToolbarTitle(currentAlbumName);
        AlbumHistoryManager.markVisited(currentAlbumName);
    }

    public void onPagesScrolled() {
        onPagesChanged();
        if (getCurrentPagePresenterFromView() != null) {
            for (int i = 0; i < session.getTabCount(); ++i) {
                if (session.getTopContentItemOnTab(i) == getCurrentPagePresenterFromView().getModel()) {
                    session.setViewingTabNumber(i);
                    break;
                }
            }
        }
    }

    private void changeColumnCountOnPageBy(int difference) {
        LoadableRecyclerView recyclerView = (LoadableRecyclerView) viewPager.getCurrentView();
        if (recyclerView != null) {
            recyclerView.setColumnCount(recyclerView.getColumnCount() + difference);
            if (MultiColumnViewUtil.allowsRememberingNumberOfColumns()) {
                MultiColumnViewUtil.rememberNumberOfColumns(recyclerView.getColumnCount());
            }
        }
    }

    public void increaseColumnCountOnPage() {
        changeColumnCountOnPageBy(1);
    }

    public void decreaseColumnCountOnPage() {
        changeColumnCountOnPageBy(-1);
    }

    public String getCurrentPageTitle() {
        return getCurrentPagePresenter().getModel().getUrl();
    }

    private PagePresenter getCurrentPagePresenter() {
        for (PagePresenter pagePresenter : pagePresenters) {
            if (pagePresenter.getModel() == session.getViewingContentItem()) {
                return pagePresenter;
            }
        }
        return new PictureAlbumAdapter("!"); // todo change
    }

    public int getPageCount() {
        return session.getTabCount();
    }

    public void closeAllPages() {
        session.removeAllTabs();
        pagePresenters.clear();
        updateView();
    }

    public int getContentItemStackSizeOnPage(int pageNumber) {
        return session.getContentItemCountOnTab(pageNumber);
    }

    public void closeCurrentPage() {
        Stack<ContentItem> removedItems = session.removeTab(session.getViewingTabNumber());
        for (ContentItem contentItem : removedItems) {
            removePresenterOfModel(contentItem);
        }
        updateView();
    }

    public void removeContentItemFromTopOfCurrentPage() {
        ContentItem removedItem = session.removeTopPageFromTab(session.getViewingTabNumber());
        removePresenterOfModel(removedItem);
        updateView();
    }

    private void removePresenterOfModel(ContentItem contentItemToRemovePresenter) {
        for (PagePresenter pagePresenter : pagePresenters) {
            if (pagePresenter.getModel() == contentItemToRemovePresenter) {
                pagePresenters.remove(pagePresenter);
                return;
            }
        }
    }

    public boolean containsPresenterOfModel(ContentItem contentItemToFind) {
        for (PagePresenter pagePresenter : pagePresenters) {
            if (pagePresenter.getModel() == contentItemToFind) {
                return true;
            }
        }
        return false;
    }

    // ViewPager methods

    public void setPageScrollable(boolean isScrollable) {
        viewPager.setScrollable(isScrollable);
    }

    public int getContentWindowHeightFromView() {
        return viewPager.getHeight();
    }

    private PagePresenter getCurrentPagePresenterFromView() {
        for (PagePresenter pagePresenter : pagePresenters) {
            if (pagePresenter.getView() == viewPager.getCurrentView()) {
                return pagePresenter;
            }
        }
        return null;
    }

    private void updateView() {
        // todo optimize, add smooth scrolling
        viewPager.removeAll();
        for (int i = 0; i < session.getTabCount(); ++i) {
            ContentItem topItemOnTab = session.getTopContentItemOnTab(i);
            for (PagePresenter pagePresenter : pagePresenters) {
                if (pagePresenter.getModel() == topItemOnTab) {
                    viewPager.addPage(pagePresenter.getView());
                }
            }
        }
        viewPager.goToPage(session.getViewingTabNumber());
    }
}
