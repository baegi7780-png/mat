package com.tech.motjip.Adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Model.ChatRoom;
import com.tech.motjip.R;
import com.tech.motjip.Utils.TimeUtil;
import com.bumptech.glide.Glide;

import java.util.List;

public class ChatRoomAdapter
        extends RecyclerView.Adapter<ChatRoomAdapter.ViewHolder> {

    public static final String PAYLOAD_FULL =
            "PAYLOAD_FULL";

    public static final String PAYLOAD_LAST_MESSAGE =
            "PAYLOAD_LAST_MESSAGE";

    public static final String PAYLOAD_TIME =
            "PAYLOAD_TIME";

    public static final String PAYLOAD_UNREAD_COUNT =
            "PAYLOAD_UNREAD_COUNT";

    private static final String TAG =
            "ChatRoomAdapter";

    private final List<ChatRoom> rooms;

    private final OnChatRoomClickListener listener;

    public interface OnChatRoomClickListener {

        void onChatRoomClick(
                ChatRoom room
        );
    }

    public ChatRoomAdapter(
            List<ChatRoom> rooms,
            OnChatRoomClickListener listener
    ) {

        this.rooms =
                rooms;

        this.listener =
                listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(
                                parent.getContext()
                        )
                        .inflate(
                                R.layout.item_chat_room,
                                parent,
                                false
                        );

        return new ViewHolder(
                view
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        if (rooms == null
                || position < 0
                || position >= rooms.size()) {

            Log.e(
                    TAG,
                    "onBindViewHolder 무시 - 잘못된 position="
                            + position
            );

            return;
        }

        ChatRoom room =
                rooms.get(
                        position
                );

        if (room == null) {

            Log.e(
                    TAG,
                    "onBindViewHolder 무시 - room null position="
                            + position
            );

            return;
        }

        bindFull(
                holder,
                room,
                position
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position,
            @NonNull List<Object> payloads
    ) {

        if (payloads.isEmpty()) {

            onBindViewHolder(
                    holder,
                    position
            );

            return;
        }

        if (rooms == null
                || position < 0
                || position >= rooms.size()) {

            Log.e(
                    TAG,
                    "payload onBindViewHolder 무시 - 잘못된 position="
                            + position
            );

            return;
        }

        ChatRoom room =
                rooms.get(
                        position
                );

        if (room == null) {

            Log.e(
                    TAG,
                    "payload onBindViewHolder 무시 - room null"
            );

            return;
        }

        boolean needsFullBind =
                false;

        boolean needsLastMessage =
                false;

        boolean needsTime =
                false;

        boolean needsUnread =
                false;

        for (Object payload : payloads) {

            if (payload instanceof List) {

                List<?> list =
                        (List<?>) payload;

                for (Object item : list) {

                    if (PAYLOAD_FULL.equals(item)) {
                        needsFullBind = true;
                    }

                    if (PAYLOAD_LAST_MESSAGE.equals(item)) {
                        needsLastMessage = true;
                    }

                    if (PAYLOAD_TIME.equals(item)) {
                        needsTime = true;
                    }

                    if (PAYLOAD_UNREAD_COUNT.equals(item)) {
                        needsUnread = true;
                    }
                }

            } else {

                if (PAYLOAD_FULL.equals(payload)) {
                    needsFullBind = true;
                }

                if (PAYLOAD_LAST_MESSAGE.equals(payload)) {
                    needsLastMessage = true;
                }

                if (PAYLOAD_TIME.equals(payload)) {
                    needsTime = true;
                }

                if (PAYLOAD_UNREAD_COUNT.equals(payload)) {
                    needsUnread = true;
                }
            }
        }

        if (needsFullBind) {

            bindFull(
                    holder,
                    room,
                    position
            );

            return;
        }

        if (needsLastMessage) {

            bindLastMessage(
                    holder,
                    room
            );
        }

        if (needsTime) {

            bindTime(
                    holder,
                    room
            );
        }

        if (needsUnread) {

            bindUnreadCount(
                    holder,
                    room
            );
        }

        bindClickListener(
                holder
        );
    }

    private void bindFull(
            @NonNull ViewHolder holder,
            @NonNull ChatRoom room,
            int position
    ) {

        Log.d(
                TAG,
                "onBindViewHolder position="
                        + position
                        + ", roomId="
                        + room.getRoomId()
                        + ", roomType="
                        + room.getRoomType()
                        + ", unreadCount="
                        + room.getUnreadCount()
        );

        bindProfiles(
                holder,
                room
        );

        bindRoomName(
                holder,
                room
        );

        bindLastMessage(
                holder,
                room
        );

        bindTime(
                holder,
                room
        );

        bindUnreadCount(
                holder,
                room
        );

        bindClickListener(
                holder
        );
    }


    private void bindProfiles(
            @NonNull ViewHolder holder,
            @NonNull ChatRoom room
    ) {

        holder.ivProfile1.setVisibility(
                View.INVISIBLE
        );

        holder.ivProfile2.setVisibility(
                View.INVISIBLE
        );

        holder.ivProfile3.setVisibility(
                View.INVISIBLE
        );

        holder.ivProfile4.setVisibility(
                View.INVISIBLE
        );

        List<String> profiles =
                room.getParticipantProfileImages();

        if (profiles == null
                || profiles.isEmpty()) {

            holder.ivProfile1.setVisibility(
                    View.VISIBLE
            );

            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.default_profile)
                    .circleCrop()
                    .into(holder.ivProfile1);

            return;
        }

        int size =
                Math.min(
                        profiles.size(),
                        4
                );

        ImageView[] imageViews = {
                holder.ivProfile1,
                holder.ivProfile2,
                holder.ivProfile3,
                holder.ivProfile4
        };

        if (size >= 1) {

            holder.ivProfile1.setVisibility(
                    View.VISIBLE
            );

            loadProfile(
                    holder.ivProfile1,
                    profiles.get(0)
            );
        }

        if (size >= 2) {

            holder.ivProfile2.setVisibility(
                    View.VISIBLE
            );

            loadProfile(
                    holder.ivProfile2,
                    profiles.get(1)
            );
        }

        if (size >= 3) {

            holder.ivProfile3.setVisibility(
                    View.VISIBLE
            );

            loadProfile(
                    holder.ivProfile3,
                    profiles.get(2)
            );
        }

        if (size >= 4) {

            holder.ivProfile4.setVisibility(
                    View.VISIBLE
            );

            loadProfile(
                    holder.ivProfile4,
                    profiles.get(3)
            );
        }

        if (size == 1) {

            holder.ivProfile1.setVisibility(
                    View.VISIBLE
            );
        }

        if (size == 2) {

            holder.ivProfile3.setVisibility(
                    View.GONE
            );

            holder.ivProfile4.setVisibility(
                    View.GONE
            );

        } else if (size == 3) {

            holder.ivProfile4.setVisibility(
                    View.GONE
            );
        }



    }

    private void loadProfile(
            ImageView imageView,
            String profileUrl
    ) {

        if (profileUrl != null
                && !profileUrl.startsWith("http")) {

            profileUrl =
                    RetrofitClient.BASE_URL
                            + profileUrl;
        }

        Glide.with(imageView.getContext())
                .load(profileUrl)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .circleCrop()
                .into(imageView);
    }

    private void bindClickListener(
            @NonNull ViewHolder holder
    ) {

        holder.itemView.setOnClickListener(v -> {

            Log.d(
                    "CHAT_CRASH_TRACE",
                    "ADAPTER_CLICK_START"
            );

            if (listener == null) {

                Log.e(
                        "CHAT_CRASH_TRACE",
                        "listener null"
                );

                return;
            }

            int currentPosition =
                    holder.getBindingAdapterPosition();

            Log.d(
                    "CHAT_CRASH_TRACE",
                    "currentPosition="
                            + currentPosition
            );

            if (currentPosition
                    == RecyclerView.NO_POSITION) {

                Log.e(
                        TAG,
                        "클릭 무시 - NO_POSITION"
                );

                return;
            }

            if (rooms == null
                    || currentPosition < 0
                    || currentPosition >= rooms.size()) {

                Log.e(
                        TAG,
                        "클릭 무시 - 잘못된 position="
                                + currentPosition
                );

                return;
            }

            ChatRoom clickedRoom =
                    rooms.get(
                            currentPosition
                    );

            if (clickedRoom == null) {

                Log.e(
                        "CHAT_CRASH_TRACE",
                        "clickedRoom null"
                );

                return;
            }

            Log.d(
                    "CHAT_CRASH_TRACE",
                    "ADAPTER_CLICK_OK position="
                            + currentPosition
                            + ", roomId="
                            + clickedRoom.getRoomId()
                            + ", roomName="
                            + clickedRoom.getRoomName()
                            + ", roomType="
                            + clickedRoom.getRoomType()
            );

            if (clickedRoom.getRoomId() == null
                    || clickedRoom.getRoomId() <= 0) {

                Log.e(
                        TAG,
                        "클릭 무시 - 잘못된 roomId"
                );

                return;
            }

            try {

                listener.onChatRoomClick(
                        clickedRoom
                );

                Log.d(
                        "CHAT_CRASH_TRACE",
                        "listener.onChatRoomClick 완료"
                );

            } catch (Exception e) {

                Log.e(
                        "CHAT_CRASH_TRACE",
                        "listener.onChatRoomClick crash",
                        e
                );

                throw e;
            }
        });
    }

    private void bindRoomName(
            @NonNull ViewHolder holder,
            @NonNull ChatRoom room
    ) {

        String displayName =
                getDisplayRoomName(
                        room
                );

        if (holder.tvRoomName != null) {

            holder.tvRoomName.setText(
                    displayName
            );
        }
    }

    private void bindLastMessage(
            @NonNull ViewHolder holder,
            @NonNull ChatRoom room
    ) {

        String lastMessage =
                room.getLastMessage();

        if ("IMAGE".equals(
                room.getLastMessageType()
        )) {

            lastMessage =
                    "📷 사진";

        } else if ("VIDEO".equals(
                room.getLastMessageType()
        )) {

            lastMessage =
                    "🎥 동영상";

        } else if (lastMessage == null
                || lastMessage.trim().isEmpty()) {

            lastMessage =
                    "아직 메시지가 없습니다.";
        }

        if (holder.tvLastMessage != null) {

            holder.tvLastMessage.setText(
                    lastMessage
            );
        }
    }

    private void bindTime(
            @NonNull ViewHolder holder,
            @NonNull ChatRoom room
    ) {

        String time =
                room.getTime();

        if (time == null
                || time.trim().isEmpty()) {

            time =
                    "";
        }

        if (holder.tvTime != null) {

            try {

                holder.tvTime.setText(
                        TimeUtil.formatChatTime(
                                time
                        )
                );

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "시간 포맷 실패",
                        e
                );

                holder.tvTime.setText(
                        ""
                );
            }
        }
    }

    private void bindUnreadCount(
            @NonNull ViewHolder holder,
            @NonNull ChatRoom room
    ) {

        if (holder.tvUnreadCount == null) {

            return;
        }

        if (room.getUnreadCount() > 0) {

            holder.tvUnreadCount.setVisibility(
                    View.VISIBLE
            );

            holder.tvUnreadCount.setText(
                    String.valueOf(
                            room.getUnreadCount()
                    )
            );

        } else {

            holder.tvUnreadCount.setVisibility(
                    View.GONE
            );
        }
    }

    public static String getDisplayRoomName(
            @NonNull ChatRoom room
    ) {

        String displayName;

        if ("DIRECT".equals(
                room.getRoomType()
        )) {

            displayName =
                    room.getOpponentNickname();

            if (displayName == null
                    || displayName.trim().isEmpty()) {

                displayName =
                        "알 수 없는 사용자";
            }

        } else {

            displayName =
                    room.getRoomName() != null
                            && !room.getRoomName()
                            .trim()
                            .isEmpty()
                            ? room.getRoomName()
                            : room.getRoomId() + "번 방";
        }

        return displayName;
    }





    @Override
    public int getItemCount() {

        if (rooms == null) {

            return 0;
        }

        return rooms.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        TextView tvRoomName;
        TextView tvLastMessage;

        ImageView ivProfile1;
        ImageView ivProfile2;
        ImageView ivProfile3;
        ImageView ivProfile4;
        TextView tvTime;
        TextView tvUnreadCount;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(
                    itemView
            );

            tvRoomName =
                    itemView.findViewById(
                            R.id.tv_room_name
                    );

            tvLastMessage =
                    itemView.findViewById(
                            R.id.tv_last_message
                    );

            tvTime =
                    itemView.findViewById(
                            R.id.tv_chat_time
                    );

            tvUnreadCount =
                    itemView.findViewById(
                            R.id.tv_unread_count
                    );


            ivProfile1 =
                    itemView.findViewById(
                            R.id.iv_profile_1
                    );

            ivProfile2 =
                    itemView.findViewById(
                            R.id.iv_profile_2
                    );

            ivProfile3 =
                    itemView.findViewById(
                            R.id.iv_profile_3
                    );

            ivProfile4 =
                    itemView.findViewById(
                            R.id.iv_profile_4
                    );

        }
    }
}