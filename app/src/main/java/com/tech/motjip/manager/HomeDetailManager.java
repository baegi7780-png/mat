package com.tech.motjip.manager;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.tech.motjip.API.HttpHelper.GetJsonAsync;
import com.tech.motjip.Model.KeywordMapVO;
import com.tech.motjip.Model.PlaceDetailModel.MenuVO;
import com.tech.motjip.Model.PlaceDetailModel.PlaceDetailVO;
import com.tech.motjip.Model.PlaceDetailModel.ReviewVO;
import com.tech.motjip.R;
import com.tech.motjip.Thread.IThreadReturn1Callback;
import com.tech.motjip.WriteActivity;

import java.util.List;

public class HomeDetailManager {

    private static final String TAG = "DETAIL_MANAGER_DEBUG";

    private final Fragment fragment;
    private final LayoutInflater inflater;
    private final View detailPage;

    private final TextView tvDetailTitle;
    private final TextView tvHomeStatus;
    private final TextView tvHomeAddress;
    private final TextView tvHomePhone;

    private final Button btnWritePlacePost;

    private final LinearLayout llMenuContainer;
    private final GridLayout glPhotoContainer;
    private final LinearLayout llReviewContainer;

    private final TextView tvMenuEmpty;
    private final TextView tvPhotoEmpty;
    private final TextView tvReviewEmpty;

    private final View viewDetailLoading;
    private final Button btnLoadMorePhoto;

    private List<String> currentPhotoUrls;
    private int photoLoadedCount;

    private static final int PHOTO_PAGE_SIZE = 12;

    public HomeDetailManager(
            Fragment fragment,
            View rootView
    ) {

        this.fragment = fragment;

        inflater =
                LayoutInflater.from(
                        fragment.requireContext()
                );

        detailPage =
                rootView.findViewById(
                        R.id.detail_page
                );

        tvDetailTitle =
                rootView.findViewById(
                        R.id.tv_detail_title
                );

        tvHomeStatus =
                rootView.findViewById(
                        R.id.tv_home_status
                );

        tvHomeAddress =
                rootView.findViewById(
                        R.id.tv_home_address
                );

        tvHomePhone =
                rootView.findViewById(
                        R.id.tv_home_phone
                );

        btnWritePlacePost =
                rootView.findViewById(
                        R.id.btn_write_place_post
                );

        llMenuContainer =
                rootView.findViewById(
                        R.id.ll_menu_container
                );

        glPhotoContainer =
                rootView.findViewById(
                        R.id.gl_photo_container
                );

        llReviewContainer =
                rootView.findViewById(
                        R.id.ll_review_container
                );

        tvMenuEmpty =
                rootView.findViewById(
                        R.id.tv_menu_empty
                );

        tvPhotoEmpty =
                rootView.findViewById(
                        R.id.tv_photo_empty
                );

        tvReviewEmpty =
                rootView.findViewById(
                        R.id.tv_review_empty
                );

        viewDetailLoading =
                rootView.findViewById(
                        R.id.view_detail_loading
                );

        btnLoadMorePhoto =
                rootView.findViewById(
                        R.id.btn_load_more_photo
                );

        btnLoadMorePhoto.setOnClickListener(
                v -> {
                    Log.d(TAG, "btnLoadMorePhoto 클릭");
                    loadMorePhotos();
                }
        );

        Log.d(TAG, "HomeDetailManager 생성 완료");
        printViewState("constructor");
    }

