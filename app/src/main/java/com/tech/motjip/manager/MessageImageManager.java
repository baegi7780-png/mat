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

    private static final String TAG = "MessageImage";

    private final MessageActivity activity;

    public interface OnImageUploadListener {

        void onUploaded(
                String imageUrl
        );
    }

    public MessageImageManager(
            MessageActivity activity
    ) {

        this.activity = activity;
    }

    public void uploadAndSendImage(
            Uri imageUri,
            OnImageUploadListener listener
    ) {

        Log.d(
                TAG,
                "uploadAndSendImage uri = " + imageUri
        );

        try {

            String filePath =
                    getRealPathFromUri(imageUri);

            if (filePath == null
                    || filePath.trim().isEmpty()) {

                Toast.makeText(
                        activity,
                        "이미지를 불러올 수 없습니다.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            File file =
                    new File(filePath);

            RequestBody requestFile =
                    RequestBody.create(
                            file,
                            MediaType.parse("image/*")
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

                                String imageUrl =
                                        response.body().getFileUrl();

                                if (imageUrl == null
                                        || imageUrl.trim().isEmpty()) {

                                    Toast.makeText(
                                            activity,
                                            "이미지 URL이 비어있습니다.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                listener.onUploaded(imageUrl);

                            } else {

                                Toast.makeText(
                                        activity,
                                        "이미지 업로드 실패",
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
                                    "이미지 업로드 실패",
                                    t
                            );

                            Toast.makeText(
                                    activity,
                                    "이미지 업로드 실패",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    });

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "이미지 처리 실패",
                    e
            );

            Toast.makeText(
                    activity,
                    "이미지 처리 실패",
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

                    return cursor.getString(index);
                }

            } finally {

                cursor.close();
            }
        }

        return uri.getPath();
    }
}