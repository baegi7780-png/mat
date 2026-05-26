package com.tech.motjip.manager;

import android.util.Log;

import androidx.annotation.NonNull;

import org.json.JSONArray;
import org.json.JSONObject;

public class MessageReadManager {

    private static final String TAG =
            "MessageReadManager";

    private final MessageSyncManager syncManager;

    public MessageReadManager(
            MessageSyncManager syncManager
    ) {

        this.syncManager =
                syncManager;
    }

    public void handleReadPayload(
            @NonNull JSONObject payload
    ) {

        try {

            Log.d(
                    "CHAT_TEST",
                    "READ_PAYLOAD="
                            + payload
            );

            String type =
                    payload.optString(
                            "type",
                            ""
                    );

            if (!"READ".equals(type)) {

                Log.d(
                        "CHAT_TEST",
                        "READ_PAYLOAD_SKIP type="
                                + type
                );

                return;
            }

            JSONArray readIdsArray =
                    payload.optJSONArray(
                            "readMessageIds"
                    );

            JSONObject unreadMapJson =
                    payload.optJSONObject(
                            "unreadCountMap"
                    );

            if (readIdsArray == null
                    || unreadMapJson == null) {

                Log.d(
                        "CHAT_TEST",
                        "READ_PAYLOAD_INVALID readIdsArray="
                                + readIdsArray
                                + ", unreadMapJson="
                                + unreadMapJson
                );

                return;
            }

            for (int i = 0;
                 i < readIdsArray.length();
                 i++) {

                long messageId =
                        readIdsArray.optLong(
                                i,
                                -1L
                        );

                if (messageId <= 0) {
                    continue;
                }

                int unreadCount =
                        unreadMapJson.optInt(
                                String.valueOf(
                                        messageId
                                ),
                                0
                        );

                Log.d(
                        "CHAT_TEST",
                        "READ_APPLY_SYNC_MANAGER messageId="
                                + messageId
                                + ", unreadCount="
                                + unreadCount
                );

                syncManager.updateReadCount(
                        messageId,
                        unreadCount
                );
            }

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "READ 이벤트 처리 실패",
                    e
            );

            Log.e(
                    "CHAT_TEST",
                    "READ_HANDLE_ERROR",
                    e
            );
        }
    }
}