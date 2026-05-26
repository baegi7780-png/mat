package com.tech.motjip.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;

import com.tech.motjip.Adapter.holder.BaseMessageViewHolder;
import com.tech.motjip.Adapter.holder.DateHeaderViewHolder;
import com.tech.motjip.Adapter.holder.ReceivedMessageViewHolder;
import com.tech.motjip.Adapter.holder.SentMessageViewHolder;
import com.tech.motjip.Adapter.holder.SystemMessageViewHolder;
import com.tech.motjip.Adapter.holder.UnreadDividerViewHolder;
import com.tech.motjip.Model.Message;
import com.tech.motjip.R;
import com.tech.motjip.Utils.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter
        extends ListAdapter<Message, BaseMessageViewHolder> {

    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    private static final int VIEW_TYPE_SYSTEM = 3;
    private static final int VIEW_TYPE_DATE_HEADER = 4;
    private static final int VIEW_TYPE_UNREAD_DIVIDER = 5;

    public static final String PAYLOAD_UNREAD_COUNT =
            "PAYLOAD_UNREAD_COUNT";

    public static final String PAYLOAD_PROFILE =
            "PAYLOAD_PROFILE";

    public static final String PAYLOAD_CONTENT =
            "PAYLOAD_CONTENT";

    private static final String MESSAGE_TYPE_UNREAD_DIVIDER =
            "__UNREAD_DIVIDER__";

    private final Long myUserId;

    private Long firstUnreadMessageId;

    public MessageAdapter(
            Long myUserId
    ) {

        super(
                DIFF_CALLBACK
        );

        this.myUserId =
                myUserId;
    }

    public void setFirstUnreadMessageId(
            Long firstUnreadMessageId
    ) {

        this.firstUnreadMessageId =
                firstUnreadMessageId;
    }

    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Message>() {

                @Override
                public boolean areItemsTheSame(
                        @NonNull Message oldItem,
                        @NonNull Message newItem
                ) {

                    if (isUnreadDivider(oldItem)
                            || isUnreadDivider(newItem)) {

                        return isUnreadDivider(oldItem)
                                && isUnreadDivider(newItem);
                    }

                    if (oldItem.isDateHeader()
                            || newItem.isDateHeader()) {

                        return oldItem.isDateHeader()
                                && newItem.isDateHeader()
                                && safeEquals(
                                oldItem.getDateHeaderKey(),
                                newItem.getDateHeaderKey()
                        );
                    }

                    if (oldItem.getId() != null
                            && newItem.getId() != null
                            && oldItem.getId().equals(
                            newItem.getId()
                    )) {

                        return true;
                    }

                    return isSamePendingAndServerMessage(
                            oldItem,
                            newItem
                    );
                }

                @Override
                public boolean areContentsTheSame(
                        @NonNull Message oldItem,
                        @NonNull Message newItem
                ) {

                    if (isUnreadDivider(oldItem)
                            || isUnreadDivider(newItem)) {

                        return isUnreadDivider(oldItem)
                                && isUnreadDivider(newItem);
                    }

                    if (oldItem.isDateHeader()
                            || newItem.isDateHeader()) {

                        return oldItem.isDateHeader()
                                && newItem.isDateHeader()
                                && safeEquals(
                                oldItem.getDateHeaderKey(),
                                newItem.getDateHeaderKey()
                        )
                                && safeEquals(
                                oldItem.getDateHeaderText(),
                                newItem.getDateHeaderText()
                        );
                    }

                    return safeEquals(
                            oldItem.getId(),
                            newItem.getId()
                    )
                            && safeEquals(
                            oldItem.getMessageContent(),
                            newItem.getMessageContent()
                    )
                            && safeEquals(
                            oldItem.getSenderId(),
                            newItem.getSenderId()
                    )
                            && safeEquals(
                            oldItem.getSenderNickname(),
                            newItem.getSenderNickname()
                    )
                            && safeEquals(
                            oldItem.getSenderProfileImage(),
                            newItem.getSenderProfileImage()
                    )
                            && safeEquals(
                            oldItem.getRoomId(),
                            newItem.getRoomId()
                    )
                            && safeEquals(
                            oldItem.getMessageType(),
                            newItem.getMessageType()
                    )
                            && safeEquals(
                            oldItem.getFileUrl(),
                            newItem.getFileUrl()
                    )
                            && safeEquals(
                            oldItem.getSentAt(),
                            newItem.getSentAt()
                    )
                            && safeEquals(
                            oldItem.getDateHeaderKey(),
                            newItem.getDateHeaderKey()
                    )
                            && safeEquals(
                            oldItem.getDateHeaderText(),
                            newItem.getDateHeaderText()
                    )
                            && oldItem.getUnreadCount()
                            == newItem.getUnreadCount();
                }

                @Nullable
                @Override
                public Object getChangePayload(
                        @NonNull Message oldItem,
                        @NonNull Message newItem
                ) {

                    if (isUnreadDivider(oldItem)
                            || isUnreadDivider(newItem)
                            || oldItem.isDateHeader()
                            || newItem.isDateHeader()
                            || oldItem.isSystemMessage()
                            || newItem.isSystemMessage()) {

                        return null;
                    }

                    List<String> payloads =
                            new ArrayList<>();

                    if (oldItem.getUnreadCount()
                            != newItem.getUnreadCount()) {

                        payloads.add(
                                PAYLOAD_UNREAD_COUNT
                        );
                    }

                    if (!safeEquals(
                            oldItem.getSenderNickname(),
                            newItem.getSenderNickname()
                    )
                            || !safeEquals(
                            oldItem.getSenderProfileImage(),
                            newItem.getSenderProfileImage()
                    )) {

                        payloads.add(
                                PAYLOAD_PROFILE
                        );
                    }

                    if (!safeEquals(
                            oldItem.getMessageContent(),
                            newItem.getMessageContent()
                    )
                            || !safeEquals(
                            oldItem.getMessageType(),
                            newItem.getMessageType()
                    )
                            || !safeEquals(
                            oldItem.getFileUrl(),
                            newItem.getFileUrl()
                    )
                            || !safeEquals(
                            oldItem.getId(),
                            newItem.getId()
                    )
                            || !safeEquals(
                            oldItem.getSentAt(),
                            newItem.getSentAt()
                    )) {

                        payloads.add(
                                PAYLOAD_CONTENT
                        );
                    }

                    if (payloads.isEmpty()) {

                        return null;
                    }

                    return payloads;
                }

                private boolean isSamePendingAndServerMessage(
                        Message oldItem,
                        Message newItem
                ) {

                    if (oldItem == null
                            || newItem == null) {

                        return false;
                    }

                    Long oldId =
                            oldItem.getId();

                    Long newId =
                            newItem.getId();

                    boolean oldIsPending =
                            oldId != null
                                    && oldId < 0;

                    boolean newIsPending =
                            newId != null
                                    && newId < 0;

                    if (oldIsPending == newIsPending) {

                        return false;
                    }

                    return safeEquals(
                            oldItem.getSenderId(),
                            newItem.getSenderId()
                    )
                            && safeEquals(
                            oldItem.getMessageContent(),
                            newItem.getMessageContent()
                    )
                            && safeEquals(
                            oldItem.getMessageType(),
                            newItem.getMessageType()
                    )
                            && safeEquals(
                            oldItem.getFileUrl(),
                            newItem.getFileUrl()
                    );
                }

                private boolean safeEquals(
                        Object a,
                        Object b
                ) {

                    if (a == null
                            && b == null) {

                        return true;
                    }

                    if (a == null
                            || b == null) {

                        return false;
                    }

                    return a.equals(
                            b
                    );
                }
            };

    @Override
    public void submitList(
            @Nullable List<Message> list
    ) {

        super.submitList(
                buildDisplayList(
                        list
                )
        );
    }

    @Override
    public void submitList(
            @Nullable List<Message> list,
            @Nullable Runnable commitCallback
    ) {

        super.submitList(
                buildDisplayList(
                        list
                ),
                commitCallback
        );
    }

    private List<Message> buildDisplayList(
            List<Message> sourceList
    ) {

        List<Message> displayList =
                new ArrayList<>();

        if (sourceList == null
                || sourceList.isEmpty()) {

            return displayList;
        }

        String previousDateKey =
                null;

        boolean unreadDividerAdded =
                false;

        for (Message message : sourceList) {

            if (message == null) {

                continue;
            }

            if (message.isDateHeader()
                    || isUnreadDivider(message)) {

                continue;
            }

            String currentDateKey =
                    TimeUtil.getMessageDateKey(
                            message.getSentAt()
                    );

            if (currentDateKey != null
                    && !currentDateKey.trim().isEmpty()
                    && !currentDateKey.equals(
                    previousDateKey
            )) {

                String headerText =
                        TimeUtil.formatMessageDateHeader(
                                message.getSentAt()
                        );

                if (headerText != null
                        && !headerText.trim().isEmpty()) {

                    displayList.add(
                            Message.createDateHeader(
                                    currentDateKey,
                                    headerText
                            )
                    );

                    previousDateKey =
                            currentDateKey;
                }
            }

            if (!unreadDividerAdded
                    && firstUnreadMessageId != null
                    && message.getId() != null
                    && firstUnreadMessageId.equals(
                    message.getId()
            )) {

                displayList.add(
                        createUnreadDividerMessage()
                );

                unreadDividerAdded =
                        true;
            }

            displayList.add(
                    message
            );
        }

        return displayList;
    }

    private static Message createUnreadDividerMessage() {

        Message message =
                new Message();

        message.setMessageType(
                MESSAGE_TYPE_UNREAD_DIVIDER
        );

        message.setMessageContent(
                "여기까지 읽음"
        );

        return message;
    }

    private static boolean isUnreadDivider(
            Message message
    ) {

        return message != null
                && MESSAGE_TYPE_UNREAD_DIVIDER.equals(
                message.getMessageType()
        );
    }

    @Override
    public int getItemViewType(
            int position
    ) {

        Message message =
                getItem(
                        position
                );

        if (message == null) {

            return VIEW_TYPE_RECEIVED;
        }

        if (isUnreadDivider(message)) {

            return VIEW_TYPE_UNREAD_DIVIDER;
        }

        if (message.isDateHeader()) {

            return VIEW_TYPE_DATE_HEADER;
        }

        if (message.isSystemMessage()) {

            return VIEW_TYPE_SYSTEM;
        }

        Long senderId =
                message.getSenderId();

        if (senderId != null
                && myUserId != null
                && senderId.equals(
                myUserId
        )) {

            return VIEW_TYPE_SENT;
        }

        return VIEW_TYPE_RECEIVED;
    }

    @NonNull
    @Override
    public BaseMessageViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view;

        if (viewType == VIEW_TYPE_SENT) {

            view =
                    LayoutInflater.from(
                                    parent.getContext()
                            )
                            .inflate(
                                    R.layout.item_message_sent,
                                    parent,
                                    false
                            );

            return new SentMessageViewHolder(
                    view
            );

        } else if (viewType == VIEW_TYPE_SYSTEM) {

            view =
                    LayoutInflater.from(
                                    parent.getContext()
                            )
                            .inflate(
                                    R.layout.item_message_system,
                                    parent,
                                    false
                            );

            return new SystemMessageViewHolder(
                    view
            );

        } else if (viewType == VIEW_TYPE_DATE_HEADER) {

            view =
                    LayoutInflater.from(
                                    parent.getContext()
                            )
                            .inflate(
                                    R.layout.item_message_date_header,
                                    parent,
                                    false
                            );

            return new DateHeaderViewHolder(
                    view
            );

        } else if (viewType == VIEW_TYPE_UNREAD_DIVIDER) {

            view =
                    LayoutInflater.from(
                                    parent.getContext()
                            )
                            .inflate(
                                    R.layout.item_message_unread_divider,
                                    parent,
                                    false
                            );

            return new UnreadDividerViewHolder(
                    view
            );

        } else {

            view =
                    LayoutInflater.from(
                                    parent.getContext()
                            )
                            .inflate(
                                    R.layout.item_message_received,
                                    parent,
                                    false
                            );

            return new ReceivedMessageViewHolder(
                    view
            );
        }
    }

    @Override
    public void onBindViewHolder(
            @NonNull BaseMessageViewHolder holder,
            int position
    ) {

        Message currentMessage =
                getItem(
                        position
                );

        Message previousMessage =
                position > 0
                        ? getItem(
                        position - 1
                )
                        : null;

        Message nextMessage =
                position < getItemCount() - 1
                        ? getItem(
                        position + 1
                )
                        : null;

        holder.bind(
                currentMessage,
                previousMessage,
                nextMessage
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull BaseMessageViewHolder holder,
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

        Message currentMessage =
                getItem(
                        position
                );

        Message previousMessage =
                position > 0
                        ? getItem(
                        position - 1
                )
                        : null;

        Message nextMessage =
                position < getItemCount() - 1
                        ? getItem(
                        position + 1
                )
                        : null;

        holder.bindPayload(
                currentMessage,
                previousMessage,
                nextMessage,
                payloads
        );
    }
}