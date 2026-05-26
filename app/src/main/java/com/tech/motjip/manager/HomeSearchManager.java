package com.tech.motjip.manager;

import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.tech.motjip.API.KakaoMap.CallbackInterface.IViewDetailItemClickCallback;
import com.tech.motjip.Controller.TestController;
import com.tech.motjip.Model.KeywordMapVO;
import com.tech.motjip.R;
import com.tech.motjip.Thread.IThreadReturn1Callback;

import java.util.List;

public class HomeSearchManager {

    private static final String TAG =
            "HomeSearchManager";

    private final Fragment fragment;

    private final TestController controller;

    private final LayoutInflater inflater;

    private final HomeSearchBottomSheetManager bottomSheetManager;

    private final EditText etSearch;

    private final Button btnSearch;

    private final Button btnToggle;

    private final View overlaySuggestion;

    private final LinearLayout suggestionContainer;

    private final LinearLayout resultContainer;

    private final Handler debounceHandler;

    private final IViewDetailItemClickCallback itemClickCallback;

    private boolean searchActive =
            false;

    private boolean clearingSearch =
            false;

    public HomeSearchManager(
            Fragment fragment,
            View rootView,
            TestController controller,
            IViewDetailItemClickCallback callback,
            HomeSearchBottomSheetManager bottomSheetManager
    ) {

        this.fragment =
                fragment;

        this.controller =
                controller;

        this.itemClickCallback =
                callback;

        this.bottomSheetManager =
                bottomSheetManager;

        inflater =
                LayoutInflater.from(
                        fragment.requireContext()
                );

        etSearch =
                rootView.findViewById(
                        R.id.et_search
                );

        btnSearch =
                rootView.findViewById(
                        R.id.btn_search
                );

        btnToggle =
                rootView.findViewById(
                        R.id.btn_toggle_suggestion
                );

        overlaySuggestion =
                rootView.findViewById(
                        R.id.overlay_suggestion
                );

        suggestionContainer =
                rootView.findViewById(
                        R.id.ll_suggestion_container
                );

        resultContainer =
                rootView.findViewById(
                        R.id.ll_search_result_container
                );

        debounceHandler =
                new Handler(
                        Looper.getMainLooper()
                );

        init();
    }

    private void init() {

        btnToggle.setOnClickListener(v -> {

            if (overlaySuggestion.getVisibility()
                    == View.VISIBLE) {

                closeSearchOverlay();

            } else {

                openSearchOverlay();
            }
        });

        etSearch.setOnFocusChangeListener(
                (v, hasFocus) -> {

                    if (hasFocus) {

                        openSearchOverlay();
                    }
                }
        );

        etSearch.setOnClickListener(
                v -> openSearchOverlay()
        );

        initSuggestionSearch();

        btnSearch.setOnClickListener(v -> {

            String keyword =
                    etSearch.getText()
                            .toString()
                            .trim();

            doSearch(
                    keyword
            );
        });
    }

