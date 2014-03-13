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

package com.example.StickyListHeaderExample;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import is.handsome.ptr_sticky_listview.interfaces.IStickyHeaderAdapter;
import is.handsome.ptr_sticky_listview.views.PTRHeaderView;
import is.handsome.ptr_sticky_listview.views.StickyHeaderListView;




public class MyActivity extends Activity {

    private StickyHeaderListView mListView;
    private PTRHeaderView mPTRHeaderView;

    private int mHeaderCount = 0;

    private Handler mHandler = new Handler();

    private IStickyHeaderAdapter mSecondHeaderAdapter = new IStickyHeaderAdapter() {
        @Override
        public View getStickyView() {
            View v = LayoutInflater.from(MyActivity.this).inflate(R.layout.ad_sticky_header, null, false);
            v.findViewById(R.id.ad_sticky_header_button).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mHeaderCount++;
                    ((TextView) mListView.getStickyView().findViewById(R.id.ad_sticky_header_counter_text)).setText(mHeaderCount + "");
                    ((TextView) mListView.getHeaderView().findViewById(R.id.ad_sticky_header_counter_text)).setText(mHeaderCount + "");
                }
            });
            return v;
        }

        // in case the view itself stores the state you can use this 2 callbacks to sync sticky and header views

        @Override
        public void onStickyViewAppeared(View headerView, View stickyView) {
            //TODO sync headerView -> stickyView
        }

        @Override
        public void onStickyViewDisappeared(View headerView, View stickyView) {
            //TODO sync stickyView - > headerView
        }
    };

    private PTRHeaderView.PullToRefreshListener mPullToRefreshListener = new PTRHeaderView.PullToRefreshListener() {
        @Override
        public void onRefresh() {
            //handle data refresh here
            //will simply wait for several seconds and dismiss pull-to-refresh progress
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        mPTRHeaderView.finishLoading();
                    } catch (Exception e) {
                        //pokemon!!! catch them all =)
                    }
                }
            }, 4000);
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        initViews();
        initHeader();
        initAdapter();

    }

    private void initViews() {
        mListView = (StickyHeaderListView) findViewById(R.id.ac_main_list_view);
    }

    private void initHeader() {
        //let's add several headers and stick only one of them

        //header 1
        TextView text1 = new TextView(this);
        text1.setLayoutParams(
                new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        (int) getResources().getDimension(R.dimen.header_height)));
        text1.setText("Header 1");
        text1.setGravity(Gravity.CENTER_VERTICAL);
        text1.setPadding(30, 0, 0, 0);
        text1.setTextColor(Color.BLACK);
        text1.setBackgroundColor(Color.WHITE);
        mListView.addHeaderView(text1);

        //header 2
        TextView text2 = new TextView(this);
        text2.setLayoutParams(
                new AbsListView.LayoutParams(
                        AbsListView.LayoutParams.MATCH_PARENT,
                        (int) getResources().getDimension(R.dimen.header_height)));
        text2.setText("Header 2");
        text2.setGravity(Gravity.CENTER_VERTICAL);
        text2.setPadding(30, 0, 0, 0);
        text2.setTextColor(Color.BLACK);
        text2.setBackgroundColor(Color.GRAY);
        mListView.addHeaderView(text2);

        //sticky header
        mListView.addStickyHeader(mSecondHeaderAdapter);

        //pull-to-refresh header
        mPTRHeaderView = new PTRHeaderView(this);
        mPTRHeaderView.setLoadingMessage("Loading FAKE data...");
        mPTRHeaderView.setPullToRefreshListener(mPullToRefreshListener);
        mListView.addHeaderView(mPTRHeaderView);
    }

    private void initAdapter() {
        String[] items = {"Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream",
                "Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream",
                "Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream",
                "Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream",
                "Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream",
                "Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream",
                "Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream",
                "Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream",
                "Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream",
                "Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream",
                "Milk", "Butter", "Yogurt", "Toothpaste", "Ice Cream"};

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, items);

        mListView.setAdapter(adapter);
    }
}
