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

    private final Fragment fragment;

    private final TestController controller;

    private final LayoutInflater inflater;

    /*
     * 검색 BottomSheet 전용 Manager
     */
    private final HomeSearchBottomSheetManager bottomSheetManager;

    /*
     * 검색 입력창
     */
    private final EditText etSearch;

    /*
     * 검색 버튼
     */
    private final Button btnSearch;

    /*
     * 검색창 토글 버튼
     */
    private final Button btnToggle;

    /*
     * 검색 오버레이
     */
    private final View overlaySuggestion;

    /*
     * 자동완성 컨테이너
     */
    private final LinearLayout suggestionContainer;

    /*
     * 검색 결과 컨테이너
     */
    private final LinearLayout resultContainer;

    /*
     * 디바운스 핸들러
     */
    private final Handler debounceHandler;

    /*
     * 검색 결과 클릭 콜백
     */
    private final IViewDetailItemClickCallback itemClickCallback;

    /*
     * 검색 결과 활성 상태
     */
    private boolean searchActive = false;

    /*
     * 검색창 초기화 중인지 여부
     * setText("") 호출 시 TextWatcher가 다시 호출되는 것을 방지
     */
    private boolean clearingSearch = false;

    public HomeSearchManager(
            Fragment fragment,
            View rootView,
            TestController controller,
            IViewDetailItemClickCallback callback,
            HomeSearchBottomSheetManager bottomSheetManager
    ) {

        this.fragment = fragment;

        this.controller = controller;

        this.itemClickCallback = callback;

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

    /*
     * 초기화
     */
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

            if (keyword.isEmpty()) {

                clearSearchResultAndCloseOverlay();

                return;
            }

            doSearch(keyword);
        });
    }

    /*
     * 자동완성 초기화
     */
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

                        if (debounceRunnable[0]
                                != null) {

                            debounceHandler.removeCallbacks(
                                    debounceRunnable[0]
                            );
                        }

                        String keyword =
                                s.toString()
                                        .trim();

                        /*
                         * 검색어 비어있으면 자동완성 / 검색 결과 초기화
                         */
                        if (keyword.isEmpty()) {

                            clearSearchResultAndCloseOverlay();

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

                                                            for (KeywordMapVO vo
                                                                    : result) {

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

                                                                suggestionContainer.addView(item);
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

    /*
     * 검색 수행
     */
    public void doSearch(
            String keyword
    ) {

        searchActive = true;

        hideKeyboard();

        closeSearchOverlay();

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

    /*
     * 검색 결과 / 자동완성 / 검색창 상태 초기화
     */
    public void clearSearchResultAndCloseOverlay() {

        searchActive = false;

        clearingSearch = true;

        debounceHandler.removeCallbacksAndMessages(
                null
        );

        if (resultContainer != null) {
            resultContainer.removeAllViews();
        }

        if (suggestionContainer != null) {
            suggestionContainer.removeAllViews();
        }

        if (etSearch != null) {
            etSearch.setText("");
            etSearch.clearFocus();
        }

        clearingSearch = false;

        closeSearchOverlay();

        hideKeyboard();

        if (bottomSheetManager != null) {
            bottomSheetManager.hide();
        }
    }

    /*
     * 검색 결과 BottomSheet 표시 여부
     */
    public boolean isSearchResultVisible() {

        return bottomSheetManager != null
                && bottomSheetManager.isVisible();
    }

    /*
     * 검색어 입력 여부
     */
    public boolean hasSearchText() {

        return etSearch != null
                && etSearch.getText() != null
                && !etSearch.getText()
                .toString()
                .trim()
                .isEmpty();
    }

    /*
     * 검색 상태 존재 여부
     */
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