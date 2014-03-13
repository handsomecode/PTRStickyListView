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
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import is.handsome.ptr_sticky_listview.interfaces.IListOverscrollListener;
import is.handsome.ptr_sticky_listview.utils.BaseAppUtils;
import is.handsome.ptr_sticky_listview.utils.ResizeAnimation;
import is.handsome.sticky_list_header.R;

/**
 * Created by Egor Vorotnikov.
 * email:egorv@handsome.is
 */
public class PTRHeaderView extends LinearLayout implements IListOverscrollListener {

    private static final String TAG = "PullToRefreshHeaderView";

    private boolean mAlreadyInflated = false;

    public interface PullToRefreshListener {
        public void onRefresh();
    }

    private enum ViewState {
        INVISIBLE, PULLING, READY_TO_RELEASE, LOADING
    }


    private String mLoadingMessage;
    private View mHeaderArea;
    private LinearLayout mHeaderRoot;
    private TextView mHeaderText;
    private ImageView mHeaderImage;

    private static final int MAX_Y_OVERSROLL = 150;//dp
    private final int mMaxOverscrollInPixels;

    private ViewState mCurrentViewState = ViewState.INVISIBLE;
    private ViewState mPreviousViewState = ViewState.INVISIBLE;

    private int mLastYOffset = 0;
    private int mHistoricalYOffset = 0;

    private boolean isNeedToRefresh = false;

    private Handler mHandler = new Handler();

    private PullToRefreshListener mListener;

    public PTRHeaderView(Context context) {
        super(context);
        this.mMaxOverscrollInPixels = (int) BaseAppUtils.dipToPixels(context, MAX_Y_OVERSROLL);
        inflate();
    }

