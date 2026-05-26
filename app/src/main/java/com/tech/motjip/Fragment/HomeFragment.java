package com.tech.motjip.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.kakao.vectormap.MapView;
import com.tech.motjip.API.KakaoMap.CallbackInterface.IViewDetailItemClickCallback;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Adapter.PlaceReviewAdapter;
import com.tech.motjip.Controller.TestController;
import com.tech.motjip.Model.KeywordMapVO;
import com.tech.motjip.Model.RecommendedPlace;
import com.tech.motjip.Model.Review;
import com.tech.motjip.R;
import com.tech.motjip.ReviewWriteActivity;
import com.tech.motjip.Thread.IThreadCallback;
import com.tech.motjip.Thread.IThreadReturn1Callback;
import com.tech.motjip.manager.HomeDetailManager;
import com.tech.motjip.manager.HomeDetailPageManager;
import com.tech.motjip.manager.HomeLocationManager;
import com.tech.motjip.manager.HomeSearchBottomSheetManager;
import com.tech.motjip.manager.HomeSearchManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment
        extends Fragment
        implements IViewDetailItemClickCallback {

    private static final String TAG =
            "HomeFragmentDebug";

    private MapView mapView;
    private View loadingView;

    private TestController controller;
    private IThreadCallback callback;

    private HomeSearchBottomSheetManager searchBottomSheetManager;
    private HomeDetailPageManager detailPageManager;
    private HomeDetailManager detailManager;

    private HomeSearchManager searchManager;
    private HomeLocationManager locationManager;

    private KeywordMapVO selectedPlace;

    private boolean isRecommendMode =
            false;

    private Long memberId =
            -1L;

    private TabLayout tabDetail;
    private View[] tabContents;

    private RecyclerView rvPlaceReviews;

    private TextView tvReview2Empty;

    private PlaceReviewAdapter placeReviewAdapter;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    )  {
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

        Log.d(
                TAG,
                "onViewCreated 시작"
        );

        loadLoginMemberId();

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

        searchBottomSheetManager =
                new HomeSearchBottomSheetManager(
                        view
                );

        detailPageManager =
                new HomeDetailPageManager(
                        view
                );

        initDetailTabs(
                view
        );

        initReview2RecyclerView(
                view
        );

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

        View btnRecommend =
                view.findViewById(
                        R.id.btn_recommend
                );

        if (btnRecommend != null) {

            btnRecommend.bringToFront();

            btnRecommend.setOnClickListener(v -> {

                toggleRecommendMode();
            });
        }

        View btnDetailClose =
                view.findViewById(
                        R.id.btn_detail_close
                );

        if (btnDetailClose != null) {

            btnDetailClose.setOnClickListener(v -> {

                if (detailPageManager != null) {

                    detailPageManager.closeDetailPage();
                }

                selectDetailTab(
                        0
                );
            });
        }

        View btnWriteReview2 =
                view.findViewById(
                        R.id.btn_write_review2
                );

        if (btnWriteReview2 != null) {

            btnWriteReview2.setOnClickListener(v -> {

                openReviewWriteActivity();
            });
        }

        requireActivity()
                .getOnBackPressedDispatcher()
                .addCallback(
                        getViewLifecycleOwner(),
                        new OnBackPressedCallback(true) {

                            @Override
                            public void handleOnBackPressed() {

                                if (detailPageManager != null
                                        && detailPageManager.isDetailPageVisible()) {

                                    detailPageManager.closeDetailPage();

                                    selectDetailTab(
                                            0
                                    );

                                    return;
                                }

                                if (searchManager != null
                                        && searchManager.hasActiveSearch()) {

                                    searchManager.clearSearchResultAndCloseOverlay();

                                    return;
                                }

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

    private void toggleRecommendMode() {

        isRecommendMode =
                !isRecommendMode;

        if (isRecommendMode) {

            loadRecommendedPlaces();

        } else {

            if (controller != null) {

                controller.clearRecommendedMarkers();
            }
        }
    }

    private void loadRecommendedPlaces() {

        RetrofitClient
                .getApiService(
                        requireContext()
                )
                .getRecommendedPlaces()
                .enqueue(
                        new Callback<List<RecommendedPlace>>() {

                            @Override
                            public void onResponse(
                                    Call<List<RecommendedPlace>> call,
                                    Response<List<RecommendedPlace>> response
                            ) {

                                if (!isAdded()) {
                                    return;
                                }

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    List<RecommendedPlace> places =
                                            response.body();

                                    if (controller != null) {

                                        controller.clearMarkers();

                                        controller.setRecommendedMarkers(
                                                places
                                        );
                                    }
                                } else {

                                    Log.e(
                                            TAG,
                                            "추천 장소 조회 실패 code = "
                                                    + response.code()
                                    );

                                    Toast.makeText(
                                            requireContext(),
                                            "추천 장소 조회 실패",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    isRecommendMode =
                                            false;
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<List<RecommendedPlace>> call,
                                    Throwable t
                            ) {

                                if (!isAdded()) {
                                    return;
                                }

                                Log.e(
                                        TAG,
                                        "추천 장소 서버 연결 실패",
                                        t
                                );

                                Toast.makeText(
                                        requireContext(),
                                        "서버 연결 실패",
                                        Toast.LENGTH_SHORT
                                ).show();

                                isRecommendMode =
                                        false;
                            }
                        }
                );
    }

    private void loadLoginMemberId() {

        SharedPreferences prefs =
                requireContext()
                        .getSharedPreferences(
                                "auth",
                                requireContext().MODE_PRIVATE
                        );

        memberId =
                prefs.getLong(
                        "memberId",
                        -1L
                );

        Log.d(
                TAG,
                "로그인 memberId = "
                        + memberId
        );
    }

    private void initReview2RecyclerView(
            View view
    ) {

        rvPlaceReviews =
                view.findViewById(
                        R.id.rv_place_reviews
                );

        tvReview2Empty =
                view.findViewById(
                        R.id.tv_review2_empty
                );

        placeReviewAdapter =
                new PlaceReviewAdapter();

        placeReviewAdapter.setOnReviewActionListener(
                new PlaceReviewAdapter.OnReviewActionListener() {

                    @Override
                    public void onEditReview(
                            Review review
                    ) {

                        openReviewEditActivity(
                                review
                        );
                    }

                    @Override
                    public void onDeleteReview(
                            Review review
                    ) {

                        confirmDeleteReview(
                                review
                        );
                    }
                }
        );

        if (rvPlaceReviews != null) {

            rvPlaceReviews.setLayoutManager(
                    new LinearLayoutManager(
                            requireContext()
                    )
            );

            rvPlaceReviews.setAdapter(
                    placeReviewAdapter
            );
        }

        showPlaceReviews(
                null
        );
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
                        view.findViewById(R.id.tab_home),
                        view.findViewById(R.id.tab_menu),
                        view.findViewById(R.id.tab_photo),
                        view.findViewById(R.id.tab_review1),
                        view.findViewById(R.id.tab_review2)
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

                        if (tab.getPosition() == 4) {

                            loadReview2List();
                        }
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

                        if (tab.getPosition() == 4) {

                            loadReview2List();
                        }
                    }
                }
        );

        showTabContent(
                0
        );
    }

    private void loadReview2List() {

        if (selectedPlace == null) {

            showPlaceReviews(
                    null
            );

            return;
        }

        try {

            long parsedPlaceId =
                    Long.parseLong(
                            selectedPlace.getId()
                    );

            if (detailManager != null) {

                detailManager.getReviews(
                        parsedPlaceId,
                        memberId,
                        reviews -> {

                            if (!isAdded()) {
                                return;
                            }

                            requireActivity().runOnUiThread(() ->
                                    showPlaceReviews(
                                            reviews
                                    )
                            );
                        }
                );
            }

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "placeId 변환 실패",
                    e
            );

            showPlaceReviews(
                    null
            );
        }
    }

    private void confirmDeleteReview(
            Review review
    ) {

        if (review == null
                || review.getReviewId() == null) {

            Toast.makeText(
                    requireContext(),
                    "삭제할 후기 정보가 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        new AlertDialog.Builder(
                requireContext()
        )
                .setTitle(
                        "후기 삭제"
                )
                .setMessage(
                        "이 후기를 삭제하시겠습니까?"
                )
                .setNegativeButton(
                        "취소",
                        null
                )
                .setPositiveButton(
                        "삭제",
                        (dialog, which) -> deleteReview(
                                review
                        )
                )
                .show();
    }

    private void deleteReview(
            Review review
    ) {

        if (review == null
                || review.getReviewId() == null) {

            return;
        }

        if (memberId == null
                || memberId <= 0) {

            Toast.makeText(
                    requireContext(),
                    "로그인 사용자 정보가 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        RetrofitClient
                .getApiService(
                        requireContext()
                )
                .deleteReview(
                        review.getReviewId(),
                        memberId
                )
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {

                                if (!isAdded()) {
                                    return;
                                }

                                if (response.isSuccessful()) {

                                    Toast.makeText(
                                            requireContext(),
                                            "후기가 삭제되었습니다.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    loadReview2List();

                                } else {

                                    Toast.makeText(
                                            requireContext(),
                                            "후기 삭제 실패 : "
                                                    + response.code(),
                                            Toast.LENGTH_SHORT
                                    ).show();
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable t
                            ) {

                                if (!isAdded()) {
                                    return;
                                }

                                Toast.makeText(
                                        requireContext(),
                                        "서버 연결 실패",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );
    }

    private void showPlaceReviews(
            List<Review> reviews
    ) {

        if (tvReview2Empty == null
                || rvPlaceReviews == null
                || placeReviewAdapter == null) {

            return;
        }

        if (reviews == null
                || reviews.isEmpty()) {

            tvReview2Empty.setVisibility(
                    View.VISIBLE
            );

            rvPlaceReviews.setVisibility(
                    View.GONE
            );

            placeReviewAdapter.setReviewList(
                    null
            );

            return;
        }

        tvReview2Empty.setVisibility(
                View.GONE
        );

        rvPlaceReviews.setVisibility(
                View.VISIBLE
        );

        placeReviewAdapter.setReviewList(
                reviews
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

            Log.e(
                    TAG,
                    "onItemClick vo null"
            );

            return;
        }

        if (isRecommendedMarkerVo(
                vo
        )) {

            Log.d(
                    TAG,
                    "추천 마커 클릭 → 카카오 재검색"
            );

            openRecommendedPlaceDetail(
                    vo
            );

            return;
        }

        openPlaceDetail(
                vo
        );
    }

    private boolean isRecommendedMarkerVo(
            KeywordMapVO vo
    ) {

        if (vo == null) {
            return false;
        }

        return vo.getRoad_address_name() == null
                || vo.getRoad_address_name().trim().isEmpty();
    }

    private void openRecommendedPlaceDetail(
            KeywordMapVO recommendedVo
    ) {

        if (recommendedVo == null
                || controller == null) {

            openPlaceDetail(
                    recommendedVo
            );

            return;
        }

        String keyword =
                recommendedVo.getPlace_name();

        if (keyword == null
                || keyword.trim().isEmpty()) {

            openPlaceDetail(
                    recommendedVo
            );

            return;
        }

        controller.searchMapData(
                keyword,
                new IThreadReturn1Callback<List<KeywordMapVO>>() {

                    @Override
                    public void ThreadEnds(
                            List<KeywordMapVO> result
                    ) {

                        if (!isAdded()) {
                            return;
                        }

                        requireActivity().runOnUiThread(() -> {

                            KeywordMapVO matchedVo =
                                    findMatchedPlace(
                                            recommendedVo,
                                            result
                                    );

                            openPlaceDetail(
                                    matchedVo == null
                                            ? recommendedVo
                                            : matchedVo
                            );
                        });
                    }

                    @Override
                    public void onError(
                            Exception e
                    ) {

                        Log.e(
                                TAG,
                                "추천 장소 재검색 실패",
                                e
                        );

                        if (!isAdded()) {
                            return;
                        }

                        requireActivity().runOnUiThread(() ->
                                openPlaceDetail(
                                        recommendedVo
                                )
                        );
                    }
                }
        );
    }

    private KeywordMapVO findMatchedPlace(
            KeywordMapVO recommendedVo,
            List<KeywordMapVO> result
    ) {

        if (recommendedVo == null
                || result == null
                || result.isEmpty()) {

            return null;
        }

        String targetId =
                recommendedVo.getId();

        String targetName =
                recommendedVo.getPlace_name();

        double targetLatitude =
                parseDoubleSafely(
                        recommendedVo.getY()
                );

        double targetLongitude =
                parseDoubleSafely(
                        recommendedVo.getX()
                );

        /*
         * 1순위:
         * placeId 일치
         */
        for (KeywordMapVO vo : result) {

            if (vo == null) {
                continue;
            }

            if (targetId != null
                    && targetId.equals(
                    vo.getId()
            )) {

                return vo;
            }
        }

        /*
         * 2순위:
         * 장소명 + 위도 + 경도 일치
         */
        for (KeywordMapVO vo : result) {

            if (vo == null) {
                continue;
            }

            String resultName =
                    vo.getPlace_name();

            double resultLatitude =
                    parseDoubleSafely(
                            vo.getY()
                    );

            double resultLongitude =
                    parseDoubleSafely(
                            vo.getX()
                    );

            if (targetName != null
                    && targetName.equals(
                    resultName
            )
                    && isSameCoordinate(
                    targetLatitude,
                    resultLatitude
            )
                    && isSameCoordinate(
                    targetLongitude,
                    resultLongitude
            )) {

                return vo;
            }
        }

        /*
         * 3순위:
         * 장소명 일치
         */
        for (KeywordMapVO vo : result) {

            if (vo == null) {
                continue;
            }

            if (targetName != null
                    && targetName.equals(
                    vo.getPlace_name()
            )) {

                return vo;
            }
        }

        /*
         * 마지막:
         * 검색 결과 첫 번째 항목
         */
        return result.get(0);
    }

    private void openPlaceDetail(
            KeywordMapVO vo
    ) {

        if (vo == null) {
            return;
        }

        selectedPlace =
                vo;

        Log.d(
                TAG,
                "상세페이지 열기 placeId = "
                        + vo.getId()
        );

        Log.d(
                TAG,
                "상세페이지 열기 placeName = "
                        + vo.getPlace_name()
        );

        if (detailPageManager != null) {

            detailPageManager.openDetailPage();
        }

        selectDetailTab(
                0
        );

        showPlaceReviews(
                null
        );

        if (detailManager != null) {

            detailManager.openDetail(
                    vo
            );
        }
    }

    public void openReviewWriteActivity() {

        Intent intent =
                new Intent(
                        requireContext(),
                        ReviewWriteActivity.class
                );

        if (selectedPlace != null) {

            try {

                long parsedPlaceId =
                        Long.parseLong(
                                selectedPlace.getId()
                        );

                intent.putExtra(
                        "placeId",
                        parsedPlaceId
                );

                intent.putExtra(
                        "placeName",
                        selectedPlace.getPlace_name()
                );

                intent.putExtra(
                        "latitude",
                        parseDoubleSafely(
                                selectedPlace.getY()
                        )
                );

                intent.putExtra(
                        "longitude",
                        parseDoubleSafely(
                                selectedPlace.getX()
                        )
                );

                intent.putExtra(
                        "memberId",
                        memberId
                );

            } catch (Exception e) {

                intent.putExtra(
                        "placeId",
                        -1L
                );

                intent.putExtra(
                        "placeName",
                        selectedPlace.getPlace_name()
                );

                intent.putExtra(
                        "latitude",
                        parseDoubleSafely(
                                selectedPlace.getY()
                        )
                );

                intent.putExtra(
                        "longitude",
                        parseDoubleSafely(
                                selectedPlace.getX()
                        )
                );

                intent.putExtra(
                        "memberId",
                        memberId
                );
            }
        }

        startActivity(
                intent
        );
    }

    private void openReviewEditActivity(
            Review review
    ) {

        if (review == null) {

            Toast.makeText(
                    requireContext(),
                    "후기 정보가 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        Intent intent =
                new Intent(
                        requireContext(),
                        ReviewWriteActivity.class
                );

        intent.putExtra(
                "isEditMode",
                true
        );

        intent.putExtra(
                "reviewId",
                review.getReviewId()
        );

        intent.putExtra(
                "placeId",
                review.getPlaceId()
        );

        intent.putExtra(
                "placeName",
                selectedPlace == null
                        ? ""
                        : selectedPlace.getPlace_name()
        );

        if (selectedPlace != null) {

            intent.putExtra(
                    "latitude",
                    parseDoubleSafely(
                            selectedPlace.getY()
                    )
            );

            intent.putExtra(
                    "longitude",
                    parseDoubleSafely(
                            selectedPlace.getX()
                    )
            );
        }

        intent.putExtra(
                "memberId",
                memberId
        );

        intent.putExtra(
                "rating",
                review.getRating()
        );

        intent.putExtra(
                "content",
                review.getContent()
        );

        intent.putExtra(
                "tags",
                review.getTags()
        );

        intent.putExtra(
                "revisit",
                review.isRevisit()
        );

        intent.putExtra(
                "imageUrl",
                review.getImageUrl()
        );

        startActivity(
                intent
        );
    }

    private double parseDoubleSafely(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return 0.0;
        }

        try {

            return Double.parseDouble(
                    value
            );

        } catch (Exception e) {

            return 0.0;
        }
    }

    private boolean isSameCoordinate(
            double a,
            double b
    ) {

        return Math.abs(
                a - b
        ) < 0.00001;
    }

    @Override
    public void onResume() {

        super.onResume();

        loadLoginMemberId();

        if (mapView != null) {

            mapView.resume();
        }

        if (tabDetail != null
                && tabDetail.getSelectedTabPosition() == 4) {

            loadReview2List();
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

        if (controller != null) {

            controller.clearRecommendedMarkers();
        }

        mapView = null;
        loadingView = null;

        tabDetail = null;
        tabContents = null;

        selectedPlace = null;

        rvPlaceReviews = null;
        tvReview2Empty = null;
        placeReviewAdapter = null;

        super.onDestroyView();
    }
}