/*******************************************************************************
 * Copyright 2014 Handsome LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package is.handsome.ptr_sticky_listview.views;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import is.handsome.ptr_sticky_listview.interfaces.IListOverscrollListener;
import is.handsome.ptr_sticky_listview.interfaces.IStickyHeaderAdapter;
import is.handsome.sticky_list_header.R;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by Egor Vorotnikov.
 * email:egorv@handsome.is
 */
public class StickyHeaderListView extends RelativeLayout {

    private static final String TAG = "StickySecondHeaderListView";

    private boolean mAlreadyInflated = false;

    private View mHeaderView;
    private View mStickyHeaderView;

    private PTRListView mListView;

    private IStickyHeaderAdapter mAdapter;

    private boolean mViewStatesSynced = false;
    private int mStickyHeaderPosition = -1;

    private ArrayList<WeakReference<IListOverscrollListener>> mOverScrollDelegates = new ArrayList<WeakReference<IListOverscrollListener>>();

    private AbsListView.OnScrollListener mOnScrollListener = new AbsListView.OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {
        }

        @Override
        public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            if (mStickyHeaderView != null) {
                if (i < mStickyHeaderPosition) {
                    if (mStickyHeaderView.getVisibility() == VISIBLE) {
                        mViewStatesSynced = false;
                    }
                    mStickyHeaderView.setVisibility(View.GONE);
                    mStickyHeaderView.requestLayout();
                    if (mAdapter != null && !mViewStatesSynced) {
                        mAdapter.onStickyViewDisappeared(mHeaderView, mStickyHeaderView);
                        mViewStatesSynced = true;
                    }
                } else {
                    if (mStickyHeaderView.getVisibility() == GONE) {
                        mViewStatesSynced = false;
                    }
                    mStickyHeaderView.setVisibility(View.VISIBLE);
                    mStickyHeaderView.requestLayout();
                    if (mAdapter != null && !mViewStatesSynced) {
                        mAdapter.onStickyViewAppeared(mHeaderView, mStickyHeaderView);
                        mViewStatesSynced = true;
                    }
                }
            }
        }
    };

    private IListOverscrollListener mListOverscrollListener = new IListOverscrollListener() {
        @Override
        public boolean onListOverscroll(int deltaY) {
            if (mOverScrollDelegates.size() > 0) {
                for (WeakReference<IListOverscrollListener> weakRef : mOverScrollDelegates) {
                    try {
                        return weakRef.get().onListOverscroll(deltaY);
                    } catch (NullPointerException e) {
                        Log.e(TAG, "Dead overscoll listener", e);
                    }
                }
            }
            return true;
        }

        @Override
        public void onTouchUp() {
            if (mOverScrollDelegates.size() > 0) {
                for (WeakReference<IListOverscrollListener> weakRef : mOverScrollDelegates) {
                    try {
                        weakRef.get().onTouchUp();
                    } catch (NullPointerException e) {
                        Log.e(TAG, "Dead overscoll listener", e);
                    }
                }
            }
        }
    };


    public StickyHeaderListView(Context context) {
        super(context);
    }

    public StickyHeaderListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StickyHeaderListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        if (!mAlreadyInflated) {
            inflate(getContext(), R.layout.v_sticky_list_view, this);
            initViews();
        }

    }

    private void initViews() {
        mListView = (PTRListView) findViewById(R.id.v_sticky_list);
        mListView.setOnScrollListener(mOnScrollListener);
        mListView.setListOverscrollListener(mListOverscrollListener);
    }

    public void setAdapter(ListAdapter adapter) {
        if (mListView != null) {
            mListView.setAdapter(adapter);
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        if (mListView != null) {
            mListView.setOnItemClickListener(listener);
        }
    }

    public void addHeaderView(View v) {
        if (mListView != null) {
            mListView.addHeaderView(v);
            if (v instanceof IListOverscrollListener) {
                mOverScrollDelegates.add(new WeakReference<IListOverscrollListener>((IListOverscrollListener) v));
            }
        }
    }

    public void addFooterView(View v) {
        if (mListView != null) {
            mListView.addFooterView(v);
        }
    }

    public void smoothScrollBy(int distance, int duration) {
        if (mListView != null) {
            mListView.smoothScrollBy(distance, duration);
        }
    }

    public void smoothScrollToPosition(int position) {
        if (mListView != null) {
            mListView.smoothScrollToPosition(position);
        }
    }

    public void addStickyHeader(IStickyHeaderAdapter adapter) {
        this.mAdapter = adapter;
        this.mStickyHeaderPosition = mListView.getHeaderViewsCount();

        mHeaderView = adapter.getStickyView();
        mListView.addHeaderView(mHeaderView);

        mStickyHeaderView = adapter.getStickyView();
        mStickyHeaderView.setVisibility(View.GONE);
        mStickyHeaderView.setClickable(true);
        addView(mStickyHeaderView, getChildCount(), new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mListView.setVerticalScrollBarEnabled(false);

        mAdapter.onStickyViewDisappeared(mHeaderView, mStickyHeaderView);
    }

    public View getHeaderView() {
        return mHeaderView;
    }

    public View getStickyView() {
        return mStickyHeaderView;
    }


}
