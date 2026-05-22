package com.tech.motjip.Utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.tech.motjip.R;

public class DialogUtil {

    public interface DialogCallback {

        void onConfirm();
    }

    public static void showMessageDialog(
            Context context,
            int iconRes,
            String title,
            String message,
            DialogCallback callback
    ) {

        View view =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.dialog_common,
                                null
                        );

        ImageView ivIcon =
                view.findViewById(
                        R.id.ivDialogIcon
                );

        TextView tvTitle =
                view.findViewById(
                        R.id.tvDialogTitle
                );

        TextView tvMessage =
                view.findViewById(
                        R.id.tvDialogMessage
                );

        Button btnConfirm =
                view.findViewById(
                        R.id.btnDialogConfirm
                );

        ivIcon.setImageResource(
                iconRes
        );

        tvTitle.setText(
                title
        );

        tvMessage.setText(
                message
        );

        AlertDialog dialog =
                new AlertDialog.Builder(context)
                        .setView(view)
                        .setCancelable(false)
                        .create();

        if (dialog.getWindow() != null) {

            dialog.getWindow()
                    .setBackgroundDrawableResource(
                            android.R.color.transparent
                    );
        }

        btnConfirm.setOnClickListener(v -> {

            dialog.dismiss();

            if (callback != null) {

                callback.onConfirm();
            }
        });

        dialog.show();
    }

    public static void showConfirmDialog(
            Context context,
            int iconRes,
            String title,
            String message,
            DialogCallback callback
    ) {

        View view =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.dialog_confirm,
                                null
                        );

        ImageView ivIcon =
                view.findViewById(
                        R.id.ivDialogIcon
                );

        TextView tvTitle =
                view.findViewById(
                        R.id.tvDialogTitle
                );

        TextView tvMessage =
                view.findViewById(
                        R.id.tvDialogMessage
                );

        Button btnCancel =
                view.findViewById(
                        R.id.btnDialogCancel
                );

        Button btnConfirm =
                view.findViewById(
                        R.id.btnDialogConfirm
                );

        ivIcon.setImageResource(
                iconRes
        );

        tvTitle.setText(
                title
        );

        tvMessage.setText(
                message
        );

        AlertDialog dialog =
                new AlertDialog.Builder(context)
                        .setView(view)
                        .setCancelable(false)
                        .create();

        if (dialog.getWindow() != null) {

            dialog.getWindow()
                    .setBackgroundDrawableResource(
                            android.R.color.transparent
                    );
        }

        btnCancel.setOnClickListener(v -> {

            dialog.dismiss();
        });

        btnConfirm.setOnClickListener(v -> {

            dialog.dismiss();

            if (callback != null) {

                callback.onConfirm();
            }
        });

        dialog.show();
    }
}