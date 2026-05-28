package com.tech.motjip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        SharedPreferences prefs =
                getSharedPreferences(
                        "app_prefs",
                        MODE_PRIVATE
                );

        boolean isSplashShown =
                prefs.getBoolean(
                        "isSplashShown",
                        false
                );

        // 이미 스플래시를 본 경우
        if (isSplashShown) {

            moveToMain();

            return;
        }

        // 첫 실행인 경우
        prefs.edit()
                .putBoolean(
                        "isSplashShown",
                        true
                )
                .apply();

        setContentView(R.layout.activity_splash);

        VideoView videoView =
                findViewById(R.id.videoView);

        Uri videoUri = Uri.parse(
                "android.resource://"
                        + getPackageName()
                        + "/"
                        + R.raw.bowl1
        );

        videoView.setVideoURI(videoUri);

        videoView.setOnPreparedListener(mp -> {

            mp.setVideoScalingMode(
                    MediaPlayer.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING
            );

            videoView.start();
        });

        videoView.setOnCompletionListener(mediaPlayer -> {

            moveToMain();
        });
    }

    private void moveToMain() {

        Intent intent =
                new Intent(
                        SplashActivity.this,
                        MainActivity.class
                );

        intent.setFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(intent);

        finish();
    }
}