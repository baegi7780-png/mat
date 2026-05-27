package com.tech.motjip;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.tech.motjip.manager.ReviewImagePickerManager;
import com.tech.motjip.manager.ReviewSubmitManager;
import com.tech.motjip.manager.ReviewTagManager;
import com.tech.motjip.manager.ReviewWriteRequestBuilder;
import com.tech.motjip.manager.ReviewWriteValidator;

import okhttp3.MultipartBody;

public class ReviewWriteActivity extends AppCompatActivity {

    private static final String TAG =
            "ReviewWriteDebug";

    private TextView tvPlaceName;
    private RatingBar ratingBar;
    private CheckBox cbRevisit;
    private EditText etReviewContent;
    private Button btnSubmitReview;

    private Button btnTaste;
    private Button btnCost;
    private Button btnKind;
    private Button btnMood;
    private Button btnParking;

    private Button btnAddPhoto;
    private ImageView ivPreviewImage;

    private Long placeId;
    private Long memberId;
    private String placeName;

    private Double latitude;
    private Double longitude;

    private boolean isEditMode =
            false;

    private Long reviewId =
            -1L;

    private ReviewTagManager reviewTagManager;
    private ReviewImagePickerManager reviewImagePickerManager;
    private ReviewWriteValidator reviewWriteValidator;
    private ReviewSubmitManager reviewSubmitManager;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.GetContent(),
                    uri -> {

                        if (reviewImagePickerManager != null) {

                            reviewImagePickerManager.setSelectedImageUri(
                                    uri
                            );
                        }
                    }
            );

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_review_write
        );

        Log.d(
                TAG,
                "ReviewWriteActivity 시작"
        );

        placeId =
                getIntent().getLongExtra(
                        "placeId",
                        -1
                );

        placeName =
                getIntent().getStringExtra(
                        "placeName"
                );

        latitude =
                getIntent().getDoubleExtra(
                        "latitude",
                        0.0
                );

        longitude =
                getIntent().getDoubleExtra(
                        "longitude",
                        0.0
                );

        memberId =
                getIntent().getLongExtra(
                        "memberId",
                        -1
                );

        if (memberId == -1) {

            SharedPreferences prefs =
                    getSharedPreferences(
                            "auth",
                            MODE_PRIVATE
                    );

            memberId =
                    prefs.getLong(
                            "memberId",
                            -1
                    );

            Log.d(
                    TAG,
                    "SharedPreferences memberId 로드 = "
                            + memberId
            );

        } else {

            Log.d(
                    TAG,
                    "Intent memberId 로드 = "
                            + memberId
            );
        }

        Log.d(
                TAG,
                "placeId = "
                        + placeId
        );

        Log.d(
                TAG,
                "placeName = "
                        + placeName
        );

        Log.d(
                TAG,
                "latitude = "
                        + latitude
        );

        Log.d(
                TAG,
                "longitude = "
                        + longitude
        );

        Log.d(
                TAG,
                "memberId = "
                        + memberId
        );

        isEditMode =
                getIntent().getBooleanExtra(
                        "isEditMode",
                        false
                );

        reviewId =
                getIntent().getLongExtra(
                        "reviewId",
                        -1L
                );

        Log.d(
                TAG,
                "isEditMode = "
                        + isEditMode
        );

        Log.d(
                TAG,
                "reviewId = "
                        + reviewId
        );

        initViews();
        initPlaceName();
        initTagButtons();
        initPhotoButton();
        initEditMode();
        initSubmitButton();
    }

    private void initViews() {

        tvPlaceName =
                findViewById(
                        R.id.tvPlaceName
                );

        ratingBar =
                findViewById(
                        R.id.ratingBar
                );

        cbRevisit =
                findViewById(
                        R.id.cbRevisit
                );

        etReviewContent =
                findViewById(
                        R.id.etReviewContent
                );

        btnSubmitReview =
                findViewById(
                        R.id.btnSubmitReview
                );

        btnTaste =
                findViewById(
                        R.id.btnTaste
                );

        btnCost =
                findViewById(
                        R.id.btnCost
                );

        btnKind =
                findViewById(
                        R.id.btnKind
                );

        btnMood =
                findViewById(
                        R.id.btnMood
                );

        btnParking =
                findViewById(
                        R.id.btnParking
                );

        btnAddPhoto =
                findViewById(
                        R.id.btnAddPhoto
                );

        ivPreviewImage =
                findViewById(
                        R.id.ivPreviewImage
                );

        reviewTagManager =
                new ReviewTagManager(
                        this
                );

        reviewImagePickerManager =
                new ReviewImagePickerManager(
                        this,
                        ivPreviewImage
                );

        reviewWriteValidator =
                new ReviewWriteValidator(
                        this
                );

        reviewSubmitManager =
                new ReviewSubmitManager();
    }

    private void initPlaceName() {

        if (placeName != null
                && !placeName.trim().isEmpty()) {

            tvPlaceName.setText(
                    placeName
            );
        }
    }

    private void initTagButtons() {

        reviewTagManager.bindTagButton(
                btnTaste,
                "맛"
        );

        reviewTagManager.bindTagButton(
                btnCost,
                "가성비"
        );

        reviewTagManager.bindTagButton(
                btnKind,
                "친절"
        );

        reviewTagManager.bindTagButton(
                btnMood,
                "분위기"
        );

        reviewTagManager.bindTagButton(
                btnParking,
                "주차"
        );
    }

    private void initPhotoButton() {

        btnAddPhoto.setOnClickListener(v -> {

            imagePickerLauncher.launch(
                    "image/*"
            );
        });
    }

    private void initEditMode() {

        if (!isEditMode) {
            return;
        }

        btnSubmitReview.setText(
                "수정하기"
        );

        int rating =
                getIntent().getIntExtra(
                        "rating",
                        0
                );

        String content =
                getIntent().getStringExtra(
                        "content"
                );

        String tags =
                getIntent().getStringExtra(
                        "tags"
                );

        boolean revisit =
                getIntent().getBooleanExtra(
                        "revisit",
                        false
                );

        ratingBar.setRating(
                rating
        );

        cbRevisit.setChecked(
                revisit
        );

        if (content != null) {

            etReviewContent.setText(
                    content
            );
        }

        if (tags != null
                && !tags.trim().isEmpty()) {

            String[] splitTags =
                    tags.split(
                            ","
                    );

            for (String tag : splitTags) {

                String trimmed =
                        tag.trim();

                if ("맛".equals(trimmed)) {

                    reviewTagManager.selectTag(
                            btnTaste,
                            "맛"
                    );
                }

                if ("가성비".equals(trimmed)) {

                    reviewTagManager.selectTag(
                            btnCost,
                            "가성비"
                    );
                }

                if ("친절".equals(trimmed)) {

                    reviewTagManager.selectTag(
                            btnKind,
                            "친절"
                    );
                }

                if ("분위기".equals(trimmed)) {

                    reviewTagManager.selectTag(
                            btnMood,
                            "분위기"
                    );
                }

                if ("주차".equals(trimmed)) {

                    reviewTagManager.selectTag(
                            btnParking,
                            "주차"
                    );
                }
            }
        }
    }

    private void initSubmitButton() {

        btnSubmitReview.setOnClickListener(v -> {

            int rating =
                    (int) ratingBar.getRating();

            boolean revisit =
                    cbRevisit.isChecked();

            String content =
                    etReviewContent
                            .getText()
                            .toString()
                            .trim();

            if (!reviewWriteValidator.validate(
                    placeId,
                    memberId,
                    rating,
                    content
            )) {

                return;
            }

            if (isEditMode
                    && (reviewId == null
                    || reviewId <= 0)) {

                Toast.makeText(
                        ReviewWriteActivity.this,
                        "수정할 후기 정보가 없습니다.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (isEditMode) {

                updateReview(
                        rating,
                        revisit,
                        content
                );

            } else {

                submitReview(
                        rating,
                        revisit,
                        content
                );
            }
        });
    }

    private void submitReview(
            int rating,
            boolean revisit,
            String content
    ) {

        String tagsString =
                reviewTagManager.getTagsString();

        MultipartBody.Part imagePart =
                reviewImagePickerManager == null
                        ? null
                        : reviewImagePickerManager.createImagePart();

        ReviewWriteRequestBuilder.RequestData requestData =
                ReviewWriteRequestBuilder.build(
                        placeId,
                        placeName,
                        latitude,
                        longitude,
                        memberId,
                        rating,
                        revisit,
                        content,
                        tagsString,
                        imagePart
                );

        setSubmitLoading(
                true
        );

        reviewSubmitManager.submitReview(
                this,
                requestData.getPlaceIdBody(),
                requestData.getPlaceNameBody(),
                requestData.getLatitudeBody(),
                requestData.getLongitudeBody(),
                requestData.getMemberIdBody(),
                requestData.getRatingBody(),
                requestData.getRevisitBody(),
                requestData.getContentBody(),
                requestData.getTagsBody(),
                requestData.getImagePart(),
                new ReviewSubmitManager.SubmitCallback() {

                    @Override
                    public void onSuccess() {

                        setSubmitLoading(
                                false
                        );

                        Toast.makeText(
                                ReviewWriteActivity.this,
                                "후기가 등록되었습니다.",
                                Toast.LENGTH_SHORT
                        ).show();

                        setResult(
                                RESULT_OK
                        );

                        finish();
                    }

                    @Override
                    public void onFail(
                            int code,
                            String errorMessage
                    ) {

                        setSubmitLoading(
                                false
                        );

                        Toast.makeText(
                                ReviewWriteActivity.this,
                                "후기 등록 실패 : "
                                        + code,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(
                            Throwable t
                    ) {

                        setSubmitLoading(
                                false
                        );

                        Toast.makeText(
                                ReviewWriteActivity.this,
                                "서버 연결 실패",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void updateReview(
            int rating,
            boolean revisit,
            String content
    ) {

        String tagsString =
                reviewTagManager.getTagsString();

        MultipartBody.Part imagePart =
                reviewImagePickerManager == null
                        ? null
                        : reviewImagePickerManager.createImagePart();

        ReviewWriteRequestBuilder.RequestData requestData =
                ReviewWriteRequestBuilder.build(
                        placeId,
                        placeName,
                        latitude,
                        longitude,
                        memberId,
                        rating,
                        revisit,
                        content,
                        tagsString,
                        imagePart
                );

        setSubmitLoading(
                true
        );

        reviewSubmitManager.updateReview(
                this,
                reviewId,
                requestData.getPlaceNameBody(),
                requestData.getLatitudeBody(),
                requestData.getLongitudeBody(),
                requestData.getMemberIdBody(),
                requestData.getRatingBody(),
                requestData.getRevisitBody(),
                requestData.getContentBody(),
                requestData.getTagsBody(),
                requestData.getImagePart(),
                new ReviewSubmitManager.SubmitCallback() {

                    @Override
                    public void onSuccess() {

                        setSubmitLoading(
                                false
                        );

                        Toast.makeText(
                                ReviewWriteActivity.this,
                                "후기가 수정되었습니다.",
                                Toast.LENGTH_SHORT
                        ).show();

                        setResult(
                                RESULT_OK
                        );

                        finish();
                    }

                    @Override
                    public void onFail(
                            int code,
                            String errorMessage
                    ) {

                        setSubmitLoading(
                                false
                        );

                        Toast.makeText(
                                ReviewWriteActivity.this,
                                "후기 수정 실패 : "
                                        + code,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(
                            Throwable t
                    ) {

                        setSubmitLoading(
                                false
                        );

                        Toast.makeText(
                                ReviewWriteActivity.this,
                                "서버 연결 실패",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void setSubmitLoading(
            boolean loading
    ) {

        btnSubmitReview.setEnabled(
                !loading
        );

        if (loading) {

            btnSubmitReview.setText(
                    isEditMode
                            ? "수정중..."
                            : "등록중..."
            );

        } else {

            btnSubmitReview.setText(
                    isEditMode
                            ? "수정하기"
                            : "등록하기"
            );
        }
    }
}