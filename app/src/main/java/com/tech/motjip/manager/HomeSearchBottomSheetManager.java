package com.tech.motjip.manager;

import android.util.Log;
import android.view.View;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.tech.motjip.R;

public class HomeSearchBottomSheetManager {

    private static final String TAG = "SEARCH_BOTTOM_SHEET_DEBUG";

    private final View bottomSheet;
    private final BottomSheetBehavior<View> bottomSheetBehavior;

    public HomeSearchBottomSheetManager(View rootView) {

        bottomSheet =
                rootView.findViewById(
                        R.id.bottom_sheet
                );

        bottomSheetBehavior =
                BottomSheetBehavior.from(
                        bottomSheet
                );

        bottomSheet.setMinimumHeight(
                0
        );

        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setFitToContents(false);
        bottomSheetBehavior.setSkipCollapsed(false);
        bottomSheetBehavior.setDraggable(true);

        bottomSheetBehavior.setPeekHeight(
                dpToPx(rootView, 20),
                false
        );

        bottomSheetBehavior.setExpandedOffset(
                dpToPx(rootView, 300)
        );

        bottomSheetBehavior.addBottomSheetCallback(
                new BottomSheetBehavior.BottomSheetCallback() {

                    @Override
                    public void onStateChanged(
                            View bottomSheet,
                            int newState
                    ) {

                        Log.d(TAG, "state = " + newState);

                        if (newState
                                == BottomSheetBehavior.STATE_HALF_EXPANDED) {

                            bottomSheetBehavior.setState(
                                    BottomSheetBehavior.STATE_COLLAPSED
                            );
                        }
                    }

                    @Override
                    public void onSlide(
                            View bottomSheet,
                            float slideOffset
                    ) {}
                }
        );

        bottomSheet.setVisibility(
                View.GONE
        );

        bottomSheet.post(() -> {

            bottomSheetBehavior.setPeekHeight(
                    dpToPx(rootView, 20),
                    false
            );
        });

        Log.d(TAG, "HomeSearchBottomSheetManager init");
    }

    public void openResultSheet() {

        Log.d(TAG, "openResultSheet()");

        bottomSheet.setVisibility(
                View.VISIBLE
        );

        bottomSheetBehavior.setPeekHeight(
                dpToPx(bottomSheet, 20),
                false
        );

        bottomSheetBehavior.setDraggable(true);

        bottomSheetBehavior.setState(
                BottomSheetBehavior.STATE_EXPANDED
        );
    }

    public void collapse() {

        Log.d(TAG, "collapse()");

        bottomSheet.setVisibility(
                View.VISIBLE
        );

        bottomSheetBehavior.setPeekHeight(
                dpToPx(bottomSheet, 20),
                false
        );

        bottomSheetBehavior.setDraggable(true);

        bottomSheetBehavior.setState(
                BottomSheetBehavior.STATE_COLLAPSED
        );
    }

    public void expand() {

        Log.d(TAG, "expand()");

        bottomSheet.setVisibility(
                View.VISIBLE
        );

        bottomSheetBehavior.setDraggable(true);

        bottomSheetBehavior.setState(
                BottomSheetBehavior.STATE_EXPANDED
        );
    }

    public void hide() {

        Log.d(TAG, "hide()");

        bottomSheet.setVisibility(
                View.GONE
        );
    }

    public void close() {

        Log.d(TAG, "close()");

        bottomSheet.setVisibility(
                View.GONE
        );
    }

    /*
     * 검색 결과 BottomSheet 표시 여부
     */
    public boolean isVisible() {

        return bottomSheet != null
                && bottomSheet.getVisibility() == View.VISIBLE;
    }

    public BottomSheetBehavior<View> getBehavior() {

        return bottomSheetBehavior;
    }

    private int dpToPx(
            View view,
            int dp
    ) {

        return (int) (
                dp
                        * view.getResources()
                        .getDisplayMetrics()
                        .density
        );
    }
}