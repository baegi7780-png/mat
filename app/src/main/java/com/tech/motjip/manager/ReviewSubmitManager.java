package com.tech.motjip.manager;

import android.content.Context;
import android.util.Log;

import com.tech.motjip.API.RetrofitClient;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewSubmitManager {

    private static final String TAG =
            "ReviewSubmitManager";

    public interface SubmitCallback {

        void onSuccess();

        void onFail(
                int code,
                String errorMessage
        );

        void onError(
                Throwable t
        );
    }

    public void submitReview(
            Context context,
            RequestBody placeIdBody,
            RequestBody placeNameBody,
            RequestBody latitudeBody,
            RequestBody longitudeBody,
            RequestBody memberIdBody,
            RequestBody ratingBody,
            RequestBody revisitBody,
            RequestBody contentBody,
            RequestBody tagsBody,
            MultipartBody.Part imagePart,
            SubmitCallback callback
    ) {

        Log.d(
                TAG,
                "submitReview 호출"
        );

        Log.d(
                TAG,
                "placeNameBody = "
                        + placeNameBody
        );

        Log.d(
                TAG,
                "latitudeBody = "
                        + latitudeBody
        );

        Log.d(
                TAG,
                "longitudeBody = "
                        + longitudeBody
        );


        RetrofitClient
                .getApiService(
                        context
                )
                .createReview(
                        placeIdBody,
                        placeNameBody,
                        latitudeBody,
                        longitudeBody,
                        memberIdBody,
                        ratingBody,
                        revisitBody,
                        contentBody,
                        tagsBody,
                        imagePart
                )
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {

                                Log.d(
                                        TAG,
                                        "submitReview response code = "
                                                + response.code()
                                );

                                if (response.isSuccessful()) {

                                    Log.d(
                                            TAG,
                                            "후기 등록 성공"
                                    );

                                    if (callback != null) {

                                        callback.onSuccess();
                                    }

                                } else {

                                    String errorBody =
                                            readErrorBody(
                                                    response
                                            );

                                    Log.e(
                                            TAG,
                                            "후기 등록 실패 code = "
                                                    + response.code()
                                                    + ", errorBody = "
                                                    + errorBody
                                    );

                                    if (callback != null) {

                                        callback.onFail(
                                                response.code(),
                                                errorBody
                                        );
                                    }
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable t
                            ) {

                                Log.e(
                                        TAG,
                                        "후기 등록 서버 연결 실패",
                                        t
                                );

                                if (callback != null) {

                                    callback.onError(
                                            t
                                    );
                                }
                            }
                        }
                );
    }

    public void updateReview(
            Context context,
            Long reviewId,
            RequestBody placeNameBody,
            RequestBody latitudeBody,
            RequestBody longitudeBody,
            RequestBody memberIdBody,
            RequestBody ratingBody,
            RequestBody revisitBody,
            RequestBody contentBody,
            RequestBody tagsBody,
            MultipartBody.Part imagePart,
            SubmitCallback callback
    ) {

        Log.d(
                TAG,
                "updateReview 호출"
        );

        Log.d(
                TAG,
                "reviewId = "
                        + reviewId
        );

        Log.d(
                TAG,
                "placeNameBody = "
                        + placeNameBody
        );

        Log.d(
                TAG,
                "latitudeBody = "
                        + latitudeBody
        );

        Log.d(
                TAG,
                "longitudeBody = "
                        + longitudeBody
        );

        RetrofitClient
                .getApiService(
                        context
                )
                .updateReview(
                        reviewId,
                        placeNameBody,
                        latitudeBody,
                        longitudeBody,
                        memberIdBody,
                        ratingBody,
                        revisitBody,
                        contentBody,
                        tagsBody,
                        imagePart
                )
                .enqueue(
                        new Callback<Void>() {

                            @Override
                            public void onResponse(
                                    Call<Void> call,
                                    Response<Void> response
                            ) {

                                Log.d(
                                        TAG,
                                        "updateReview response code = "
                                                + response.code()
                                );

                                if (response.isSuccessful()) {

                                    Log.d(
                                            TAG,
                                            "후기 수정 성공"
                                    );

                                    if (callback != null) {

                                        callback.onSuccess();
                                    }

                                } else {

                                    String errorBody =
                                            readErrorBody(
                                                    response
                                            );

                                    Log.e(
                                            TAG,
                                            "후기 수정 실패 code = "
                                                    + response.code()
                                                    + ", errorBody = "
                                                    + errorBody
                                    );

                                    if (callback != null) {

                                        callback.onFail(
                                                response.code(),
                                                errorBody
                                        );
                                    }
                                }
                            }

                            @Override
                            public void onFailure(
                                    Call<Void> call,
                                    Throwable t
                            ) {

                                Log.e(
                                        TAG,
                                        "후기 수정 서버 연결 실패",
                                        t
                                );

                                if (callback != null) {

                                    callback.onError(
                                            t
                                    );
                                }
                            }
                        }
                );
    }

    private String readErrorBody(
            Response<Void> response
    ) {

        String errorBody =
                "";

        try {

            if (response.errorBody() != null) {

                errorBody =
                        response
                                .errorBody()
                                .string();
            }

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "errorBody 읽기 실패",
                    e
            );
        }

        return errorBody;
    }
}