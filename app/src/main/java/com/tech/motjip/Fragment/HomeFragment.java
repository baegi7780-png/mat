package com.tech.motjip.Fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapView;
import com.tech.motjip.API.HttpHelper.GetJsonAsync;
import com.tech.motjip.API.KakaoMap.CallbackInterface.IViewDetailItemClickCallback;
import com.tech.motjip.Controller.TestController;
import com.tech.motjip.Model.KeywordMapVO;
import com.tech.motjip.Model.PlaceDetailModel.MenuVO;
import com.tech.motjip.Model.PlaceDetailModel.PlaceDetailVO;
import com.tech.motjip.Model.PlaceDetailModel.ReviewVO;
import com.tech.motjip.R;
import com.tech.motjip.Thread.IThreadCallback;
import com.tech.motjip.Thread.IThreadReturn1Callback;

import java.util.List;

public class HomeFragment extends Fragment implements IViewDetailItemClickCallback {

    // GPS 좌표 요청 코드
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    TestController controller;
    private IThreadCallback callback;

    private MapView mapView;
    private EditText etSearch;
    private Button btnSearch;
    private Button btnToggle;
    private View overlaySuggestion;
    private LinearLayout suggestionContainer;
    private LinearLayout resultContainer;
    private View loadingView;
    private LayoutInflater inflater;
    private Handler debounceHandler;
    private View detailPage;
    private TextView tvDetailTitle;
    private Button btnDetailClose;
    private View bottomSheet;
    private TabLayout tabDetail;
    private View[] tabContents;
    // 홈 탭 콘텐츠
    private TextView tvHomeStatus;
    private TextView tvHomeAddress;
    private TextView tvHomePhone;
    // 메뉴 탭 컨테이너
    private LinearLayout llMenuContainer;
    // 사진 탭
    private GridLayout glPhotoContainer;
    private Button btnLoadMorePhoto;
    private List<String> currentPhotoUrls;  // 현재 가게의 전체 사진 URL
    private int photoLoadedCount;            // 현재까지 로드한 사진 수
    private static final int PHOTO_PAGE_SIZE = 12;
    // 리뷰1 탭 컨테이너
    private LinearLayout llReviewContainer;
    // 빈 상태 placeholder
    private TextView tvMenuEmpty;
    private TextView tvPhotoEmpty;
    private TextView tvReviewEmpty;
    // 상세페이지 로딩 뷰
    private View viewDetailLoading;

