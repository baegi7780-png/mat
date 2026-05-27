package com.tech.motjip;

import android.os.Bundle;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

public class ImagePreviewActivity
        extends AppCompatActivity {

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_image_preview
        );

        ImageView ivPreview =
                findViewById(
                        R.id.iv_preview
                );

        String imageUrl =
                getIntent().getStringExtra(
                        "imageUrl"
                );

        Glide.with(this)
                .load(
                        "http://10.0.2.2:8080"
                                + imageUrl
                )
                .into(ivPreview);
    }
}