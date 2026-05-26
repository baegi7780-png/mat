package com.tech.motjip.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.kakao.vectormap.MapView;
import com.tech.motjip.API.KakaoMap.CallbackInterface.IViewDetailItemClickCallback;
import com.tech.motjip.Controller.TestController;
import com.tech.motjip.Model.KeywordMapVO;
import com.tech.motjip.R;
import com.tech.motjip.Thread.IThreadCallback;
import com.tech.motjip.manager.HomeDetailManager;
import com.tech.motjip.manager.HomeDetailPageManager;
import com.tech.motjip.manager.HomeLocationManager;
import com.tech.motjip.manager.HomeSearchBottomSheetManager;
import com.tech.motjip.manager.HomeSearchManager;

public class HomeFragment extends Fragment implements IViewDetailItemClickCallback {

    private MapView mapView;
    private View loadingView;

    private TestController controller;
    private IThreadCallback callback;

    /*
     * 검색 BottomSheet 전용
     */
    private HomeSearchBottomSheetManager searchBottomSheetManager;

    /*
     * 상세 Overlay 전용
     */
    private HomeDetailPageManager detailPageManager;

    /*
     * 상세 데이터 전용
     */
    private HomeDetailManager detailManager;

    private HomeSearchManager searchManager;
    private HomeLocationManager locationManager;

    private TabLayout tabDetail;
    private View[] tabContents;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(
                R.layout.fragment_home,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(
                view,
                savedInstanceState
        );

        mapView =
                view.findViewById(
                        R.id.map_view
                );

        loadingView =
                view.findViewById(
                        R.id.view_loading
                );

        controller =
                new TestController(
                        requireActivity()
                );

        /*
         * 검색 BottomSheet Manager
         */
        searchBottomSheetManager =
                new HomeSearchBottomSheetManager(
                        view
                );

        /*
         * 상세 Overlay Manager
         */
        detailPageManager =
                new HomeDetailPageManager(
                        view
                );

        initDetailTabs(
                view
        );

        /*
         * 상세 데이터 Manager
         */
        detailManager =
                new HomeDetailManager(
                        this,
                        view
                );

        searchManager =
                new HomeSearchManager(
                        this,
                        view,
                        controller,
                        this,
                        searchBottomSheetManager
                );

        callback =
                new IThreadCallback() {
                    @Override
                    public void ThreadEnds() {

                        if (!isAdded()) {
                            return;
                        }

                        requireActivity().runOnUiThread(() -> {

                            if (loadingView != null) {
                                loadingView.setVisibility(
                                        View.GONE
                                );
                            }

                            controller.setMarkerClickListener(
                                    HomeFragment.this
                            );
                        });
                    }
                };

        locationManager =
                new HomeLocationManager(
                        this,
                        mapView,
                        controller,
                        callback
                );

        View btnDetailClose =
                view.findViewById(
                        R.id.btn_detail_close
                );

        if (btnDetailClose != null) {

            btnDetailClose.setOnClickListener(v -> {

                /*
                 * 상세 Overlay 닫기
                 */
                if (detailPageManager != null) {
                    detailPageManager.closeDetailPage();
                }

                /*
                 * 홈 탭 선택
                 */
                selectDetailTab(
                        0
                );
            });
        }

        /*
         * 뒤로가기 처리
         * 1. 상세페이지 열림 → 상세 닫기
         * 2. 검색 상태 존재 → 검색 초기화
         * 3. 아무것도 없음 → 기존 뒤로가기
         */
        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(
                        getViewLifecycleOwner(),
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                /*
                                 * 상세페이지가 열려있으면
                                 * 뒤로가기 시 상세페이지 닫기
                                 */
                                if (detailPageManager != null
                                        && detailPageManager.isDetailPageVisible()) {

                                    detailPageManager.closeDetailPage();

                                    selectDetailTab(
                                            0
                                    );

                                    return;
                                }

                                /*
                                 * 검색 상태가 남아있으면
                                 * 검색 결과 제거 + 검색창 초기화 + 화살표 닫기
                                 */
                                if (searchManager != null
                                        && searchManager.hasActiveSearch()) {

                                    searchManager.clearSearchResultAndCloseOverlay();

                                    return;
                                }

                                /*
                                 * 상세페이지와 검색 결과가 모두 닫혀있으면
                                 * 기존 뒤로가기 동작 수행
                                 */
                                setEnabled(false);

                                requireActivity()
                                        .getOnBackPressedDispatcher()
                                        .onBackPressed();
                            }
                        }
                );

        if (mapView != null) {

            mapView.post(() -> {

                if (locationManager != null) {
                    locationManager.startMap();
                }
            });
        }
    }

    private void initDetailTabs(
            View view
    ) {

        tabDetail =
                view.findViewById(
                        R.id.tab_detail
                );

        if (tabDetail == null) {
            return;
        }

        tabContents =
                new View[]{
                        view.findViewById(
                                R.id.tab_home
                        ),
                        view.findViewById(
                                R.id.tab_menu
                        ),
                        view.findViewById(
                                R.id.tab_photo
                        ),
                        view.findViewById(
                                R.id.tab_review1
                        ),
                        view.findViewById(
                                R.id.tab_review2
                        )
                };

        tabDetail.addOnTabSelectedListener(
                new TabLayout.OnTabSelectedListener() {

                    @Override
                    public void onTabSelected(
                            TabLayout.Tab tab
                    ) {

                        showTabContent(
                                tab.getPosition()
                        );
                    }

                    @Override
                    public void onTabUnselected(
                            TabLayout.Tab tab
                    ) {}

                    @Override
                    public void onTabReselected(
                            TabLayout.Tab tab
                    ) {

                        showTabContent(
                                tab.getPosition()
                        );
                    }
                }
        );

        showTabContent(
                0
        );
    }

    private void selectDetailTab(
            int position
    ) {

        if (tabDetail == null) {
            showTabContent(
                    position
            );
            return;
        }

        TabLayout.Tab tab =
                tabDetail.getTabAt(
                        position
                );

        if (tab != null) {

            tab.select();

        } else {

            showTabContent(
                    position
            );
        }
    }

    private void showTabContent(
            int position
    ) {

        if (tabContents == null) {
            return;
        }

        for (int i = 0;
             i < tabContents.length;
             i++) {

            if (tabContents[i] == null) {
                continue;
            }

            tabContents[i].setVisibility(
                    i == position
                            ? View.VISIBLE
                            : View.GONE
            );
        }
    }

    @Override
    public void onItemClick(
            KeywordMapVO vo
    ) {

        if (vo == null) {
            return;
        }

        /*
         * 상세 Overlay 열기
         */
        if (detailPageManager != null) {
            detailPageManager.openDetailPage();
        }

        /*
         * 홈 탭 선택
         */
        selectDetailTab(
                0
        );

        /*
         * 상세 데이터 로드
         */
        if (detailManager != null) {
            detailManager.openDetail(
                    vo
            );
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mapView != null) {
            mapView.resume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        if (mapView != null) {
            mapView.pause();
        }
    }

    @Override
    public void onDestroyView() {

        if (searchManager != null) {
            searchManager.clear();
        }

        mapView = null;
        loadingView = null;
        tabDetail = null;
        tabContents = null;

        super.onDestroyView();
    }
}