    public void openDetail(
            KeywordMapVO vo
    ) {

        Log.d(TAG, "openDetail() 호출");

        if (vo == null) {
            Log.e(TAG, "openDetail() vo == null");
            return;
        }

        Log.d(TAG, "placeName = " + vo.getPlace_name());
        Log.d(TAG, "placeUrl = " + vo.getPlace_url());
        Log.d(TAG, "address = " + vo.getRoad_address_name());

        printViewState("openDetail before set");

        tvDetailTitle.setText(
                vo.getPlace_name()
        );

        tvHomeAddress.setText(
                vo.getRoad_address_name()
        );

        detailPage.setVisibility(
                View.VISIBLE
        );

        detailPage.bringToFront();

        Log.d(TAG, "detailPage = VISIBLE + bringToFront()");
        printViewState("openDetail after detail visible");

        btnWritePlacePost.setOnClickListener(v -> {

            Log.d(TAG, "글쓰기 버튼 클릭");

            Intent intent =
                    new Intent(
                            fragment.requireContext(),
                            WriteActivity.class
                    );

            intent.putExtra(
                    "placeName",
                    vo.getPlace_name()
            );

            intent.putExtra(
                    "placeAddress",
                    vo.getRoad_address_name()
            );

            fragment.startActivity(intent);
        });

        viewDetailLoading.setVisibility(
                View.VISIBLE
        );

        viewDetailLoading.bringToFront();

        Log.d(TAG, "viewDetailLoading = VISIBLE + bringToFront()");

        clearDetailViews();

        Log.d(TAG, "GetPlaceDetailAsync 호출 시작");

        GetJsonAsync.GetPlaceDetailAsync(
                vo.getPlace_url(),
                new IThreadReturn1Callback<PlaceDetailVO>() {

                    @Override
                    public void ThreadEnds(
                            PlaceDetailVO detail
                    ) {

                        Log.d(TAG, "GetPlaceDetailAsync ThreadEnds()");

                        if (!fragment.isAdded()) {
                            Log.e(TAG, "fragment is not added - return");
                            return;
                        }

                        if (detail == null) {
                            Log.e(TAG, "detail == null");

                            fragment.requireActivity()
                                    .runOnUiThread(() -> {
                                        viewDetailLoading.setVisibility(
                                                View.GONE
                                        );

                                        printViewState("detail null");
                                    });

                            return;
                        }

                        Log.d(TAG, "detail load success");
                        Log.d(TAG, "openDisplay = " + detail.getOpenDisplay());
                        Log.d(TAG, "openTimeText = " + detail.getOpenTimeText());
                        Log.d(TAG, "phoneNumber = " + detail.getPhoneNumber());

                        Log.d(
                                TAG,
                                "menu count = "
                                        + (
                                        detail.getMenus() == null
                                                ? 0
                                                : detail.getMenus().size()
                                )
                        );

                        Log.d(
                                TAG,
                                "photo count = "
                                        + (
                                        detail.getPhotoUrls() == null
                                                ? 0
                                                : detail.getPhotoUrls().size()
                                )
                        );

                        Log.d(
                                TAG,
                                "review count = "
                                        + (
                                        detail.getReviews() == null
                                                ? 0
                                                : detail.getReviews().size()
                                )
                        );

                        fragment.requireActivity()
                                .runOnUiThread(() -> {

                                    Log.d(TAG, "UI bind 시작");

                                    bindHome(detail);
                                    bindMenus(detail);
                                    bindPhotos(detail);
                                    bindReviews(detail);

                                    viewDetailLoading.setVisibility(
                                            View.GONE
                                    );

                                    detailPage.bringToFront();

                                    Log.d(TAG, "viewDetailLoading = GONE");
                                    Log.d(TAG, "detailPage bringToFront() after loading");

                                    printViewState("after bind detail");
                                });
                    }

                    @Override
                    public void onError(
                            Exception e
                    ) {

                        Log.e(
                                TAG,
                                "상세 로딩 실패",
                                e
                        );

                        if (!fragment.isAdded()) {
                            Log.e(TAG, "fragment is not added onError - return");
                            return;
                        }

                        fragment.requireActivity()
                                .runOnUiThread(() -> {
                                    viewDetailLoading.setVisibility(
                                            View.GONE
                                    );

                                    detailPage.bringToFront();

                                    printViewState("onError");
                                });
                    }
                }
        );
    }

