package com.tech.motjip;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.tech.motjip.Controller.WriteController;
import com.tech.motjip.Model.CommunityPost;
import com.tech.motjip.Utils.DialogUtil;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WriteActivity extends AppCompatActivity {

    private ImageButton btnBack;

    private Spinner spinnerTag;
    private Spinner spinnerRegion;

    private EditText etTitle;
    private EditText etLocation;
    private EditText etDate;
    private EditText etContent;

    private TextView tvTitleCount;
    private TextView tvContentCount;

    private ImageView imgPreview;
    private TextView tvFileName;

    private ImageButton btnRemoveImage;

    private Button btnAddImage;
    private Button btnUpload;
    private Button btnCancel;

    private ProgressBar progressUpload;

    private Uri selectedImageUri;

    private ActivityResultLauncher<String> imagePickerLauncher;

    private WriteController writeController;

    private boolean isEditMode = false;
    private boolean isUploading = false;

    private CommunityPost editPost;

    private String uploadButtonDefaultText = "등록하기";

    private String[] tags = {
            "한식",
            "중식",
            "양식",
            "일식",
            "분식",
            "카페"
    };

    private String[] regions = {
            "서울",
            "부산",
            "대구",
            "인천",
            "광주",
            "대전",
            "울산",
            "세종",
            "경기",
            "강원",
            "충북",
            "충남",
            "전북",
            "전남",
            "경북",
            "경남",
            "제주"
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_write);

        writeController = new WriteController(this);

        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {

                    if (uri != null) {

                        if (!writeController.isImageSizeValid(
                                uri,
                                10
                        )) {

                            DialogUtil.showMessageDialog(
                                    this,
                                    R.drawable.fail,
                                    "이미지 오류",
                                    "10MB 이하 이미지만 업로드 가능합니다.",
                                    null
                            );

                            return;
                        }

                        selectedImageUri = uri;

                        imgPreview.setImageURI(uri);

                        String fileName =
                                writeController.getFileName(uri);

                        tvFileName.setText(fileName);

                        btnRemoveImage.setVisibility(View.VISIBLE);

                        DialogUtil.showMessageDialog(
                                this,
                                R.drawable.success,
                                "이미지 선택",
                                "이미지가 선택되었습니다.",
                                null
                        );
                    }
                }
        );

        btnBack = findViewById(R.id.btnBack);

        spinnerTag = findViewById(R.id.spinnerTag);
        spinnerRegion = findViewById(R.id.spinnerRegion);

        etTitle = findViewById(R.id.etTitle);
        etLocation = findViewById(R.id.etLocation);
        etDate = findViewById(R.id.etDate);
        etContent = findViewById(R.id.etContent);

        tvTitleCount = findViewById(R.id.tvTitleCount);
        tvContentCount = findViewById(R.id.tvContentCount);

        imgPreview = findViewById(R.id.imgPreview);
        tvFileName = findViewById(R.id.tvFileName);
        btnRemoveImage = findViewById(R.id.btnRemoveImage);

        btnAddImage = findViewById(R.id.btnAddImage);
        btnUpload = findViewById(R.id.btnUpload);
        btnCancel = findViewById(R.id.btnCancel);

        progressUpload = findViewById(R.id.progressUpload);

        uploadButtonDefaultText =
                btnUpload.getText().toString();

        etTitle.setSingleLine(true);
        etLocation.setSingleLine(true);

        etTitle.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        etLocation.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        etContent.setImeOptions(EditorInfo.IME_ACTION_DONE);

        // 지도 상세페이지에서 전달된 장소 데이터 받기
        String placeName =
                getIntent().getStringExtra(
                        "placeName"
                );

        String placeAddress =
                getIntent().getStringExtra(
                        "placeAddress"
                );

        // 전달받은 장소명 자동 입력
        // 단순 기본값이므로 사용자가 삭제하거나 수정할 수 있음
        if (placeName != null
                && !placeName.isEmpty()) {

            etTitle.setText(
                    placeName + " 같이 가실 분!"
            );

            etTitle.setSelection(
                    etTitle.getText().length()
            );
        }

        // 전달받은 주소 자동 입력
        // 단순 기본값이므로 사용자가 삭제하거나 수정할 수 있음
        if (placeAddress != null
                && !placeAddress.isEmpty()) {

            etLocation.setText(
                    placeAddress
            );

            etLocation.setSelection(
                    etLocation.getText().length()
            );
        }

        tvTitleCount.setText(
                etTitle.getText().toString().length() + " / 25"
        );

        tvContentCount.setText(
                etContent.getText().toString().length() + " / 500"
        );

        updateTitleCountColor(
                etTitle.getText().toString().length()
        );

        updateContentCountColor(
                etContent.getText().toString().length()
        );

        setTextWatchers();

        etTitle.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_NEXT) {

                etLocation.requestFocus();

                return true;
            }

            return false;
        });

        etLocation.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_NEXT) {

                etDate.requestFocus();

                etDate.performClick();

                return true;
            }

            return false;
        });

        etContent.setOnEditorActionListener((v, actionId, event) -> {

            if (actionId == EditorInfo.IME_ACTION_DONE) {

                etContent.clearFocus();

                InputMethodManager imm =
                        (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

                if (imm != null) {

                    imm.hideSoftInputFromWindow(
                            etContent.getWindowToken(),
                            0
                    );
                }

                return true;
            }

            return false;
        });

        ArrayAdapter<String> tagAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        tags
                );

        spinnerTag.setAdapter(tagAdapter);

        ArrayAdapter<String> regionAdapter =
                new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_spinner_dropdown_item,
                        regions
                );

        spinnerRegion.setAdapter(regionAdapter);

        isEditMode =
                getIntent().getBooleanExtra(
                        "isEditMode",
                        false
                );

        if (isEditMode) {

            editPost =
                    (CommunityPost) getIntent()
                            .getSerializableExtra("communityPost");

            if (editPost != null) {

                setEditModeData();
            }
        }

        etDate.setFocusable(false);
        etDate.setClickable(true);

        etDate.setOnClickListener(v -> {

            Calendar now = Calendar.getInstance();

            int year = now.get(Calendar.YEAR);
            int month = now.get(Calendar.MONTH);
            int day = now.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog =
                    new DatePickerDialog(
                            WriteActivity.this,
                            (view, selectedYear, selectedMonth, selectedDay) -> {

                                TimePickerDialog timePickerDialog =
                                        new TimePickerDialog(
                                                WriteActivity.this,
                                                (timeView, selectedHour, selectedMinute) -> {

                                                    Calendar selectedDateTime =
                                                            Calendar.getInstance();

                                                    selectedDateTime.set(
                                                            selectedYear,
                                                            selectedMonth,
                                                            selectedDay,
                                                            selectedHour,
                                                            selectedMinute,
                                                            0
                                                    );

                                                    if (selectedDateTime.before(Calendar.getInstance())) {

                                                        DialogUtil.showMessageDialog(
                                                                WriteActivity.this,
                                                                R.drawable.fail,
                                                                "날짜 오류",
                                                                "현재 시간 이후로 선택해 주세요.",
                                                                null
                                                        );

                                                        return;
                                                    }

                                                    String selectedDateTimeText =
                                                            selectedYear + "-"
                                                                    + String.format("%02d", selectedMonth + 1)
                                                                    + "-"
                                                                    + String.format("%02d", selectedDay)
                                                                    + " "
                                                                    + String.format("%02d", selectedHour)
                                                                    + ":"
                                                                    + String.format("%02d", selectedMinute);

                                                    etDate.setText(selectedDateTimeText);

                                                    etDate.clearFocus();

                                                    etContent.requestFocus();

                                                },
                                                now.get(Calendar.HOUR_OF_DAY),
                                                now.get(Calendar.MINUTE),
                                                true
                                        );

                                timePickerDialog.show();

                            },
                            year,
                            month,
                            day
                    );

            datePickerDialog.getDatePicker().setMinDate(
                    now.getTimeInMillis()
            );

            datePickerDialog.show();
        });

        btnBack.setOnClickListener(v -> {
            handleExit();
        });

        btnCancel.setOnClickListener(v -> {
            handleExit();
        });

        btnAddImage.setOnClickListener(v ->
                imagePickerLauncher.launch("image/*")
        );

        btnRemoveImage.setOnClickListener(v -> {

            selectedImageUri = null;

            imgPreview.setImageResource(
                    R.drawable.chef_illustration
            );

            tvFileName.setText(
                    "선택된 이미지 없음"
            );

            btnRemoveImage.setVisibility(View.GONE);
        });

        btnUpload.setOnClickListener(v -> {

            String tag =
                    spinnerTag.getSelectedItem().toString();

            String region =
                    spinnerRegion.getSelectedItem().toString();

            String title =
                    etTitle.getText().toString().trim();

            String location =
                    etLocation.getText().toString().trim();

            String date =
                    writeController.convertDateForServer(
                            etDate.getText().toString().trim()
                    );

            String content =
                    etContent.getText().toString().trim();

            String validationMessage =
                    writeController.validateCommunityPost(
                            title,
                            location,
                            date,
                            content
                    );

            if (validationMessage != null) {

                DialogUtil.showMessageDialog(
                        this,
                        R.drawable.fail,
                        "입력 오류",
                        validationMessage,
                        null
                );

                return;
            }

            setUploading(true);

            if (isEditMode) {

                writeController.updateCommunityPost(
                        editPost.getComId(),
                        tag,
                        region,
                        title,
                        location,
                        date,
                        content,
                        selectedImageUri,
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {

                                if (response.isSuccessful()) {

                                    DialogUtil.showMessageDialog(
                                            WriteActivity.this,
                                            R.drawable.success,
                                            "수정 완료",
                                            "게시글이 수정되었습니다.",
                                            () -> {

                                                setResult(RESULT_OK);

                                                finish();
                                            }
                                    );

                                } else {

                                    setUploading(false);

                                    String errorMessage =
                                            writeController.getErrorMessage(
                                                    response,
                                                    "게시글 수정에 실패했습니다."
                                            );

                                    DialogUtil.showMessageDialog(
                                            WriteActivity.this,
                                            R.drawable.fail,
                                            "수정 실패",
                                            errorMessage,
                                            null
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable t
                            ) {

                                setUploading(false);

                                DialogUtil.showMessageDialog(
                                        WriteActivity.this,
                                        R.drawable.fail,
                                        "서버 연결 실패",
                                        "서버와 연결할 수 없습니다.\n"
                                                + t.getMessage(),
                                        null
                                );
                            }
                        }
                );

            } else {

                writeController.uploadCommunityPost(
                        tag,
                        region,
                        title,
                        location,
                        date,
                        content,
                        selectedImageUri,
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {

                                if (response.isSuccessful()) {

                                    DialogUtil.showMessageDialog(
                                            WriteActivity.this,
                                            R.drawable.success,
                                            "등록 완료",
                                            "게시글이 성공적으로 등록되었습니다.",
                                            () -> {

                                                setResult(RESULT_OK);

                                                finish();
                                            }
                                    );

                                } else {

                                    setUploading(false);

                                    String errorMessage =
                                            writeController.getErrorMessage(
                                                    response,
                                                    "게시글 등록에 실패했습니다."
                                            );

                                    DialogUtil.showMessageDialog(
                                            WriteActivity.this,
                                            R.drawable.fail,
                                            "등록 실패",
                                            errorMessage,
                                            null
                                    );
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable t
                            ) {

                                setUploading(false);

                                DialogUtil.showMessageDialog(
                                        WriteActivity.this,
                                        R.drawable.fail,
                                        "서버 연결 실패",
                                        "서버와 연결할 수 없습니다.\n"
                                                + t.getMessage(),
                                        null
                                );
                            }
                        }
                );
            }
        });
    }

    private void setTextWatchers() {

        etTitle.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {

                tvTitleCount.setText(
                        s.length() + " / 25"
                );

                updateTitleCountColor(
                        s.length()
                );
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        etContent.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {

                tvContentCount.setText(
                        s.length() + " / 500"
                );

                updateContentCountColor(
                        s.length()
                );
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void updateTitleCountColor(int length) {

        if (length >= 25) {

            tvTitleCount.setTextColor(
                    Color.parseColor("#D32F2F")
            );

        } else if (length >= 20) {

            tvTitleCount.setTextColor(
                    Color.parseColor("#F57C00")
            );

        } else {

            tvTitleCount.setTextColor(
                    Color.parseColor("#888888")
            );
        }
    }

    private void updateContentCountColor(int length) {

        if (length >= 500) {

            tvContentCount.setTextColor(
                    Color.parseColor("#D32F2F")
            );

        } else if (length >= 450) {

            tvContentCount.setTextColor(
                    Color.parseColor("#F57C00")
            );

        } else {

            tvContentCount.setTextColor(
                    Color.parseColor("#888888")
            );
        }
    }

    private void setUploading(boolean uploading) {

        isUploading = uploading;

        btnUpload.setEnabled(!uploading);
        btnCancel.setEnabled(!uploading);
        btnBack.setEnabled(!uploading);
        btnAddImage.setEnabled(!uploading);
        btnRemoveImage.setEnabled(!uploading);

        if (uploading) {

            progressUpload.setVisibility(View.VISIBLE);

            if (isEditMode) {

                btnUpload.setText("수정 중...");

            } else {

                btnUpload.setText("등록 중...");
            }

        } else {

            progressUpload.setVisibility(View.GONE);

            if (isEditMode) {

                btnUpload.setText("수정 완료");

            } else {

                btnUpload.setText(uploadButtonDefaultText);
            }
        }
    }

    private void setEditModeData() {

        etTitle.setText(editPost.getTitle());

        etLocation.setText(editPost.getPlaceName());

        etDate.setText(editPost.getMeetingAt());

        etContent.setText(editPost.getContent());

        btnUpload.setText("수정 완료");

        btnRemoveImage.setVisibility(View.GONE);

        for (int i = 0; i < tags.length; i++) {

            if (tags[i].equals(editPost.getTag())) {

                spinnerTag.setSelection(i);
                break;
            }
        }

        for (int i = 0; i < regions.length; i++) {

            if (regions[i].equals(editPost.getRegion())) {

                spinnerRegion.setSelection(i);
                break;
            }
        }
    }

    private void handleExit() {

        if (isUploading) {
            return;
        }

        if (hasInputData()) {

            showExitDialog();

        } else {

            finish();
        }
    }

    private boolean hasInputData() {

        return writeController.hasInputData(
                etTitle.getText().toString(),
                etLocation.getText().toString(),
                etDate.getText().toString(),
                etContent.getText().toString(),
                selectedImageUri
        );
    }

    private void showExitDialog() {

        new AlertDialog.Builder(this)
                .setTitle("작성 취소")
                .setMessage("작성 중인 내용이 사라집니다.\n정말 나가시겠습니까?")
                .setPositiveButton("나가기", (dialog, which) -> finish())
                .setNegativeButton("계속 작성", null)
                .show();
    }

    @Override
    public void onBackPressed() {
        handleExit();
    }
}