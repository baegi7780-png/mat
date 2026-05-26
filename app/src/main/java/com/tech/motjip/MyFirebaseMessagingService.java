package com.tech.motjip;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Dto.RequestDto.FcmTokenRequestDto;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG =
            "FCM_SERVICE";

    private static final String CHANNEL_ID =
            "motjip_fcm_channel";

    private static final String CHANNEL_NAME =
            "맛집 알림";

    @Override
    public void onNewToken(
            @NonNull String token
    ) {

        super.onNewToken(
                token
        );

        Log.d(
                TAG,
                "FCM Token : " + token
        );

        saveFcmTokenToServer(
                token
        );
    }

    @Override
    public void onMessageReceived(
            @NonNull RemoteMessage message
    ) {

        super.onMessageReceived(
                message
        );

        Log.d(
                TAG,
                "알림 수신 완료"
        );

        Map<String, String> data =
                message.getData();

        String title =
                getDataValue(
                        data,
                        "title"
                );

        String body =
                getDataValue(
                        data,
                        "body"
                );

        if (title == null) {

            title =
                    "맛집";
        }

        if (body == null) {

            body =
                    "새 알림이 도착했습니다.";
        }

        if (message.getNotification() != null) {

            if (message.getNotification().getTitle() != null
                    && title.trim().isEmpty()) {

                title =
                        message.getNotification()
                                .getTitle();
            }

            if (message.getNotification().getBody() != null
                    && body.trim().isEmpty()) {

                body =
                        message.getNotification()
                                .getBody();
            }
        }

        String type =
                getDataValue(
                        data,
                        "type"
                );

        String roomId =
                getDataValue(
                        data,
                        "roomId"
                );

        String roomName =
                getDataValue(
                        data,
                        "roomName"
                );

        String roomType =
                getDataValue(
                        data,
                        "roomType"
                );

        Log.d(
                TAG,
                "FCM data title="
                        + title
                        + ", body="
                        + body
                        + ", type="
                        + type
                        + ", roomId="
                        + roomId
                        + ", roomName="
                        + roomName
                        + ", roomType="
                        + roomType
        );

        if ("CHAT_MESSAGE".equals(type)
                && isCurrentlyViewingRoom(roomId)) {

            Log.d(
                    TAG,
                    "현재 보고 있는 채팅방 알림 무시 roomId="
                            + roomId
            );

            return;
        }

        showNotification(
                title,
                body,
                type,
                roomId,
                roomName,
                roomType
        );
    }

    private boolean isCurrentlyViewingRoom(
            String roomId
    ) {

        Long parsedRoomId =
                parseLongOrNull(
                        roomId
                );

        if (parsedRoomId == null
                || parsedRoomId <= 0) {

            return false;
        }

        SharedPreferences prefs =
                getSharedPreferences(
                        "chat_state",
                        Context.MODE_PRIVATE
                );

        long activeRoomId =
                prefs.getLong(
                        "activeChatRoomId",
                        -1L
                );

        boolean isViewing =
                activeRoomId == parsedRoomId;

        Log.d(
                TAG,
                "현재 보고있는 채팅방 확인 activeRoomId="
                        + activeRoomId
                        + ", pushRoomId="
                        + parsedRoomId
                        + ", isViewing="
                        + isViewing
        );

        return isViewing;
    }

    private String getDataValue(
            Map<String, String> data,
            String key
    ) {

        if (data == null
                || key == null) {

            return null;
        }

        String value =
                data.get(
                        key
                );

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        return value.trim();
    }

    private void showNotification(
            String title,
            String body,
            String type,
            String roomId,
            String roomName,
            String roomType
    ) {

        createNotificationChannel();

        Intent intent =
                createNotificationClickIntent(
                        type,
                        roomId,
                        roomName,
                        roomType
                );

        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        createNotificationRequestCode(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                                | PendingIntent.FLAG_IMMUTABLE
                );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                R.mipmap.ic_launcher
                        )
                        .setContentTitle(
                                title
                        )
                        .setContentText(
                                body
                        )
                        .setStyle(
                                new NotificationCompat.BigTextStyle()
                                        .bigText(
                                                body
                                        )
                        )
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setAutoCancel(
                                true
                        )
                        .setContentIntent(
                                pendingIntent
                        );

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(
                        this
                );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                Log.d(
                        TAG,
                        "알림 권한 없음"
                );

                return;
            }
        }

        notificationManager.notify(
                createNotificationId(),
                builder.build()
        );
    }

    private Intent createNotificationClickIntent(
            String type,
            String roomId,
            String roomName,
            String roomType
    ) {

        Long parsedRoomId =
                parseLongOrNull(
                        roomId
                );

        if ("CHAT_MESSAGE".equals(type)
                && parsedRoomId != null
                && parsedRoomId > 0) {

            Intent intent =
                    new Intent(
                            this,
                            MessageActivity.class
                    );

            intent.putExtra(
                    "roomId",
                    parsedRoomId
            );

            if (roomName != null
                    && !roomName.trim().isEmpty()) {

                intent.putExtra(
                        "roomName",
                        roomName
                );
            }

            if (roomType != null
                    && !roomType.trim().isEmpty()) {

                intent.putExtra(
                        "roomType",
                        roomType
                );
            }

            intent.putExtra(
                    "notificationType",
                    type
            );

            intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP
                            | Intent.FLAG_ACTIVITY_SINGLE_TOP
                            | Intent.FLAG_ACTIVITY_NEW_TASK
            );

            Log.d(
                    TAG,
                    "CHAT_MESSAGE → MessageActivity 이동 roomId="
                            + parsedRoomId
            );

            return intent;
        }

        Intent intent =
                new Intent(
                        this,
                        NotificationActivity.class
                );

        intent.putExtra(
                "notificationType",
                type
        );

        intent.addFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        | Intent.FLAG_ACTIVITY_NEW_TASK
        );

        Log.d(
                TAG,
                "알림 페이지 이동 type="
                        + type
        );

        return intent;
    }

    private Long parseLongOrNull(
            String value
    ) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            return Long.parseLong(
                    value.trim()
            );

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "roomId 파싱 실패 value="
                            + value,
                    e
            );
        }

        return null;
    }

    private int createNotificationId() {

        return (int) (
                System.currentTimeMillis()
                        % Integer.MAX_VALUE
        );
    }

    private int createNotificationRequestCode() {

        return (int) (
                System.nanoTime()
                        % Integer.MAX_VALUE
        );
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            CHANNEL_NAME,
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "맛집 앱 푸시 알림 채널"
            );

            NotificationManager manager =
                    getSystemService(
                            NotificationManager.class
                    );

            if (manager != null) {

                manager.createNotificationChannel(
                        channel
                );
            }
        }
    }

    private void saveFcmTokenToServer(
            String token
    ) {

        SharedPreferences prefs =
                getSharedPreferences(
                        "auth",
                        Context.MODE_PRIVATE
                );

        String accessToken =
                prefs.getString(
                        "accessToken",
                        null
                );

        if (accessToken == null
                || accessToken.trim().isEmpty()) {

            Log.d(
                    TAG,
                    "AccessToken 없음 - FCM Token 서버 저장 보류"
            );

            return;
        }

        ApiService apiService =
                RetrofitClient.getApiService(
                        this
                );

        FcmTokenRequestDto requestDto =
                new FcmTokenRequestDto(
                        token
                );

        apiService.updateFcmToken(
                requestDto
        ).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(
                    Call<Void> call,
                    Response<Void> response
            ) {

                if (response.isSuccessful()) {

                    Log.d(
                            TAG,
                            "FCM Token 서버 저장 성공"
                    );

                } else {

                    Log.d(
                            TAG,
                            "FCM Token 서버 저장 실패 code : "
                                    + response.code()
                    );
                }
            }

            @Override
            public void onFailure(
                    Call<Void> call,
                    Throwable t
            ) {

                Log.d(
                        TAG,
                        "FCM Token 서버 저장 통신 실패 : "
                                + t.getMessage()
                );
            }
        });
    }
}