package com.tech.motjip.Adapter.holder;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.Model.Message;

import java.util.List;

public abstract class BaseMessageViewHolder
        extends RecyclerView.ViewHolder {

    public BaseMessageViewHolder(
            @NonNull View itemView
    ) {

        super(
                itemView
        );
    }

    public abstract void bind(
            Message message,
            Message previousMessage,
            Message nextMessage
    );

    public void bindPayload(
            Message message,
            Message previousMessage,
            Message nextMessage,
            List<Object> payloads
    ) {

        bind(
                message,
                previousMessage,
                nextMessage
        );
    }
}