    public PTRHeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mMaxOverscrollInPixels = (int) BaseAppUtils.dipToPixels(context, MAX_Y_OVERSROLL);
        inflate();
    }

    public void setLoadingMessage(String message) {
        this.mLoadingMessage = message;
    }

    public void setPullToRefreshListener(PullToRefreshListener listener) {
        this.mListener = listener;
    }

    public void finishLoading() {
        mCurrentViewState = ViewState.INVISIBLE;
        post(mCloseHeader);
    }

    public void setProgressHeaderPadding(int left, int top, int right, int bottom) {
        mHeaderArea.setPadding(left, top, right, bottom);
        mHeaderArea.requestLayout();
    }

    private void inflate() {
        if (!mAlreadyInflated) {
            inflate(getContext(), R.layout.v_pull_to_refresh_header, this);
            initViews();
            mAlreadyInflated = true;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ViewGroup.LayoutParams p = mHeaderRoot.getLayoutParams();
        p.height = 0;
        mHeaderRoot.setLayoutParams(p);
        mHeaderRoot.requestLayout();
    }

    private void initViews() {
        mHeaderRoot = (LinearLayout) findViewById(R.id.v_ptr_root);
        mHeaderArea = findViewById(R.id.v_ptr_header_area);
        mHeaderText = (TextView) findViewById(R.id.v_ptr_header_text);
        mHeaderImage = (ImageView) findViewById(R.id.v_ptr_header_icon);
    }

    @Override
    public boolean onListOverscroll(int deltaY) {
        int currentHeght = mHeaderRoot.getLayoutParams().height;
        if (mHistoricalYOffset < currentHeght && mCurrentViewState == ViewState.LOADING) {
            mHistoricalYOffset = currentHeght;
        }
        mHistoricalYOffset += deltaY;
        if (mHistoricalYOffset < 0) {
            mHistoricalYOffset = 0;
        }
        if (mHistoricalYOffset > mMaxOverscrollInPixels) {
            mHistoricalYOffset = mMaxOverscrollInPixels;
        }
        if (mHistoricalYOffset < mHeaderArea.getLayoutParams().height
                && mCurrentViewState != ViewState.LOADING) {
            mCurrentViewState = ViewState.PULLING;
        }
        if (mHistoricalYOffset >= mHeaderArea.getLayoutParams().height
                && mCurrentViewState != ViewState.LOADING) {
            mCurrentViewState = ViewState.READY_TO_RELEASE;
        }
        updateViewToCurrentState();

        ViewGroup.LayoutParams params = mHeaderRoot.getLayoutParams();
        params.height = mHistoricalYOffset;
        mHeaderRoot.setGravity(Gravity.BOTTOM);
        mHeaderRoot.setLayoutParams(params);
        if (mHistoricalYOffset == 0 && mCurrentViewState != ViewState.LOADING) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onTouchUp() {
        if (mCurrentViewState == ViewState.READY_TO_RELEASE
                || mCurrentViewState == ViewState.LOADING) {
            mHandler.post(mCloseToShowProgress);
        } else {
            mHandler.post(mCloseHeader);
        }
    }

    private Runnable mCloseHeader = new Runnable() {
        @Override
        public void run() {
            try {
                mCurrentViewState = ViewState.INVISIBLE;
                mHistoricalYOffset = 0;
                ResizeAnimation anim = new ResizeAnimation(
                        mHeaderRoot,
                        1.2f,
                        mHeaderRoot.getLayoutParams().height,
                        0);
                anim.setInterpolator(new AccelerateInterpolator());
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        try {
                            mCurrentViewState = ViewState.INVISIBLE;
                            updateViewToCurrentState();
                        } catch (Exception e) {
                            Log.w(TAG, e);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mHeaderRoot.startAnimation(anim);
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }
    };

    private Runnable mCloseToShowProgress = new Runnable() {
        @Override
        public void run() {
            try {
                mHistoricalYOffset = 0;
                ResizeAnimation anim = new ResizeAnimation(
                        mHeaderRoot,
                        1.2f,
                        mHeaderRoot.getLayoutParams().height,
                        mHeaderArea.getLayoutParams().height);
                anim.setInterpolator(new AccelerateInterpolator());
                anim.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        try {
                            mCurrentViewState = ViewState.LOADING;
                            if (mListener != null) {
                                mListener.onRefresh();
                            }
                            updateViewToCurrentState();
                        } catch (Exception e) {
                            Log.w(TAG, e);
                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mHeaderRoot.startAnimation(anim);
            } catch (Exception e) {
                Log.w(TAG, e);
            }
        }
    };

    private void updateViewToCurrentState() {
        ViewState previous = mPreviousViewState;
        if (mPreviousViewState != null) {
            if (mPreviousViewState == mCurrentViewState) {
                return;
            } else {
                mPreviousViewState = mCurrentViewState;
            }
        }
        switch (mCurrentViewState) {
            case INVISIBLE:
                break;
            case PULLING:
                mHeaderText.setText(getResources().getString(R.string.pull_to_refresh));
                mHeaderImage.clearAnimation();
                if (previous == ViewState.READY_TO_RELEASE) {
                    mHeaderImage.clearAnimation();
                    Animation rotate = AnimationUtils.loadAnimation(mHeaderImage.getContext(), R.anim.anim_flip_horizontaly_reverse);
                    rotate.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {

                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            post(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        if (mCurrentViewState == ViewState.PULLING) {
                                            mHeaderImage.setImageResource(R.drawable.arrow_down);
                                            mHeaderImage.clearAnimation();
                                        }
                                    } catch (Exception e) {
                                        Log.w(TAG, e);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {

                        }
                    });
                    mHeaderImage.startAnimation(rotate);
                } else {
                    mHeaderImage.setImageResource(R.drawable.arrow_down);
                }

                break;
            case READY_TO_RELEASE:
                mHeaderText.setText(getResources().getString(R.string.release_to_refresh));
                mHeaderImage.clearAnimation();
                Animation rotate = AnimationUtils.loadAnimation(mHeaderImage.getContext(), R.anim.anim_flip_horizontaly);
                rotate.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    if (mCurrentViewState == ViewState.READY_TO_RELEASE) {
                                        mHeaderImage.setImageResource(R.drawable.arrow_up);
                                        mHeaderImage.clearAnimation();
                                    }
                                } catch (Exception e) {
                                    Log.w(TAG, e);
                                }
                            }
                        });

                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                mHeaderImage.startAnimation(rotate);
                break;
            case LOADING:
                mHeaderText.setText(mLoadingMessage);
                mHeaderImage.clearAnimation();
                mHeaderImage.setImageResource(R.drawable.loop);
                mHeaderImage.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Animation rotate = AnimationUtils.loadAnimation(mHeaderImage.getContext(), R.anim.anim_spin);
                            rotate.setInterpolator(new LinearInterpolator());
                            mHeaderImage.startAnimation(rotate);
                        } catch (Exception e) {
                            Log.w(TAG, e);
                        }
                    }
                });
                break;
        }
    }


}
