package com.tech.motjip.Controller;

import android.content.Context;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Dto.RequestDto.DeleteNotificationRequestDto;
import com.tech.motjip.Model.NotificationItem;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationController {

    private final ApiService apiService;

    public NotificationController(
            Context context
    ) {

        apiService =
                RetrofitClient.getApiService(context);
    }

    public interface NotificationListCallback {

        void onSuccess(
                List<NotificationItem> notifications
        );

        void onError(
                String message
        );
    }

    public interface NotificationActionCallback {

        void onSuccess();

        void onError(
                String message
        );
    }

    public void getNotifications(
            NotificationListCallback callback
    ) {

        apiService.getNotifications()
                .enqueue(new Callback<List<NotificationItem>>() {

                    @Override
                    public void onResponse(
                            Call<List<NotificationItem>> call,
                            Response<List<NotificationItem>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            callback.onSuccess(
                                    response.body()
                            );

                        } else {

                            callback.onError(
                                    "알림 목록을 불러오지 못했습니다."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<NotificationItem>> call,
                            Throwable t
                    ) {

                        callback.onError(
                                "서버 연결 실패"
                        );
                    }
                });
    }

    public void markAsRead(
            Long notificationId,
            NotificationActionCallback callback
    ) {

        apiService.markNotificationAsRead(
                        notificationId
                )
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            callback.onSuccess();

                        } else {

                            callback.onError(
                                    "알림 읽음 처리에 실패했습니다."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        callback.onError(
                                "서버 연결 실패"
                        );
                    }
                });
    }

    public void deleteNotification(
            Long notificationId,
            NotificationActionCallback callback
    ) {

        apiService.deleteNotification(
                        notificationId
                )
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            callback.onSuccess();

                        } else {

                            callback.onError(
                                    "알림 삭제에 실패했습니다."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        callback.onError(
                                "서버 연결 실패"
                        );
                    }
                });
    }

    public void deleteNotifications(
            List<Long> notificationIds,
            NotificationActionCallback callback
    ) {

        DeleteNotificationRequestDto requestDto =
                new DeleteNotificationRequestDto(
                        notificationIds
                );

        apiService.deleteNotifications(
                        requestDto
                )
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            callback.onSuccess();

                        } else {

                            callback.onError(
                                    "선택 삭제에 실패했습니다."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        callback.onError(
                                "서버 연결 실패"
                        );
                    }
                });
    }

    public void respondFriendRequest(
            Long friendRequestId,
            String status,
            NotificationActionCallback callback
    ) {

        apiService.respondFriendRequest(
                        friendRequestId,
                        status
                )
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            callback.onSuccess();

                        } else if (response.code() == 409) {

                            callback.onError(
                                    "이미 처리된 친구 요청입니다."
                            );

                        } else {

                            callback.onError(
                                    "친구 요청 처리에 실패했습니다."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        callback.onError(
                                "서버 연결 실패"
                        );
                    }
                });
    }

    public void respondCommunityInvite(
            Long communityInviteId,
            String status,
            NotificationActionCallback callback
    ) {

        apiService.respondCommunityInvite(
                        communityInviteId,
                        status
                )
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            callback.onSuccess();

                        } else if (response.code() == 409) {

                            callback.onError(
                                    "이미 처리된 모임 초대입니다."
                            );

                        } else {

                            callback.onError(
                                    "모임 초대 처리에 실패했습니다."
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        callback.onError(
                                "서버 연결 실패"
                        );
                    }
                });
    }
}