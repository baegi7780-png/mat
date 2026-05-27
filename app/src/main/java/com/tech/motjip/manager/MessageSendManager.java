package com.tech.motjip.manager;

import android.util.Log;
import android.widget.Toast;

import com.tech.motjip.MessageActivity;

import org.json.JSONObject;

import ua.naiksoftware.stomp.StompClient;

public class MessageSendManager {

    private static final String TAG = "MessageSendManager";

    private final MessageActivity activity;
    private final long roomId;

    public MessageSendManager(
            MessageActivity activity,
            long roomId
    ) {

        this.activity = activity;
        this.roomId = roomId;
    }

    public void sendMessage(
            StompClient stompClient,
            Long currentUserId,
            String currentNickname,
            String content,
            String messageType,
            String fileUrl,
            Runnable onDisconnected
    ) {

        try {

            if (currentUserId == null
                    || currentUserId <= 0) {

                Toast.makeText(
                        activity,
                        "사용자 정보를 불러오는 중입니다.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            if (stompClient == null
                    || !stompClient.isConnected()) {

                Toast.makeText(
                        activity,
                        "서버와 연결이 끊겨 메시지를 보낼 수 없습니다.",
                        Toast.LENGTH_SHORT
                ).show();

                if (onDisconnected != null) {
                    onDisconnected.run();
                }

                return;
            }

            JSONObject payload =
                    new JSONObject();

            payload.put(
                    "roomId",
                    roomId
            );

            payload.put(
                    "senderId",
                    currentUserId
            );

            payload.put(
                    "senderNickname",
                    currentNickname
            );

            payload.put(
                    "messageContent",
                    content
            );

            payload.put(
                    "messageType",
                    messageType
            );

            if (fileUrl != null) {

                payload.put(
                        "fileUrl",
                        fileUrl
                );
            }

            stompClient.send(
                    "/pub/chat/message",
                    payload.toString()
            ).subscribe();

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "메시지 전송 실패",
                    e
            );

            if (onDisconnected != null) {
                onDisconnected.run();
            }
        }
    }
}