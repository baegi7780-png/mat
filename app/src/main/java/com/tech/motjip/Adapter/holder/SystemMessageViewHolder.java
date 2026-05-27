package com.tech.motjip.Adapter.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tech.motjip.Adapter.binder.MessageBindHelper;
import com.tech.motjip.Model.Message;
import com.tech.motjip.R;

public class SystemMessageViewHolder
        extends BaseMessageViewHolder {

    private final TextView tvMessageContent;

    public SystemMessageViewHolder(
            @NonNull View itemView
    ) {

        super(
                itemView
        );

        tvMessageContent =
                itemView.findViewById(
                        R.id.tv_message_content
                );
    }

    @Override
    public void bind(
            Message message,
            Message previousMessage,
            Message nextMessage
    ) {

        if (message == null) {

            return;
        }

        MessageBindHelper.applyMessageGroupSpacing(
                itemView,
                false
        );

        if (tvMessageContent != null) {

            tvMessageContent.setText(
                    message.getMessageContent() != null
                            ? message.getMessageContent()
                            : ""
            );
        }
    }
}