    private void clearDetailViews() {

        Log.d(TAG, "clearDetailViews()");

        tvHomeStatus.setVisibility(
                View.GONE
        );

        tvHomePhone.setVisibility(
                View.GONE
        );

        llMenuContainer.removeAllViews();
        glPhotoContainer.removeAllViews();
        llReviewContainer.removeAllViews();

        tvMenuEmpty.setVisibility(
                View.GONE
        );

        tvPhotoEmpty.setVisibility(
                View.GONE
        );

        tvReviewEmpty.setVisibility(
                View.GONE
        );

        btnLoadMorePhoto.setVisibility(
                View.GONE
        );

        currentPhotoUrls = null;
        photoLoadedCount = 0;

        printViewState("after clearDetailViews");
    }

    private void bindHome(
            PlaceDetailVO detail
    ) {

        Log.d(TAG, "bindHome()");

        if (detail.getOpenDisplay() != null) {

            tvHomeStatus.setText(
                    detail.getOpenDisplay()
                            + " · "
                            + detail.getOpenTimeText()
            );

            tvHomeStatus.setVisibility(
                    View.VISIBLE
            );

        } else {

            tvHomeStatus.setVisibility(
                    View.GONE
            );
        }

        if (detail.getPhoneNumber() != null) {

            tvHomePhone.setText(
                    detail.getPhoneNumber()
            );

            tvHomePhone.setVisibility(
                    View.VISIBLE
            );

        } else {

            tvHomePhone.setVisibility(
                    View.GONE
            );
        }
    }

    private void bindMenus(
            PlaceDetailVO detail
    ) {

        Log.d(TAG, "bindMenus()");

        llMenuContainer.removeAllViews();

        boolean menuEmpty =
                detail.getMenus() == null
                        || detail.getMenus().isEmpty();

        Log.d(TAG, "menuEmpty = " + menuEmpty);

        tvMenuEmpty.setVisibility(
                menuEmpty
                        ? View.VISIBLE
                        : View.GONE
        );

        if (detail.getMenus() == null) {
            Log.d(TAG, "menus == null");
            return;
        }

        for (MenuVO menu : detail.getMenus()) {

            View item =
                    inflater.inflate(
                            R.layout.item_menu,
                            llMenuContainer,
                            false
                    );

            ((TextView) item.findViewById(
                    R.id.tv_menu_name
            )).setText(
                    menu.getMenuName()
            );

            TextView tvMenuPrice =
                    item.findViewById(
                            R.id.tv_menu_price
                    );

            if (menu.getMenuPrice() > 0) {

                tvMenuPrice.setText(
                        String.format(
                                "%,d원",
                                menu.getMenuPrice()
                        )
                );

            } else {

                tvMenuPrice.setText(
                        "가격정보없음"
                );
            }

            llMenuContainer.addView(item);
        }

        Log.d(TAG, "menu view count = " + llMenuContainer.getChildCount());
    }

    private void bindPhotos(
            PlaceDetailVO detail
    ) {

        Log.d(TAG, "bindPhotos()");

        currentPhotoUrls =
                detail.getPhotoUrls();

        photoLoadedCount = 0;

        glPhotoContainer.removeAllViews();

        boolean photoEmpty =
                currentPhotoUrls == null
                        || currentPhotoUrls.isEmpty();

        Log.d(TAG, "photoEmpty = " + photoEmpty);

        tvPhotoEmpty.setVisibility(
                photoEmpty
                        ? View.VISIBLE
                        : View.GONE
        );

        loadMorePhotos();
    }

