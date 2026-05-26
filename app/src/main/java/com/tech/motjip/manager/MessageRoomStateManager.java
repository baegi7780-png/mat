package com.tech.motjip.manager;

import android.util.Log;

import androidx.annotation.NonNull;

import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.MessageActivity;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessageRoomStateManager {

    private static final String TAG =
            "MessageRoomState";

    private final MessageActivity activity;

    private final long roomId;

    public MessageRoomStateManager(
            MessageActivity activity,
            long roomId
    ) {

        this.activity = activity;
        this.roomId = roomId;
    }

    public void enterChatRoom(
            Long currentUserId
    ) {

        Log.d(
                TAG,
                "enterChatRoom 호출 roomId="
                        + roomId
                        + ", currentUserId="
                        + currentUserId
        );

        if (roomId == -1
                || currentUserId == null
                || currentUserId <= 0) {

            Log.d(
                    TAG,
                    "채팅방 활성 등록 중단"
            );

            return;
        }

        RetrofitClient.getApiService(activity)
                .enterChatRoom(
                        currentUserId,
                        roomId
                )
                .enqueue(new Callback<Map<String, Object>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<Map<String, Object>> call,
                            @NonNull Response<Map<String, Object>> response
                    ) {

                        Log.d(
                                TAG,
                                "채팅방 활성 등록 응답 code="
                                        + response.code()
                        );

                        Log.d(
                                "CHAT_TEST",
                                "ENTER_ROOM_RESPONSE code="
                                        + response.code()
                        );

                        if (response.body() != null) {

                            Log.d(
                                    "CHAT_TEST",
                                    "ENTER_ROOM_BODY="
                                            + response.body()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Map<String, Object>> call,
                            @NonNull Throwable t
                    ) {

                        Log.e(
                                TAG,
                                "채팅방 활성 등록 실패",
                                t
                        );

                        Log.e(
                                "CHAT_TEST",
                                "ENTER_ROOM_FAIL",
                                t
                        );
                    }
                });
    }

    public void exitChatRoom(
            Long currentUserId,
            Long roomId
    ) {

        Log.d(
                TAG,
                "exitChatRoom 호출 currentUserId="
                        + currentUserId
                        + ", roomId="
                        + roomId
        );

        if (currentUserId == null
                || currentUserId <= 0
                || roomId == null
                || roomId <= 0) {

            Log.d(
                    TAG,
                    "채팅방 활성 제거 중단"
            );

            return;
        }

        RetrofitClient.getApiService(activity)
                .exitChatRoom(
                        currentUserId,
                        roomId
                )
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<Void> call,
                            @NonNull Response<Void> response
                    ) {

                        Log.d(
                                TAG,
                                "채팅방 활성 제거 응답 code="
                                        + response.code()
                        );

                        Log.d(
                                "CHAT_TEST",
                                "EXIT_ROOM_RESPONSE code="
                                        + response.code()
                                        + ", memberId="
                                        + currentUserId
                                        + ", roomId="
                                        + roomId
                        );
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Void> call,
                            @NonNull Throwable t
                    ) {

                        Log.e(
                                TAG,
                                "채팅방 활성 제거 실패",
                                t
                        );

                        Log.e(
                                "CHAT_TEST",
                                "EXIT_ROOM_FAIL",
                                t
                        );
                    }
                });
    }

    public void updateReadStatus(
            Long currentUserId
    ) {

        Log.d(
                TAG,
                "updateReadStatus 호출 roomId="
                        + roomId
                        + ", currentUserId="
                        + currentUserId
        );

        if (roomId == -1
                || currentUserId == null
                || currentUserId <= 0) {

            Log.d(
                    TAG,
                    "읽음 처리 중단"
            );

            return;
        }

        RetrofitClient.getApiService(activity)
                .updateReadTime(
                        roomId,
                        currentUserId
                )
                .enqueue(new Callback<Map<String, Object>>() {

                    @Override
                    public void onResponse(
                            @NonNull Call<Map<String, Object>> call,
                            @NonNull Response<Map<String, Object>> response
                    ) {

                        Log.d(
                                TAG,
                                "읽음 처리 응답 code="
                                        + response.code()
                        );

                        Log.d(
                                "CHAT_TEST",
                                "READ_RESPONSE code="
                                        + response.code()
                        );

                        if (response.body() != null) {

                            Log.d(
                                    "CHAT_TEST",
                                    "READ_RESPONSE_BODY="
                                            + response.body()
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            @NonNull Call<Map<String, Object>> call,
                            @NonNull Throwable t
                    ) {

                        Log.e(
                                TAG,
                                "읽음 처리 실패",
                                t
                        );

                        Log.e(
                                "CHAT_TEST",
                                "READ_FAIL",
                                t
                        );
                    }
                });
    }
}