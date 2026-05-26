package com.tech.motjip.manager;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

public class ReviewImagePickerManager {

    private static final String TAG =
            "ReviewImagePickerDebug";

    private final Context context;

    private final ImageView ivPreviewImage;

    private Uri selectedImageUri;

    public ReviewImagePickerManager(
            Context context,
            ImageView ivPreviewImage
    ) {

        this.context =
                context;

        this.ivPreviewImage =
                ivPreviewImage;
    }

    public void setSelectedImageUri(
            Uri uri
    ) {

        if (uri == null) {

            Log.d(
                    TAG,
                    "이미지 선택 취소"
            );

            return;
        }

        selectedImageUri =
                uri;

        Log.d(
                TAG,
                "이미지 선택 완료 uri = "
                        + uri
        );

        showPreview(
                uri
        );
    }

    private void showPreview(
            Uri uri
    ) {

        if (ivPreviewImage == null) {

            Log.e(
                    TAG,
                    "ivPreviewImage null"
            );

            return;
        }

        ivPreviewImage.setVisibility(
                View.VISIBLE
        );

        ivPreviewImage.setImageURI(
                uri
        );

        Log.d(
                TAG,
                "이미지 미리보기 표시 완료"
        );
    }

    public MultipartBody.Part createImagePart() {

        if (selectedImageUri == null) {

            Log.d(
                    TAG,
                    "선택 이미지 없음 → imagePart null"
            );

            return null;
        }

        try {

            Log.d(
                    TAG,
                    "이미지 Multipart 변환 시작"
            );

            InputStream inputStream =
                    context
                            .getContentResolver()
                            .openInputStream(
                                    selectedImageUri
                            );

            byte[] imageBytes =
                    getBytes(
                            inputStream
                    );

            Log.d(
                    TAG,
                    "이미지 byte 크기 = "
                            + imageBytes.length
            );

            RequestBody requestFile =
                    RequestBody.create(
                            MediaType.parse(
                                    "image/*"
                            ),
                            imageBytes
                    );

            MultipartBody.Part imagePart =
                    MultipartBody.Part.createFormData(
                            "image",
                            "review_image.jpg",
                            requestFile
                    );

            Log.d(
                    TAG,
                    "이미지 Multipart 변환 완료"
            );

            return imagePart;

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "이미지 처리 실패",
                    e
            );

            Toast.makeText(
                    context,
                    "이미지 처리 실패",
                    Toast.LENGTH_SHORT
            ).show();

            return null;
        }
    }

    public Uri getSelectedImageUri() {

        return selectedImageUri;
    }

    public void clear() {

        selectedImageUri =
                null;

        if (ivPreviewImage != null) {

            ivPreviewImage.setVisibility(
                    View.GONE
            );

            ivPreviewImage.setImageDrawable(
                    null
            );
        }
    }

    private byte[] getBytes(
            InputStream inputStream
    ) throws IOException {

        Log.d(
                TAG,
                "getBytes 시작"
        );

        ByteArrayOutputStream byteBuffer =
                new ByteArrayOutputStream();

        int bufferSize =
                1024;

        byte[] buffer =
                new byte[bufferSize];

        int len;

        while ((len = inputStream.read(buffer)) != -1) {

            byteBuffer.write(
                    buffer,
                    0,
                    len
            );
        }

        Log.d(
                TAG,
                "getBytes 완료 size = "
                        + byteBuffer.size()
        );

        return byteBuffer.toByteArray();
    }
}