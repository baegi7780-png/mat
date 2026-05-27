package com.tech.motjip.Adapter.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tech.motjip.Adapter.binder.MessageBindHelper;
import com.tech.motjip.Model.Message;
import com.tech.motjip.R;

public class UnreadDividerViewHolder
        extends BaseMessageViewHolder {

    private final TextView tvUnreadDivider;

    public UnreadDividerViewHolder(
            @NonNull View itemView
    ) {

        super(
                itemView
        );

        tvUnreadDivider =
                itemView.findViewById(
                        R.id.tv_unread_divider
                );
    }

    @Override
    public void bind(
            Message message,
            Message previousMessage,
            Message nextMessage
    ) {

        MessageBindHelper.applyMessageGroupSpacing(
                itemView,
                false
        );

        if (tvUnreadDivider != null) {

            tvUnreadDivider.setText(
                    "여기까지 읽음"
            );
        }
    }
}