    private void initSuggestionSearch() {

        Runnable[] debounceRunnable = {
                null
        };

        etSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {}

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {

                        if (clearingSearch) {
                            return;
                        }

                        if (debounceRunnable[0] != null) {

                            debounceHandler.removeCallbacks(
                                    debounceRunnable[0]
                            );
                        }

                        String keyword =
                                s.toString()
                                        .trim();

                        /*
                         * 검색어가 비어 있을 때는
                         * 검색 마커를 지우지 않습니다.
                         *
                         * 검색 마커는 추천 마커가 켜질 때만 제거됩니다.
                         */
                        if (keyword.isEmpty()) {

                            Log.d(
                                    TAG,
                                    "검색어 비어 있음 → 마커 유지, 자동완성만 초기화"
                            );

                            searchActive =
                                    false;

                            debounceHandler.removeCallbacksAndMessages(
                                    null
                            );

                            if (suggestionContainer != null) {

                                suggestionContainer.removeAllViews();
                            }

                            if (resultContainer != null) {

                                resultContainer.removeAllViews();
                            }

                            if (bottomSheetManager != null) {

                                bottomSheetManager.hide();
                            }

                            return;
                        }

                        debounceRunnable[0] =
                                () -> controller.searchMapData(
                                        keyword,
                                        new IThreadReturn1Callback<List<KeywordMapVO>>() {

                                            @Override
                                            public void ThreadEnds(
                                                    List<KeywordMapVO> result
                                            ) {

                                                if (!fragment.isAdded()) {
                                                    return;
                                                }

                                                fragment.requireActivity()
                                                        .runOnUiThread(() -> {

                                                            suggestionContainer.removeAllViews();

                                                            for (KeywordMapVO vo : result) {

                                                                View item =
                                                                        inflater.inflate(
                                                                                R.layout.item_suggestion,
                                                                                suggestionContainer,
                                                                                false
                                                                        );

                                                                ((TextView) item.findViewById(
                                                                        R.id.tv_suggestion_name
                                                                )).setText(
                                                                        vo.getPlace_name()
                                                                );

                                                                item.setOnClickListener(v -> {

                                                                    etSearch.setText(
                                                                            vo.getPlace_name()
                                                                    );

                                                                    closeSearchOverlay();

                                                                    doSearch(
                                                                            vo.getPlace_name()
                                                                    );
                                                                });

                                                                suggestionContainer.addView(
                                                                        item
                                                                );
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onError(
                                                    Exception e
                                            ) {

                                                Log.e(
                                                        "Suggestion",
                                                        "검색 오류",
                                                        e
                                                );
                                            }
                                        }
                                );

                        debounceHandler.postDelayed(
                                debounceRunnable[0],
                                500
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {}
                }
        );
    }

    public void doSearch(
            String keyword
    ) {

        if (keyword == null) {

            keyword =
                    "";
        }

        keyword =
                keyword.trim();

        hideKeyboard();

        closeSearchOverlay();

        /*
         * 검색어가 비어 있으면
         * 검색 마커와 추천 마커 모두 유지합니다.
         * 실제 검색만 중단합니다.
         */
        if (keyword.isEmpty()) {

            Log.d(
                    TAG,
                    "검색어 비어 있음 → 검색 중단, 마커 유지"
            );

            searchActive =
                    false;

            if (resultContainer != null) {

                resultContainer.removeAllViews();
            }

            if (suggestionContainer != null) {

                suggestionContainer.removeAllViews();
            }

            if (bottomSheetManager != null) {

                bottomSheetManager.hide();
            }

            return;
        }

        if (controller != null) {

            /*
             * 검색어가 있을 때 검색을 실행하면
             * 추천 마커를 제거하고 기존 검색 마커도 새 결과로 교체합니다.
             */
            controller.clearRecommendedMarkers();

            controller.clearMarkers();
        }

        searchActive =
                true;

        if (bottomSheetManager != null) {

            bottomSheetManager.openResultSheet();
        }

        controller.searchMapData(
                keyword,
                controller.createSearchCallback(
                        resultContainer,
                        suggestionContainer,
                        overlaySuggestion,
                        inflater,
                        itemClickCallback
                )
        );
    }

    public void openSearchOverlay() {

        overlaySuggestion.setVisibility(
                View.VISIBLE
        );

        btnToggle.setText(
                "▲"
        );
    }

    public void closeSearchOverlay() {

        overlaySuggestion.setVisibility(
                View.GONE
        );

        btnToggle.setText(
                "▼"
        );
    }

    public void clearSearchResultAndCloseOverlay() {

        searchActive =
                false;

        clearingSearch =
                true;

        debounceHandler.removeCallbacksAndMessages(
                null
        );

        /*
         * 검색창 초기화는 마커를 지우지 않습니다.
         * 검색 마커는 추천 마커가 켜질 때,
         * 또는 새 검색어로 검색할 때만 교체됩니다.
         */

        if (resultContainer != null) {

            resultContainer.removeAllViews();
        }

        if (suggestionContainer != null) {

            suggestionContainer.removeAllViews();
        }

        if (etSearch != null) {

            etSearch.setText(
                    ""
            );

            etSearch.clearFocus();
        }

        clearingSearch =
                false;

        closeSearchOverlay();

        hideKeyboard();

        if (bottomSheetManager != null) {

            bottomSheetManager.hide();
        }
    }

    public boolean isSearchResultVisible() {

        return bottomSheetManager != null
                && bottomSheetManager.isVisible();
    }

    public boolean hasSearchText() {

        return etSearch != null
                && etSearch.getText() != null
                && !etSearch.getText()
                .toString()
                .trim()
                .isEmpty();
    }

    public boolean hasActiveSearch() {

        return searchActive
                || isSearchResultVisible()
                || hasSearchText();
    }

    private void hideKeyboard() {

        InputMethodManager imm =
                fragment.requireActivity()
                        .getSystemService(
                                InputMethodManager.class
                        );

        if (imm != null
                && etSearch != null) {

            imm.hideSoftInputFromWindow(
                    etSearch.getWindowToken(),
                    0
            );
        }
    }

    public void clear() {

        debounceHandler.removeCallbacksAndMessages(
                null
        );
    }
}