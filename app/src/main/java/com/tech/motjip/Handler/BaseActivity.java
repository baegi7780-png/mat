package com.tech.motjip.Handler;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import javax.inject.Inject;

/**
 * 각 액티비티에서 공통된 로직을 사용하기위한 추상 베이스클래스
 */
public abstract class BaseActivity extends AppCompatActivity {

    // 각 액티비티 추상클래스 상속 필수
    @Inject
    protected BackKeyHandler backKeyHandler;

    private ActivityResultLauncher<String> notificationPermissionLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notificationPermissionLauncher =
                registerForActivityResult(
                        new ActivityResultContracts.RequestPermission(),
                        isGranted -> {
                            // 알림 권한 허용/거부 후 별도 처리 필요 없으면 비워둬도 됨
                        }
                );

        requestNotificationPermission();

        // 뒤로가기키 처리
        if (backKeyHandler != null) {
            backKeyHandler.handleBackkey(this);
        }
    }

    private void requestNotificationPermission() {

        if (Build.VERSION.SDK_INT
                < Build.VERSION_CODES.TIRAMISU) {

            return;
        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED) {

            return;
        }

        notificationPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
        );
    }
}