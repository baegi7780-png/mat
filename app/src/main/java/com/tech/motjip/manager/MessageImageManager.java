package com.tech.motjip.manager;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.MessageActivity;
import com.tech.motjip.Model.UploadResponse;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageImageManager {

    private static final String TAG =
            "MessageImage";

    private final MessageActivity activity;

    /*
     * IMAGE / VIDEO 공용 업로드 콜백
     */
    public interface OnFileUploadListener {

        void onUploaded(
                String fileUrl
        );
    }

    public MessageImageManager(
            MessageActivity activity
    ) {

        this.activity = activity;
    }

    /*
     * 기존 이미지 업로드 유지
     */
    public void uploadAndSendImage(
            Uri imageUri,
            OnFileUploadListener listener
    ) {

        uploadAndSendFile(
                imageUri,
                listener
        );
    }

    /*
     * 동영상 업로드 추가
     */
    public void uploadAndSendVideo(
            Uri videoUri,
            OnFileUploadListener listener
    ) {

        uploadAndSendFile(
                videoUri,
                listener
        );
    }

    /*
     * IMAGE / VIDEO 공용 업로드
     */
    public void uploadAndSendFile(
            Uri uri,
            OnFileUploadListener listener
    ) {

        Log.d(
                TAG,
                "uploadAndSendFile uri = " + uri
        );

        try {

            String filePath =
                    getRealPathFromUri(
                            uri
                    );

            if (filePath == null
                    || filePath.trim().isEmpty()) {

                Toast.makeText(
                        activity,
                        "파일을 불러올 수 없습니다.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            File file =
                    new File(filePath);

            /*
             * image/* 또는 video/*
             */
            String mimeType =
                    activity.getContentResolver()
                            .getType(uri);

            if (mimeType == null
                    || mimeType.trim().isEmpty()) {

                mimeType =
                        "application/octet-stream";
            }

            RequestBody requestFile =
                    RequestBody.create(
                            file,
                            MediaType.parse(
                                    mimeType
                            )
                    );

            MultipartBody.Part body =
                    MultipartBody.Part.createFormData(
                            "file",
                            file.getName(),
                            requestFile
                    );

            RetrofitClient.getApiService(activity)
                    .uploadImage(body)
                    .enqueue(new Callback<UploadResponse>() {

                        @Override
                        public void onResponse(
                                @NonNull Call<UploadResponse> call,
                                @NonNull Response<UploadResponse> response
                        ) {

                            if (response.isSuccessful()
                                    && response.body() != null) {

                                String fileUrl =
                                        response.body()
                                                .getFileUrl();

                                if (fileUrl == null
                                        || fileUrl.trim().isEmpty()) {

                                    Toast.makeText(
                                            activity,
                                            "파일 URL이 비어있습니다.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                listener.onUploaded(
                                        fileUrl
                                );

                            } else {

                                Toast.makeText(
                                        activity,
                                        "파일 업로드 실패",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }

                        @Override
                        public void onFailure(
                                @NonNull Call<UploadResponse> call,
                                @NonNull Throwable t
                        ) {

                            Log.e(
                                    TAG,
                                    "파일 업로드 실패",
                                    t
                            );

                            Toast.makeText(
                                    activity,
                                    "파일 업로드 실패",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "파일 처리 실패",
                    e
            );

            Toast.makeText(
                    activity,
                    "파일 처리 실패",
                    Toast.LENGTH_SHORT
            ).show();
        }
    }

    private String getRealPathFromUri(
            Uri uri
    ) {

        Cursor cursor =
                activity.getContentResolver().query(
                        uri,
                        null,
                        null,
                        null,
                        null
                );

        if (cursor != null) {

            try {

                int index =
                        cursor.getColumnIndex(
                                MediaStore.Images.Media.DATA
                        );

                if (index != -1
                        && cursor.moveToFirst()) {

                    return cursor.getString(
                            index
                    );
                }

            } finally {

                cursor.close();
            }
        }

        return uri.getPath();
    }
}