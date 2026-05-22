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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM_SERVICE";

    private static final String CHANNEL_ID = "motjip_fcm_channel";
    private static final String CHANNEL_NAME = "맛집 알림";
    private static final int NOTIFICATION_ID = 1001;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        Log.d(TAG, "FCM Token : " + token);

        saveFcmTokenToServer(token);
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Log.d(TAG, "알림 수신 완료");

        String title = "맛집";
        String body = "새 알림이 도착했습니다.";

        if (message.getNotification() != null) {

            if (message.getNotification().getTitle() != null) {
                title = message.getNotification().getTitle();
            }

            if (message.getNotification().getBody() != null) {
                body = message.getNotification().getBody();
            }
        }

        showNotification(title, body);
    }

    private void showNotification(String title, String body) {

        createNotificationChannel();

        // 👉 알림 클릭 시 이동할 화면
        Intent intent = new Intent(this, NotificationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED) {

                Log.d(TAG, "알림 권한 없음");
                return;
            }
        }

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );

            channel.setDescription("맛집 앱 푸시 알림 채널");

            NotificationManager manager =
                    getSystemService(NotificationManager.class);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    private void saveFcmTokenToServer(String token) {

        SharedPreferences prefs =
                getSharedPreferences("auth", Context.MODE_PRIVATE);

        String accessToken = prefs.getString("accessToken", null);

        if (accessToken == null || accessToken.trim().isEmpty()) {
            Log.d(TAG, "AccessToken 없음 - FCM Token 서버 저장 보류");
            return;
        }

        ApiService apiService = RetrofitClient.getApiService(this);

        FcmTokenRequestDto requestDto = new FcmTokenRequestDto(token);

        apiService.updateFcmToken(requestDto).enqueue(new Callback<Void>() {

            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {

                if (response.isSuccessful()) {
                    Log.d(TAG, "FCM Token 서버 저장 성공");
                } else {
                    Log.d(TAG, "FCM Token 서버 저장 실패 code : " + response.code());
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Log.d(TAG, "FCM Token 서버 저장 통신 실패 : " + t.getMessage());
            }
        });
    }
}