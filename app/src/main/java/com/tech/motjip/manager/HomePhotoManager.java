package com.tech.motjip.manager;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.tech.motjip.R;

import java.util.List;

public class HomePhotoManager {

    /*
     * 사진 페이지 사이즈
     */
    private static final int PHOTO_PAGE_SIZE = 12;

    private final Fragment fragment;

    private final LayoutInflater inflater;

    /*
     * 사진 GridLayout
     */
    private final GridLayout glPhotoContainer;

    /*
     * 더보기 버튼
     */
    private final Button btnLoadMorePhoto;

    /*
     * 사진 없음 placeholder
     */
    private final TextView tvPhotoEmpty;

    /*
     * 현재 사진 URL 목록
     */
    private List<String> currentPhotoUrls;

    /*
     * 현재 로드된 개수
     */
    private int photoLoadedCount;

    public HomePhotoManager(
            Fragment fragment,
            View rootView
    ) {

        this.fragment = fragment;

        inflater =
                LayoutInflater.from(
                        fragment.requireContext()
                );

        glPhotoContainer =
                rootView.findViewById(
                        R.id.gl_photo_container
                );

        btnLoadMorePhoto =
                rootView.findViewById(
                        R.id.btn_load_more_photo
                );

        tvPhotoEmpty =
                rootView.findViewById(
                        R.id.tv_photo_empty
                );

        /*
         * 더보기 버튼 클릭
         */
        btnLoadMorePhoto.setOnClickListener(
                v -> loadMorePhotos()
        );
    }

    /*
     * 사진 세팅
     */
    public void setPhotos(
            List<String> photoUrls
    ) {

        currentPhotoUrls = photoUrls;

        /*
         * 초기화
         */
        photoLoadedCount = 0;

        glPhotoContainer.removeAllViews();

        /*
         * 사진 없음 처리
         */
        boolean photoEmpty =
                currentPhotoUrls == null
                        || currentPhotoUrls.isEmpty();

        tvPhotoEmpty.setVisibility(
                photoEmpty
                        ? View.VISIBLE
                        : View.GONE
        );

        /*
         * 사진 없으면 버튼 숨김
         */
        if (photoEmpty) {

            btnLoadMorePhoto.setVisibility(
                    View.GONE
            );

            return;
        }

        /*
         * 첫 로드
         */
        loadMorePhotos();
    }

    /*
     * 사진 추가 로드
     */
    public void loadMorePhotos() {

        if (currentPhotoUrls == null
                || currentPhotoUrls.isEmpty()) {

            btnLoadMorePhoto.setVisibility(
                    View.GONE
            );

            return;
        }

        /*
         * 다음 페이지 계산
         */
        int end =
                Math.min(
                        photoLoadedCount + PHOTO_PAGE_SIZE,
                        currentPhotoUrls.size()
                );

        /*
         * 사진 추가
         */
        for (int i = photoLoadedCount;
             i < end;
             i++) {

            View item =
                    inflater.inflate(
                            R.layout.item_photo,
                            glPhotoContainer,
                            false
                    );

            ImageView ivPhoto =
                    item.findViewById(
                            R.id.iv_photo
                    );

            Glide.with(fragment)
                    .load(currentPhotoUrls.get(i))
                    .into(ivPhoto);

            glPhotoContainer.addView(item);
        }

        /*
         * 현재 로드 개수 갱신
         */
        photoLoadedCount = end;

        /*
         * 남은 사진 여부
         */
        btnLoadMorePhoto.setVisibility(
                photoLoadedCount < currentPhotoUrls.size()
                        ? View.VISIBLE
                        : View.GONE
        );
    }

    /*
     * 사진 초기화
     */
    public void clear() {

        currentPhotoUrls = null;

        photoLoadedCount = 0;

        glPhotoContainer.removeAllViews();

        btnLoadMorePhoto.setVisibility(
                View.GONE
        );

        tvPhotoEmpty.setVisibility(
                View.GONE
        );
    }
}