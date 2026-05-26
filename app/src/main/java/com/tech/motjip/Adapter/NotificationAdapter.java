package com.tech.motjip.Adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.MessageActivity;
import com.tech.motjip.Model.NotificationItem;
import com.tech.motjip.R;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NotificationAdapter
        extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    public interface OnNotificationActionListener {

        void onAcceptClick(
                NotificationItem item
        );

        void onRejectClick(
                NotificationItem item
        );

        void onReadClick(
                NotificationItem item,
                int position
        );

        void onSelectionModeChange(
                boolean isSelectionMode
        );
    }

    private final Context context;

    private final OnNotificationActionListener actionListener;

    private final List<NotificationItem> notificationList =
            new ArrayList<>();

    private boolean isSelectionMode = false;

    public NotificationAdapter(
            Context context,
            OnNotificationActionListener actionListener
    ) {

        this.context = context;

        this.actionListener =
                actionListener;
    }

    public void setNotifications(
            List<NotificationItem> notifications
    ) {

        notificationList.clear();

        if (notifications != null) {

            notificationList.addAll(notifications);
        }

        notifyDataSetChanged();
    }

    public boolean isSelectionMode() {

        return isSelectionMode;
    }

    public void setSelectionMode(
            boolean selectionMode
    ) {

        isSelectionMode = selectionMode;

        if (!selectionMode) {

            clearSelections();
        }

        notifyDataSetChanged();

        if (actionListener != null) {

            actionListener.onSelectionModeChange(
                    selectionMode
            );
        }
    }

    public void clearSelections() {

        for (NotificationItem item : notificationList) {

            item.setSelected(false);
        }
    }

    public int getSelectedCount() {

        int count = 0;

        for (NotificationItem item : notificationList) {

            if (item.isSelected()) {

                count++;
            }
        }

        return count;
    }

    public List<Long> getSelectedNotificationIds() {

        List<Long> selectedIds =
                new ArrayList<>();

        for (NotificationItem item : notificationList) {

            if (item.isSelected()) {

                selectedIds.add(
                        item.getNotificationId()
                );
            }
        }

        return selectedIds;
    }

    public List<Long> getReadNotificationIds() {

        List<Long> readIds =
                new ArrayList<>();

        for (NotificationItem item : notificationList) {

            if (item.isRead()) {

                readIds.add(
                        item.getNotificationId()
                );
            }
        }

        return readIds;
    }

    public void removeSelectedNotifications() {

        List<NotificationItem> removeList =
                new ArrayList<>();

        for (NotificationItem item : notificationList) {

            if (item.isSelected()) {

                removeList.add(item);
            }
        }

        notificationList.removeAll(removeList);

        notifyDataSetChanged();
    }

    public void removeReadNotifications() {

        List<NotificationItem> removeList =
                new ArrayList<>();

        for (NotificationItem item : notificationList) {

            if (item.isRead()) {

                removeList.add(item);
            }
        }

        notificationList.removeAll(removeList);

        notifyDataSetChanged();
    }

    public NotificationItem getNotification(
            int position
    ) {

        return notificationList.get(position);
    }

    public void removeNotification(
            int position
    ) {

        notificationList.remove(position);

        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.item_notification,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        NotificationItem item =
                notificationList.get(position);

        holder.tvMessage.setText(
                item.getMessage()
        );

        holder.tvTime.setText(
                formatKoreanDateTime(
                        item.getCreatedAt()
                )
        );

        if (isSelectionMode) {

            holder.cbSelect.setVisibility(
                    View.VISIBLE
            );

        } else {

            holder.cbSelect.setVisibility(
                    View.GONE
            );
        }

        holder.cbSelect.setOnCheckedChangeListener(null);

        holder.cbSelect.setChecked(
                item.isSelected()
        );

        holder.cbSelect.setOnCheckedChangeListener(
                (buttonView, isChecked) ->
                        item.setSelected(isChecked)
        );

        if (!item.isRead()) {

            holder.tvUnread.setVisibility(
                    View.VISIBLE
            );

        } else {

            holder.tvUnread.setVisibility(
                    View.GONE
            );
        }

        if ("PENDING".equals(item.getStatus())
                && (
                "FRIEND_INVITE".equals(item.getType())
                        || "COMMUNITY_INVITE".equals(item.getType())
        )) {

            holder.layoutActions.setVisibility(
                    View.VISIBLE
            );

        } else {

            holder.layoutActions.setVisibility(
                    View.GONE
            );
        }

        holder.itemView.setOnLongClickListener(v -> {

            if (!isSelectionMode) {

                setSelectionMode(true);

                item.setSelected(true);

                notifyItemChanged(position);

                return true;
            }

            return false;
        });

        holder.itemView.setOnClickListener(v -> {

            if (isSelectionMode) {

                item.setSelected(
                        !item.isSelected()
                );

                notifyItemChanged(position);

                if (getSelectedCount() == 0) {

                    setSelectionMode(false);
                }

                return;
            }

            int adapterPosition =
                    holder.getAdapterPosition();

            if (adapterPosition == RecyclerView.NO_POSITION) {

                return;
            }

            if (actionListener != null
                    && !item.isRead()) {

                actionListener.onReadClick(
                        item,
                        adapterPosition
                );
            }

            if ("CHAT_INVITE".equals(item.getType())
                    && item.getTargetId() != null) {

                Intent intent =
                        new Intent(
                                context,
                                MessageActivity.class
                        );

                intent.putExtra(
                        "roomId",
                        item.getTargetId()
                );

                intent.putExtra(
                        "roomName",
                        "채팅방"
                );

                context.startActivity(intent);
            }
        });

        holder.btnAccept.setOnClickListener(v -> {

            if (actionListener != null) {

                actionListener.onAcceptClick(
                        item
                );
            }
        });

        holder.btnReject.setOnClickListener(v -> {

            if (actionListener != null) {

                actionListener.onRejectClick(
                        item
                );
            }
        });

        switch (item.getType()) {

            case "FRIEND_INVITE":

                holder.tvType.setText(
                        "친구 요청"
                );

                holder.tvType.setTextColor(
                        Color.parseColor("#FFC107")
                );

                break;

            case "CHAT_INVITE":

                holder.tvType.setText(
                        "채팅 초대"
                );

                holder.tvType.setTextColor(
                        Color.parseColor("#4CAF50")
                );

                break;

            case "COMMUNITY_INVITE":

                holder.tvType.setText(
                        "모임 초대"
                );

                holder.tvType.setTextColor(
                        Color.parseColor("#FF8A3D")
                );

                break;

            default:

                holder.tvType.setText(
                        "알림"
                );

                holder.tvType.setTextColor(
                        Color.parseColor("#666666")
                );

                break;
        }
    }

    private String formatKoreanDateTime(
            String dateTime
    ) {

        try {

            LocalDateTime localDateTime =
                    LocalDateTime.parse(dateTime);

            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern(
                            "yyyy년 M월 d일 a h:mm",
                            Locale.KOREAN
                    );

            return localDateTime.format(formatter);

        } catch (Exception e) {

            e.printStackTrace();

            return dateTime;
        }
    }

    @Override
    public int getItemCount() {

        return notificationList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvType;
        TextView tvMessage;
        TextView tvTime;
        TextView tvUnread;

        View layoutActions;

        Button btnAccept;
        Button btnReject;

        CheckBox cbSelect;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            tvType =
                    itemView.findViewById(
                            R.id.tvType
                    );

            tvMessage =
                    itemView.findViewById(
                            R.id.tvMessage
                    );

            tvTime =
                    itemView.findViewById(
                            R.id.tvTime
                    );

            tvUnread =
                    itemView.findViewById(
                            R.id.tvUnread
                    );

            layoutActions =
                    itemView.findViewById(
                            R.id.layoutActions
                    );

            btnAccept =
                    itemView.findViewById(
                            R.id.btnAccept
                    );

            btnReject =
                    itemView.findViewById(
                            R.id.btnReject
                    );

            cbSelect =
                    itemView.findViewById(
                            R.id.cbSelect
                    );
        }
    }
}