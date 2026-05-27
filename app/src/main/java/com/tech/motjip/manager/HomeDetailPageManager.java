package com.tech.motjip.manager;

import android.util.Log;
import android.view.View;

import com.google.android.material.tabs.TabLayout;
import com.tech.motjip.R;

public class HomeDetailPageManager {

    private static final String TAG = "DETAIL_PAGE_DEBUG";

    private final View detailPage;
    private final View viewDetailLoading;
    private final TabLayout tabDetail;

    public HomeDetailPageManager(View rootView) {

        detailPage =
                rootView.findViewById(
                        R.id.detail_page
                );

        viewDetailLoading =
                rootView.findViewById(
                        R.id.view_detail_loading
                );

        tabDetail =
                rootView.findViewById(
                        R.id.tab_detail
                );

        Log.d(TAG, "HomeDetailPageManager init");
    }

    /*
     * 상세페이지 열기
     * 검색 BottomSheet와 별개로 overlay만 표시
     */
    public void openDetailPage() {

        Log.d(TAG, "openDetailPage()");

        if (detailPage != null) {
            detailPage.setVisibility(
                    View.VISIBLE
            );

            detailPage.bringToFront();
        }

        selectHomeTab();
    }

    /*
     * 상세페이지 닫기
     */
    public void closeDetailPage() {

        Log.d(TAG, "closeDetailPage()");

        if (detailPage != null) {
            detailPage.setVisibility(
                    View.GONE
            );
        }

        if (viewDetailLoading != null) {
            viewDetailLoading.setVisibility(
                    View.GONE
            );
        }

        selectHomeTab();
    }

    /*
     * 상세 로딩 표시
     */
    public void showDetailLoading() {

        Log.d(TAG, "showDetailLoading()");

        if (viewDetailLoading != null) {
            viewDetailLoading.setVisibility(
                    View.VISIBLE
            );

            viewDetailLoading.bringToFront();
        }
    }

    /*
     * 상세 로딩 숨기기
     */
    public void hideDetailLoading() {

        Log.d(TAG, "hideDetailLoading()");

        if (viewDetailLoading != null) {
            viewDetailLoading.setVisibility(
                    View.GONE
            );
        }
    }

    /*
     * 상세페이지 표시 여부
     */
    public boolean isDetailPageVisible() {

        return detailPage != null
                && detailPage.getVisibility() == View.VISIBLE;
    }

    private void selectHomeTab() {

        if (tabDetail != null) {

            TabLayout.Tab homeTab =
                    tabDetail.getTabAt(
                            0
                    );

            if (homeTab != null) {
                homeTab.select();
            }
        }
    }
}