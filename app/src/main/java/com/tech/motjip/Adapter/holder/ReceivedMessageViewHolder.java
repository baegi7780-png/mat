package com.tech.motjip.Adapter.holder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tech.motjip.Adapter.MessageAdapter;
import com.tech.motjip.Adapter.binder.MessageBindHelper;
import com.tech.motjip.Model.Message;
import com.tech.motjip.R;

import java.util.List;

public class ReceivedMessageViewHolder
        extends BaseMessageViewHolder {

    private final ImageView ivProfile;
    private final TextView tvSenderName;
    private final TextView tvMessageContent;
    private final TextView tvUnreadCount;
    private final TextView tvTime;
    private final ImageView ivMessageImage;

    public ReceivedMessageViewHolder(
            @NonNull View itemView
    ) {
        super(itemView);

        ivProfile = itemView.findViewById(R.id.iv_profile);
        tvSenderName = itemView.findViewById(R.id.tv_sender_name);
        tvMessageContent = itemView.findViewById(R.id.tv_message_content);
        tvUnreadCount = itemView.findViewById(R.id.tv_unread_count);
        tvTime = itemView.findViewById(R.id.tv_time);
        ivMessageImage = itemView.findViewById(R.id.iv_message_image);
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

        boolean isContinuousFromPrevious =
                MessageBindHelper.isContinuousMessage(
                        message,
                        previousMessage
                );

        boolean isContinuousToNext =
                MessageBindHelper.isContinuousMessage(
                        nextMessage,
                        message
                );

        MessageBindHelper.applyMessageGroupSpacing(
                itemView,
                isContinuousFromPrevious
        );

        MessageBindHelper.bindProfileAndSenderName(
                message,
                ivProfile,
                tvSenderName,
                isContinuousFromPrevious
        );

        MessageBindHelper.bindTextOrImageMessage(
                message,
                tvMessageContent,
                ivMessageImage
        );

        MessageBindHelper.bindTime(
                message,
                tvTime,
                isContinuousToNext
        );

        MessageBindHelper.bindUnreadCount(
                message,
                tvUnreadCount,
                false
        );
    }

    @Override
    public void bindPayload(
            Message message,
            Message previousMessage,
            Message nextMessage,
            List<Object> payloads
    ) {

        if (message == null
                || payloads == null
                || payloads.isEmpty()) {

            bind(
                    message,
                    previousMessage,
                    nextMessage
            );

            return;
        }

        boolean hasUnreadPayload =
                payloads.contains(
                        MessageAdapter.PAYLOAD_UNREAD_COUNT
                );

        if (hasUnreadPayload
                && payloads.size() == 1) {

            MessageBindHelper.bindUnreadCount(
                    message,
                    tvUnreadCount,
                    false
            );

            return;
        }

        bind(
                message,
                previousMessage,
                nextMessage
        );
    }
}