package com.tech.motjip.manager;

import android.util.Log;

import com.tech.motjip.Model.Message;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MessageCacheManager {

    private static final String TAG =
            "MessageCacheManager";

    /*
     * 최대 방 캐시 개수
     */
    private static final int MAX_ROOM_CACHE_SIZE =
            30;

    /*
     * 방별 메시지 캐시
     */
    private static final Map<Long, List<Message>>
            roomMessageCache =
            new LinkedHashMap<Long, List<Message>>(
                    16,
                    0.75f,
                    true
            ) {

                @Override
                protected boolean removeEldestEntry(
                        Map.Entry<Long, List<Message>> eldest
                ) {

                    boolean shouldRemove =
                            size() > MAX_ROOM_CACHE_SIZE;

                    if (shouldRemove) {

                        Log.d(
                                TAG,
                                "오래된 방 캐시 제거 roomId="
                                        + eldest.getKey()
                        );
                    }

                    return shouldRemove;
                }
            };

    private final long roomId;

    public MessageCacheManager(
            long roomId
    ) {

        this.roomId =
                roomId;
    }

    public synchronized List<Message> getCachedMessages() {

        List<Message> cachedMessages =
                roomMessageCache.get(
                        roomId
                );

        if (cachedMessages == null) {

            return new ArrayList<>();
        }

        List<Message> copiedList =
                new ArrayList<>();

        for (Message message : cachedMessages) {

            if (message == null) {
                continue;
            }

            copiedList.add(
                    copyMessage(message)
            );
        }

        Log.d(
                TAG,
                "메모리 캐시 조회 roomId="
                        + roomId
                        + ", size="
                        + copiedList.size()
        );

        return copiedList;
    }

    public synchronized void saveMessages(
            List<Message> messageList
    ) {

        if (messageList == null) {
            return;
        }

        List<Message> copiedList =
                new ArrayList<>();

        for (Message message : messageList) {

            if (message == null) {
                continue;
            }

            copiedList.add(
                    copyMessage(message)
            );
        }

        roomMessageCache.put(
                roomId,
                copiedList
        );

        Log.d(
                TAG,
                "메모리 캐시 저장 roomId="
                        + roomId
                        + ", size="
                        + copiedList.size()
        );
    }

    public synchronized void clear() {

        roomMessageCache.remove(
                roomId
        );

        Log.d(
                TAG,
                "메모리 캐시 제거 roomId="
                        + roomId
        );
    }

    public static synchronized void clearAll() {

        roomMessageCache.clear();

        Log.d(
                TAG,
                "전체 메모리 캐시 제거"
        );
    }

    private Message copyMessage(
            Message original
    ) {

        Message copiedMessage =
                new Message();

        copiedMessage.setId(
                original.getId()
        );

        copiedMessage.setMessageContent(
                original.getMessageContent()
        );

        copiedMessage.setSenderId(
                original.getSenderId()
        );

        copiedMessage.setSenderNickname(
                original.getSenderNickname()
        );

        copiedMessage.setSenderProfileImage(
                original.getSenderProfileImage()
        );

        copiedMessage.setRoomId(
                original.getRoomId()
        );

        copiedMessage.setMessageType(
                original.getMessageType()
        );

        copiedMessage.setFileUrl(
                original.getFileUrl()
        );

        copiedMessage.setSentAt(
                original.getSentAt()
        );

        copiedMessage.setViewType(
                original.getViewType()
        );

        copiedMessage.setUnreadCount(
                original.getUnreadCount()
        );

        return copiedMessage;
    }
}