    private void loadMorePhotos() {

        Log.d(TAG, "loadMorePhotos()");

        if (currentPhotoUrls == null
                || currentPhotoUrls.isEmpty()) {

            Log.d(TAG, "사진 없음 - 더보기 숨김");

            btnLoadMorePhoto.setVisibility(
                    View.GONE
            );

            return;
        }

        int end =
                Math.min(
                        photoLoadedCount + PHOTO_PAGE_SIZE,
                        currentPhotoUrls.size()
                );

        Log.d(
                TAG,
                "photo load range = "
                        + photoLoadedCount
                        + " ~ "
                        + end
        );

        for (int i = photoLoadedCount;
             i < end;
             i++) {

            View item =
                    inflater.inflate(
                            R.layout.item_photo,
                            glPhotoContainer,
                            false
                    );

            ImageView iv =
                    item.findViewById(
                            R.id.iv_photo
                    );

            Glide.with(fragment)
                    .load(currentPhotoUrls.get(i))
                    .into(iv);

            glPhotoContainer.addView(item);
        }

        photoLoadedCount = end;

        btnLoadMorePhoto.setVisibility(
                photoLoadedCount < currentPhotoUrls.size()
                        ? View.VISIBLE
                        : View.GONE
        );

        Log.d(
                TAG,
                "photoLoadedCount = "
                        + photoLoadedCount
                        + ", button visibility = "
                        + visibilityToString(
                        btnLoadMorePhoto.getVisibility()
                )
        );
    }

    private void bindReviews(
            PlaceDetailVO detail
    ) {

        Log.d(TAG, "bindReviews()");

        llReviewContainer.removeAllViews();

        boolean reviewEmpty =
                detail.getReviews() == null
                        || detail.getReviews().isEmpty();

        Log.d(TAG, "reviewEmpty = " + reviewEmpty);

        tvReviewEmpty.setVisibility(
                reviewEmpty
                        ? View.VISIBLE
                        : View.GONE
        );

        if (detail.getReviews() == null) {
            Log.d(TAG, "reviews == null");
            return;
        }

        for (ReviewVO review
                : detail.getReviews()) {

            View item =
                    inflater.inflate(
                            R.layout.item_review,
                            llReviewContainer,
                            false
                    );

            ImageView ivProfile =
                    item.findViewById(
                            R.id.iv_review_profile
                    );

            Glide.with(fragment)
                    .load(review.getProfileImageUrl())
                    .circleCrop()
                    .into(ivProfile);

            ((TextView) item.findViewById(
                    R.id.tv_review_nickname
            )).setText(
                    review.getNickname()
            );

            ((TextView) item.findViewById(
                    R.id.tv_review_user_meta
            )).setText(
                    String.format(
                            "후기 %d개 · 평균 ★%.1f",
                            review.getReviewCount(),
                            review.getAverageScore()
                    )
            );

            ((TextView) item.findViewById(
                    R.id.tv_review_rating
            )).setText(
                    "★ "
                            + review.getStarRating()
            );

            ((TextView) item.findViewById(
                    R.id.tv_review_date
            )).setText(
                    review.getUpdatedAt()
            );

            ((TextView) item.findViewById(
                    R.id.tv_review_contents
            )).setText(
                    review.getContents()
            );

            llReviewContainer.addView(item);
        }

        Log.d(TAG, "review view count = " + llReviewContainer.getChildCount());
    }

    private void printViewState(
            String from
    ) {

        Log.d(
                TAG,
                "["
                        + from
                        + "] detailPage="
                        + (
                        detailPage == null
                                ? "null"
                                : visibilityToString(
                                detailPage.getVisibility()
                        )
                )
                        + ", loading="
                        + (
                        viewDetailLoading == null
                                ? "null"
                                : visibilityToString(
                                viewDetailLoading.getVisibility()
                        )
                )
                        + ", menuContainerChild="
                        + (
                        llMenuContainer == null
                                ? "null"
                                : llMenuContainer.getChildCount()
                )
                        + ", photoContainerChild="
                        + (
                        glPhotoContainer == null
                                ? "null"
                                : glPhotoContainer.getChildCount()
                )
                        + ", reviewContainerChild="
                        + (
                        llReviewContainer == null
                                ? "null"
                                : llReviewContainer.getChildCount()
                )
        );
    }

    private String visibilityToString(
            int visibility
    ) {

        if (visibility == View.VISIBLE) {
            return "VISIBLE";
        }

        if (visibility == View.INVISIBLE) {
            return "INVISIBLE";
        }

        if (visibility == View.GONE) {
            return "GONE";
        }

        return "UNKNOWN(" + visibility + ")";
    }
}