    public HomeFragment() {}

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle savedInstanceState
    ) {
        super.onViewCreated(view, savedInstanceState);

        controller = new TestController(requireActivity());

        mapView = view.findViewById(R.id.map_view);
        etSearch = view.findViewById(R.id.et_search);
        btnSearch = view.findViewById(R.id.btn_search);
        btnToggle = view.findViewById(R.id.btn_toggle_suggestion);
        overlaySuggestion = view.findViewById(R.id.overlay_suggestion);
        suggestionContainer = view.findViewById(R.id.ll_suggestion_container);
        resultContainer = view.findViewById(R.id.ll_search_result_container);
        loadingView = view.findViewById(R.id.view_loading);
        detailPage = view.findViewById(R.id.detail_page);
        tvDetailTitle = view.findViewById(R.id.tv_detail_title);
        btnDetailClose = view.findViewById(R.id.btn_detail_close);
        bottomSheet = view.findViewById(R.id.bottom_sheet);
        tvHomeStatus = view.findViewById(R.id.tv_home_status);
        tvHomeAddress = view.findViewById(R.id.tv_home_address);
        tvHomePhone = view.findViewById(R.id.tv_home_phone);
        llMenuContainer = view.findViewById(R.id.ll_menu_container);
        glPhotoContainer = view.findViewById(R.id.gl_photo_container);
        btnLoadMorePhoto = view.findViewById(R.id.btn_load_more_photo);
        btnLoadMorePhoto.setOnClickListener(v -> loadMorePhotos());
        llReviewContainer = view.findViewById(R.id.ll_review_container);
        tvMenuEmpty = view.findViewById(R.id.tv_menu_empty);
        tvPhotoEmpty = view.findViewById(R.id.tv_photo_empty);
        tvReviewEmpty = view.findViewById(R.id.tv_review_empty);
        viewDetailLoading = view.findViewById(R.id.view_detail_loading);
        tabDetail = view.findViewById(R.id.tab_detail);
        tabContents = new View[]{
                view.findViewById(R.id.tab_home),
                view.findViewById(R.id.tab_menu),
                view.findViewById(R.id.tab_photo),
                view.findViewById(R.id.tab_review1),
                view.findViewById(R.id.tab_review2)
        };
        // 탭 클릭 시 해당 콘텐츠만 보이게
        tabDetail.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                for (int i = 0; i < tabContents.length; i++) {
                    tabContents[i].setVisibility(i == tab.getPosition() ? View.VISIBLE : View.GONE);
                }
            }
            @Override public void onTabUnselected(TabLayout.Tab tab) {}
            @Override public void onTabReselected(TabLayout.Tab tab) {}
        });
        inflater = LayoutInflater.from(requireContext());

        // 검색 관련 처리 콜백 여기서 처리
        IThreadReturn1Callback<List<KeywordMapVO>> searchCallback =
                controller.createSearchCallback(resultContainer, suggestionContainer, overlaySuggestion, inflater,
                        this);

        // 지도 로드 될때 처리
        callback = new IThreadCallback() {
            @Override
            public void ThreadEnds() {
                // 버튼으로 검색 오버레이 처리
                btnToggle.setOnClickListener(v -> {
                    if (overlaySuggestion.getVisibility() == View.VISIBLE) {
                        closeSearchOverlay();
                    } else {
                        openSearchOverlay();
                    }
                });

                // 검색창 클릭시 오버레이 처리
                etSearch.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) openSearchOverlay();
                });
                etSearch.setOnClickListener(v -> openSearchOverlay());

                // 입력이 멈춘 뒤 일정 시간이 지났을 때 검색을 실행하는 디바운스 핸들러
                // Runnable을 배열로 감싸야 객체가 바뀌지 않음 정적 배열은 한번 할당하면 객체가 안바뀜
                debounceHandler = new Handler(Looper.getMainLooper());
                Runnable[] debounceRunnable = {null};

                // 텍스트 체인지이벤트 처리
                etSearch.addTextChangedListener(new TextWatcher() {
                    @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        // 이전에 예약된 검색 요청 취소 (연속 입력 시 중복 API 호출 방지)
                        if (debounceRunnable[0] != null) {
                            debounceHandler.removeCallbacks(debounceRunnable[0]);
                        }

                        String keyword = s.toString().trim();

                        if (keyword.isEmpty()) {
                            suggestionContainer.removeAllViews();
                            return;
                        }

                        // 0.5초 후 실행 예약 — 그 사이 입력이 오면 위에서 취소되고 다시 예약됨
                        debounceRunnable[0] = () -> controller.searchMapData(keyword, new IThreadReturn1Callback<List<KeywordMapVO>>() {
                            @Override
                            public void ThreadEnds(List<KeywordMapVO> result) {
                                if (!isAdded()) return; // Fragment 살아있는지 검증
                                requireActivity().runOnUiThread(() -> {
                                    suggestionContainer.removeAllViews();
                                    for (KeywordMapVO vo : result) {
                                        View item = inflater.inflate(R.layout.item_suggestion, suggestionContainer, false);
                                        ((TextView) item.findViewById(R.id.tv_suggestion_name)).setText(vo.getPlace_name());
                                        // 검색 리스트 아이템에 온클릭 이벤트 처리(검색)
                                        item.setOnClickListener(v -> doSearch(vo.getPlace_name(), searchCallback));
                                        suggestionContainer.addView(item);
                                    }
                                });
                            }

                            @Override
                            public void onError(Exception e) {
                                Log.e("Suggestion", "검색 오류", e);
                            }
                        });
                        // 0.5초 단위로 처리
                        debounceHandler.postDelayed(debounceRunnable[0], 500);
                    }

                    @Override public void afterTextChanged(Editable s) {}
                });

                // 검색버튼 처리
                btnSearch.setOnClickListener(v -> {
                    String keyword = etSearch.getText().toString().trim();
                    if (keyword.isEmpty()) return;
                    doSearch(keyword, searchCallback);
                });

                // 맵 로딩 완료 후 로딩 뷰 제거
                loadingView.setVisibility(View.GONE);

                // 마커 클릭 이벤트 등록 — 리스트 클릭과 동일하게 onItemClick 호출
                controller.setMarkerClickListener(HomeFragment.this);

                // 상세페이지 닫기 버튼 처리
                btnDetailClose.setOnClickListener(v -> closeDetailPage());
            }
        };

        // 맵뷰 로드되고나서 GPS 권한 확인 후 맵 로딩
        mapView.post(() -> getGPSPosition());
    }

    // 입력값에 따른 검색을 수행합니다.
    private void doSearch(String keyword, IThreadReturn1Callback<List<KeywordMapVO>> callback) {
        // 검색 시작 시 상세페이지가 열려있으면 닫기
        if (detailPage.getVisibility() == View.VISIBLE) {
            closeDetailPage();
        }
        ((InputMethodManager) requireActivity().getSystemService(InputMethodManager.class)).hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
        controller.searchMapData(keyword, callback);
    }

    // 상세페이지 닫기 처리 (닫기 버튼과 검색 시 공통 사용)
    private void closeDetailPage() {
        detailPage.setVisibility(View.GONE);
        bottomSheet.setVisibility(View.VISIBLE);
        // 다음에 다시 열릴 때 홈 탭부터 시작하도록 초기화
        TabLayout.Tab homeTab = tabDetail.getTabAt(0);
        if (homeTab != null) homeTab.select();
    }

    // GPS와 관련된 권한을 처리합니다.
    private void getGPSPosition() {
        // 만약 GPS 권한이 없다면
        if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // 권한 요청을 시도하고 함수를 종료합니다.
            requestPermissions(
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }
        // 권한이 있다면 맵이동 처리 관련 로직을 수행합니다.
        fetchLocationAndStartMap();
    }

    // 권한 요청에 대한 처리를 시도합니다.
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // GPS권한 요청이라면
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            // 사용자가 허용했다면
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //GPS 기반으로 맵 시작
                fetchLocationAndStartMap();
            } else {
                // 거부했다면 기본 좌표로 맵 시작
                controller.mapStart(mapView, callback);
            }
        }
    }

    // GPS 좌표에 따른 맵 이동을 처리합니다.
    private void fetchLocationAndStartMap() {
        LocationManager lm = (LocationManager) requireActivity().getSystemService(LocationManager.class);
        controller.fetchLocation(lm, new IThreadReturn1Callback<LatLng>() {
            @Override
            public void ThreadEnds(LatLng latLng) {
                //if (!isAdded()) return; // Fragment 살아있는지 검증
                // 성공했을경우 맵로드 가 GPS 기반 좌표값에 따라 이동됨
                controller.mapStart(mapView, callback, latLng);
            }

            @Override
            public void onError(Exception e) {
                Log.d("test","log");
                //if (!isAdded()) return; // Fragment 살아있는지 검증
                // 실패했을 경우 기본 좌표 사용
                controller.mapStart(mapView, callback);
            }
        });
    }

    private void openSearchOverlay() {
        if (overlaySuggestion.getVisibility() == View.VISIBLE) return; // 이미 열려있으면 무시
        overlaySuggestion.setVisibility(View.VISIBLE);
        btnToggle.setText("▲");
    }

    private void closeSearchOverlay() {
        overlaySuggestion.setVisibility(View.GONE);
        btnToggle.setText("▼");
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.resume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.pause();
    }

    // 사진을 PHOTO_PAGE_SIZE(12)개씩 추가로 로드합니다.
    private void loadMorePhotos() {
        if (currentPhotoUrls == null || currentPhotoUrls.isEmpty()) {
            btnLoadMorePhoto.setVisibility(View.GONE);
            return;
        }
        int end = Math.min(photoLoadedCount + PHOTO_PAGE_SIZE, currentPhotoUrls.size());
        for (int i = photoLoadedCount; i < end; i++) {
            View item = inflater.inflate(R.layout.item_photo, glPhotoContainer, false);
            ImageView iv = item.findViewById(R.id.iv_photo);
            Glide.with(this).load(currentPhotoUrls.get(i)).into(iv);
            glPhotoContainer.addView(item);
        }
        photoLoadedCount = end;
        // 다 보여줬으면 더보기 버튼 숨김
        btnLoadMorePhoto.setVisibility(photoLoadedCount < currentPhotoUrls.size() ? View.VISIBLE : View.GONE);
    }

    // 바텀시트 리스트 아이템 클릭 시 호출 — 상세페이지 UI 처리 일체
    @Override
    public void onItemClick(KeywordMapVO vo) {
        // 상세페이지 열기 + 가게명/주소 즉시 세팅
        tvDetailTitle.setText(vo.getPlace_name());
        tvHomeAddress.setText(vo.getRoad_address_name());
        detailPage.setVisibility(View.VISIBLE);
        bottomSheet.setVisibility(View.GONE);

        // 로딩 뷰 표시
        viewDetailLoading.setVisibility(View.VISIBLE);

        // PlaceDetailVO 비동기 호출 — 영업/전화번호 데이터 채우기
        GetJsonAsync.GetPlaceDetailAsync(vo.getPlace_url(), new IThreadReturn1Callback<PlaceDetailVO>() {
            @Override
            public void ThreadEnds(PlaceDetailVO detail) {
                if (!isAdded()) return; // Fragment 살아있는지 검증
                requireActivity().runOnUiThread(() -> {
                    tvHomeStatus.setText(detail.getOpenDisplay() + " · " + detail.getOpenTimeText());
                    tvHomePhone.setText(detail.getPhoneNumber());

                    // 메뉴 탭 채우기 (동적 추가)
                    llMenuContainer.removeAllViews();
                    // 메뉴 0개면 placeholder 표시
                    boolean menuEmpty = (detail.getMenus() == null || detail.getMenus().isEmpty());
                    tvMenuEmpty.setVisibility(menuEmpty ? View.VISIBLE : View.GONE);
                    if (detail.getMenus() != null) {
                        for (MenuVO menu : detail.getMenus()) {
                            View item = inflater.inflate(R.layout.item_menu, llMenuContainer, false);
                            ((TextView) item.findViewById(R.id.tv_menu_name)).setText(menu.getMenuName());
                            // 가격이 유효한 경우만 표시 (0 이하면 텍스트뷰 숨김)
                            TextView tvMenuPrice = item.findViewById(R.id.tv_menu_price);
                            if (menu.getMenuPrice() > 0) {
                                tvMenuPrice.setText(String.format("%,d원", menu.getMenuPrice()));
                            } else {
                                tvMenuPrice.setText("가격정보없음");
                            }
                            llMenuContainer.addView(item);
                        }
                    }

                    // 사진 탭 초기화 + 첫 12개 로드
                    currentPhotoUrls = detail.getPhotoUrls();
                    photoLoadedCount = 0;
                    glPhotoContainer.removeAllViews();
                    // 사진 0개면 placeholder 표시
                    boolean photoEmpty = (currentPhotoUrls == null || currentPhotoUrls.isEmpty());
                    tvPhotoEmpty.setVisibility(photoEmpty ? View.VISIBLE : View.GONE);
                    loadMorePhotos();

                    // 리뷰1 탭 채우기 (동적 추가)
                    llReviewContainer.removeAllViews();
                    // 리뷰 0개면 placeholder 표시
                    boolean reviewEmpty = (detail.getReviews() == null || detail.getReviews().isEmpty());
                    tvReviewEmpty.setVisibility(reviewEmpty ? View.VISIBLE : View.GONE);
                    if (detail.getReviews() != null) {
                        for (ReviewVO review : detail.getReviews()) {
                            View item = inflater.inflate(R.layout.item_review, llReviewContainer, false);
                            // 프로필 이미지 (둥글게)
                            ImageView ivProfile = item.findViewById(R.id.iv_review_profile);
                            Glide.with(HomeFragment.this).load(review.getProfileImageUrl()).circleCrop().into(ivProfile);
                            // 닉네임
                            ((TextView) item.findViewById(R.id.tv_review_nickname)).setText(review.getNickname());
                            // 후기 개수 · 평균 별점
                            ((TextView) item.findViewById(R.id.tv_review_user_meta)).setText(
                                    String.format("후기 %d개 · 평균 ★%.1f", review.getReviewCount(), review.getAverageScore()));
                            // 별점
                            ((TextView) item.findViewById(R.id.tv_review_rating)).setText("★ " + review.getStarRating());
                            // 날짜
                            ((TextView) item.findViewById(R.id.tv_review_date)).setText(review.getUpdatedAt());
                            // 리뷰 본문
                            ((TextView) item.findViewById(R.id.tv_review_contents)).setText(review.getContents());
                            llReviewContainer.addView(item);
                        }
                    }

                    viewDetailLoading.setVisibility(View.GONE);
                });
            }

            @Override
            public void onError(Exception e) {
                if (!isAdded()) return; // Fragment 살아있는지 검증
                Log.e("PlaceDetail", "상세 로딩 실패", e);
                requireActivity().runOnUiThread(() -> viewDetailLoading.setVisibility(View.GONE));
            }
        });
    }

    @Override
    public void onDestroyView() {
        // controller.onDestroy();
        if (debounceHandler != null) {
            debounceHandler.removeCallbacksAndMessages(null); // 예약된 검색 요청 전부 취소
        }
        super.onDestroyView();
    }
}
