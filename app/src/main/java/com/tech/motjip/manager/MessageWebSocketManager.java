package com.tech.motjip.manager;

import android.util.Log;

import com.tech.motjip.MessageActivity;
import com.tech.motjip.Model.Message;
import com.tech.motjip.manager.socket.SocketManager;

import org.json.JSONObject;

import ua.naiksoftware.stomp.StompClient;

public class MessageWebSocketManager {

    public interface MessageCallback {

        void onMessageReceived(
                Message message
        );
    }

    public interface ReadCallback {

        void onReadReceived(
                JSONObject jsonObject
        );
    }

    public interface RoomStateCallback {

        void onConnected();
    }

    private static final String TAG =
            "MessageWebSocketManager";

    private final MessageActivity activity;
    private final long roomId;

    private final SocketManager socketManager =
            SocketManager.getInstance();

    private MessageCallback messageCallback;
    private ReadCallback readCallback;
    private RoomStateCallback roomStateCallback;

    private final String messageSubscribeKey;
    private final String readSubscribeKey;
    private final String connectedCallbackKey;

    private boolean disconnected =
            false;

    public MessageWebSocketManager(
            MessageActivity activity,
            long roomId
    ) {

        this.activity =
                activity;

        this.roomId =
                roomId;

        messageSubscribeKey =
                "message_room_" + roomId;

        readSubscribeKey =
                "read_room_" + roomId;

        connectedCallbackKey =
                "connected_room_" + roomId;
    }

    public void setMessageCallback(
            MessageCallback messageCallback
    ) {

        this.messageCallback =
                messageCallback;
    }

    public void setReadCallback(
            ReadCallback readCallback
    ) {

        this.readCallback =
                readCallback;
    }

    public void setRoomStateCallback(
            RoomStateCallback roomStateCallback
    ) {

        this.roomStateCallback =
                roomStateCallback;
    }

    public StompClient getStompClient() {

        return socketManager.getStompClient();
    }

    public void connect() {

        disconnected =
                false;

        socketManager.addConnectedCallback(
                connectedCallbackKey,
                () -> {

                    subscribeMessageTopic();

                    subscribeReadTopic();

                    if (roomStateCallback != null) {

                        roomStateCallback.onConnected();
                    }
                }
        );

        socketManager.connect(
                activity
        );
    }

    private void subscribeMessageTopic() {

        Log.d(
                "CHAT_TEST",
                "SUBSCRIBE_MESSAGE_TOPIC roomId="
                        + roomId
        );

        socketManager.subscribe(
                messageSubscribeKey,
                "/sub/chat/room/" + roomId,
                payload -> {

                    try {

                        Log.d(
                                "CHAT_TEST",
                                "MESSAGE_RECEIVED payload="
                                        + payload
                        );

                        JSONObject jsonObject =
                                new JSONObject(
                                        payload
                                );

                        Long senderId =
                                null;

                        if (jsonObject.has(
                                "senderId"
                        )
                                && !jsonObject.isNull(
                                "senderId"
                        )) {

                            senderId =
                                    jsonObject.optLong(
                                            "senderId"
                                    );
                        }

                        String text =
                                jsonObject.optString(
                                        "messageContent",
                                        ""
                                );

                        String nickname =
                                jsonObject.optString(
                                        "senderNickname",
                                        "익명"
                                );

                        String sentAt =
                                jsonObject.optString(
                                        "sentAt",
                                        ""
                                );

                        String messageType =
                                jsonObject.optString(
                                        "messageType",
                                        "TEXT"
                                );

                        String fileUrl =
                                null;

                        if (jsonObject.has(
                                "fileUrl"
                        )
                                && !jsonObject.isNull(
                                "fileUrl"
                        )) {

                            fileUrl =
                                    jsonObject.optString(
                                            "fileUrl",
                                            null
                                    );
                        }

                        int unreadCount =
                                jsonObject.optInt(
                                        "unreadCount",
                                        0
                                );

                        Message receivedMsg =
                                new Message(
                                        text,
                                        senderId,
                                        nickname,
                                        unreadCount
                                );

                        if (jsonObject.has(
                                "id"
                        )
                                && !jsonObject.isNull(
                                "id"
                        )) {

                            receivedMsg.setId(
                                    jsonObject.optLong(
                                            "id"
                                    )
                            );
                        }

                        String senderProfileImage =
                                null;

                        if (jsonObject.has(
                                "senderProfileImage"
                        )
                                && !jsonObject.isNull(
                                "senderProfileImage"
                        )) {

                            senderProfileImage =
                                    jsonObject.optString(
                                            "senderProfileImage",
                                            null
                                    );
                        }

                        receivedMsg.setSenderProfileImage(
                                senderProfileImage
                        );

                        receivedMsg.setRoomId(
                                roomId
                        );

                        receivedMsg.setSentAt(
                                sentAt
                        );

                        receivedMsg.setMessageType(
                                messageType
                        );

                        receivedMsg.setFileUrl(
                                fileUrl
                        );

                        receivedMsg.setUnreadCount(
                                unreadCount
                        );

                        Log.d(
                                "CHAT_TEST",
                                "MESSAGE_PARSED id="
                                        + receivedMsg.getId()
                                        + ", senderId="
                                        + receivedMsg.getSenderId()
                                        + ", content="
                                        + receivedMsg.getMessageContent()
                                        + ", unreadCount="
                                        + receivedMsg.getUnreadCount()
                                        + ", profile="
                                        + receivedMsg.getSenderProfileImage()
                        );

                        if (messageCallback != null) {

                            messageCallback.onMessageReceived(
                                    receivedMsg
                            );
                        }

                    } catch (Exception e) {

                        Log.e(
                                TAG,
                                "WebSocket 메시지 처리 실패",
                                e
                        );

                        Log.e(
                                "CHAT_TEST",
                                "MESSAGE_PARSE_ERROR",
                                e
                        );
                    }
                }
        );
    }

    private void subscribeReadTopic() {

        Log.d(
                "CHAT_TEST",
                "SUBSCRIBE_READ_TOPIC roomId="
                        + roomId
        );

        socketManager.subscribe(
                readSubscribeKey,
                "/sub/chat/room/" + roomId + "/read",
                payload -> {

                    try {

                        Log.d(
                                "CHAT_TEST",
                                "READ_RECEIVED payload="
                                        + payload
                        );

                        JSONObject jsonObject =
                                new JSONObject(
                                        payload
                                );

                        if (readCallback != null) {

                            readCallback.onReadReceived(
                                    jsonObject
                            );
                        }

                    } catch (Exception e) {

                        Log.e(
                                TAG,
                                "읽음 이벤트 payload 파싱 실패",
                                e
                        );

                        Log.e(
                                "CHAT_TEST",
                                "READ_PARSE_ERROR",
                                e
                        );
                    }
                }
        );
    }

    public void scheduleReconnect() {

        socketManager.connect(
                activity
        );
    }

    public void disconnect() {

        if (disconnected) {

            Log.d(
                    "CHAT_TEST",
                    "WS_ROOM_DISCONNECT_SKIP_ALREADY roomId="
                            + roomId
            );

            return;
        }

        disconnected =
                true;

        socketManager.unsubscribe(
                messageSubscribeKey
        );

        socketManager.unsubscribe(
                readSubscribeKey
        );

        socketManager.removeConnectedCallback(
                connectedCallbackKey
        );

        Log.d(
                "CHAT_TEST",
                "WS_ROOM_UNSUBSCRIBE_CLEAR roomId="
                        + roomId
        );
    }

    public boolean isConnected() {

        return socketManager.isConnected();
    }
}