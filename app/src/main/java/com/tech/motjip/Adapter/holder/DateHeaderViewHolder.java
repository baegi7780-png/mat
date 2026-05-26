package com.tech.motjip.Adapter.holder;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.tech.motjip.Adapter.binder.MessageBindHelper;
import com.tech.motjip.Model.Message;
import com.tech.motjip.R;

public class DateHeaderViewHolder
        extends BaseMessageViewHolder {

    private final TextView tvDateHeader;

    public DateHeaderViewHolder(
            @NonNull View itemView
    ) {

        super(
                itemView
        );

        tvDateHeader =
                itemView.findViewById(
                        R.id.tv_date_header
                );
    }

    @Override
    public void bind(
            Message message,
            Message previousMessage,
            Message nextMessage
    ) {

        if (message == null
                || tvDateHeader == null) {

            return;
        }

        MessageBindHelper.applyMessageGroupSpacing(
                itemView,
                false
        );

        tvDateHeader.setText(
                message.getDateHeaderText() != null
                        ? message.getDateHeaderText()
                        : ""
        );
    }
}