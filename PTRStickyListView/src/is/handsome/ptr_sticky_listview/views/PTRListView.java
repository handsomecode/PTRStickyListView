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
import android.view.MotionEvent;
import android.widget.ListView;
import is.handsome.ptr_sticky_listview.interfaces.IListOverscrollListener;

/**
 * Created by Egor Vorotnikov.
 * email:egorv@handsome.is
 */
public class PTRListView extends ListView {

    private static final String TAG = "PTRListView";

    private IListOverscrollListener mListener;

    private float lastY = -1;

    public PTRListView(Context context) {
        super(context);
    }

    public PTRListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PTRListView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setListOverscrollListener(IListOverscrollListener listener) {
        this.mListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        int firstVisibleItem = getFirstVisiblePosition();
        boolean onTop = firstVisibleItem == 0 && getChildAt(0) != null && getChildAt(0).getTop() >= 0;

        boolean result = true;

        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:

                break;
            case MotionEvent.ACTION_MOVE:
                if (onTop) {
                    if (lastY > 0) {
                        float deltaY = ev.getY() - lastY;
                        lastY = ev.getY();
                        if (onTop) {
                            if (mListener != null) {
                                result = mListener.onListOverscroll((int) deltaY);
                            }
                        }
                    } else {
                        lastY = ev.getY();
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mListener != null) {
                    mListener.onTouchUp();
                    lastY = -1;
                }
                break;
        }

        if (result) {
            return super.onTouchEvent(ev);
        } else {
            return true;
        }
    }

}
