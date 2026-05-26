package com.tech.motjip.Utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.view.Window;
import android.view.WindowManager;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

public class ImagePreviewDialog {

    public static void show(
            Context context,
            String imageUrl
    ) {

        if (context == null
                || imageUrl == null
                || imageUrl.trim().isEmpty()) {

            return;
        }

        Dialog dialog =
                new Dialog(context);

        dialog.requestWindowFeature(
                Window.FEATURE_NO_TITLE
        );

        PhotoView photoView =
                new PhotoView(context);

        photoView.setBackgroundColor(
                Color.BLACK
        );

        photoView.setOnClickListener(v ->
                dialog.dismiss()
        );

        dialog.setContentView(
                photoView
        );

        Window window =
                dialog.getWindow();

        if (window != null) {

            window.setBackgroundDrawableResource(
                    android.R.color.black
            );

            window.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
            );
        }

        dialog.setOnShowListener(d -> {

            Window showWindow =
                    dialog.getWindow();

            if (showWindow != null) {

                showWindow.setLayout(
                        WindowManager.LayoutParams.MATCH_PARENT,
                        WindowManager.LayoutParams.MATCH_PARENT
                );

                showWindow.setBackgroundDrawableResource(
                        android.R.color.black
                );
            }
        });

        Glide.with(context)
                .load(imageUrl)
                .fitCenter()
                .into(photoView);

        dialog.show();
    }
}