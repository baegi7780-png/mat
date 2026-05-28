package com.tech.motjip.manager.socket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.tech.motjip.Auth.TokenManager;

import java.util.HashMap;
import java.util.Map;

import io.reactivex.disposables.Disposable;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class SocketManager {

    public interface TopicCallback {

        void onMessage(
                String payload
        );
    }

    public interface ConnectedCallback {

        void onConnected();
    }

    private static final String TAG =
            "SocketManager";

    private static final String SOCKET_URL =
            "wss://spiny-impure-laptop.ngrok-free.dev/ws/chat/websocket";

    private static SocketManager instance;

    private StompClient stompClient;

    private Context appContext;

    private boolean isDestroyed =
            false;

    private boolean isConnecting =
            false;

    private boolean isReconnecting =
            false;

    private Disposable lifecycleDisposable;

    private final Handler reconnectHandler =
            new Handler(
                    Looper.getMainLooper()
            );

    private final Map<String, SubscriptionInfo> subscriptionMap =
            new HashMap<>();

    private final Map<String, ConnectedCallback> connectedCallbackMap =
            new HashMap<>();

    private final Runnable reconnectRunnable =
            new Runnable() {

                @Override
                public void run() {

                    if (isDestroyed) {
                        return;
                    }

                    if (appContext == null) {
                        return;
                    }

                    if (stompClient == null
                            || !stompClient.isConnected()) {

                        Log.d(
                                TAG,
                                "Socket 재연결 시도"
                        );

                        Log.d(
                                "CHAT_TEST",
                                "SOCKET_RECONNECT_TRY"
                        );

                        connect(
                                appContext
                        );
                    }

                    isReconnecting =
                            false;
                }
            };

    private SocketManager() {

    }

    public static synchronized SocketManager getInstance() {

        if (instance == null) {

            instance =
                    new SocketManager();
        }

        return instance;
    }

    @SuppressLint("CheckResult")
    public synchronized void connect(
            Context context
    ) {

        if (context == null) {
            return;
        }

        appContext =
                context.getApplicationContext();

        isDestroyed =
                false;

        if (stompClient != null
                && stompClient.isConnected()) {

            Log.d(
                    "CHAT_TEST",
                    "SOCKET_CONNECT_SKIP_ALREADY_CONNECTED"
            );

            notifyConnectedCallbacks();

            return;
        }

        if (isConnecting) {

            Log.d(
                    "CHAT_TEST",
                    "SOCKET_CONNECT_SKIP_CONNECTING"
            );

            return;
        }

        isConnecting =
                true;

        clearClientOnly();

        isConnecting =
                true;

        Map<String, String> connectHeaders =
                new HashMap<>();

        TokenManager tokenManager =
                new TokenManager(
                        appContext
                );

        String accessToken =
                tokenManager.getAccessToken();

        if (accessToken != null
                && !accessToken.trim().isEmpty()) {

            connectHeaders.put(
                    "Authorization",
                    "Bearer " + accessToken
            );

            Log.d(
                    "CHAT_TEST",
                    "SOCKET_AUTH_HEADER_ADDED"
            );

        } else {

            Log.e(
                    "CHAT_TEST",
                    "SOCKET_AUTH_TOKEN_EMPTY"
            );
        }

        stompClient =
                Stomp.over(
                        Stomp.ConnectionProvider.OKHTTP,
                        SOCKET_URL,
                        connectHeaders
                );

        lifecycleDisposable =
                stompClient.lifecycle()
                        .subscribe(lifecycleEvent -> {

                            switch (lifecycleEvent.getType()) {

                                case OPENED:

                                    Log.d(
                                            TAG,
                                            "Socket 연결 성공"
                                    );

                                    Log.d(
                                            "CHAT_TEST",
                                            "SOCKET_OPENED"
                                    );

                                    isConnecting =
                                            false;

                                    isReconnecting =
                                            false;

                                    reconnectHandler.removeCallbacks(
                                            reconnectRunnable
                                    );

                                    resubscribeAll();

                                    notifyConnectedCallbacks();

                                    break;

                                case ERROR:

                                    Log.e(
                                            TAG,
                                            "Socket 오류",
                                            lifecycleEvent.getException()
                                    );

                                    Log.e(
                                            "CHAT_TEST",
                                            "SOCKET_ERROR",
                                            lifecycleEvent.getException()
                                    );

                                    isConnecting =
                                            false;

                                    markAllDisconnected();

                                    break;

                                case CLOSED:

                                    Log.d(
                                            TAG,
                                            "Socket 연결 종료"
                                    );

                                    Log.d(
                                            "CHAT_TEST",
                                            "SOCKET_CLOSED"
                                    );

                                    isConnecting =
                                            false;

                                    markAllDisconnected();

                                    scheduleReconnect();

                                    break;

                                case FAILED_SERVER_HEARTBEAT:

                                    Log.e(
                                            TAG,
                                            "Socket 서버 heartbeat 실패"
                                    );

                                    Log.e(
                                            "CHAT_TEST",
                                            "SOCKET_FAILED_SERVER_HEARTBEAT"
                                    );

                                    isConnecting =
                                            false;

                                    markAllDisconnected();

                                    scheduleReconnect();

                                    break;
                            }
                        });

        stompClient.connect();
    }

    public synchronized StompClient getStompClient() {

        return stompClient;
    }

    public synchronized boolean isConnected() {

        return stompClient != null
                && stompClient.isConnected();
    }

    public synchronized void addConnectedCallback(
            String key,
            ConnectedCallback callback
    ) {

        if (key == null
                || key.trim().isEmpty()) {

            return;
        }

        if (callback == null) {

            connectedCallbackMap.remove(
                    key
            );

            return;
        }

        connectedCallbackMap.put(
                key,
                callback
        );

        if (isConnected()) {

            callback.onConnected();
        }
    }

    public synchronized void removeConnectedCallback(
            String key
    ) {

        if (key == null) {
            return;
        }

        connectedCallbackMap.remove(
                key
        );
    }

    @SuppressLint("CheckResult")
    public synchronized void subscribe(
            String key,
            String topic,
            TopicCallback callback
    ) {

        if (key == null
                || key.trim().isEmpty()
                || topic == null
                || topic.trim().isEmpty()
                || callback == null) {

            return;
        }

        SubscriptionInfo oldInfo =
                subscriptionMap.get(
                        key
                );

        if (oldInfo != null) {

            oldInfo.dispose();
        }

        SubscriptionInfo newInfo =
                new SubscriptionInfo(
                        key,
                        topic,
                        callback
                );

        subscriptionMap.put(
                key,
                newInfo
        );

        if (isConnected()) {

            subscribeInternal(
                    newInfo
            );
        }
    }

    public synchronized void unsubscribe(
            String key
    ) {

        if (key == null) {
            return;
        }

        SubscriptionInfo info =
                subscriptionMap.remove(
                        key
                );

        if (info != null) {

            info.dispose();
        }
    }

    public synchronized void disconnectAll() {

        isDestroyed =
                true;

        reconnectHandler.removeCallbacks(
                reconnectRunnable
        );

        isConnecting =
                false;

        isReconnecting =
                false;

        for (SubscriptionInfo info : subscriptionMap.values()) {

            if (info != null) {

                info.dispose();
            }
        }

        subscriptionMap.clear();

        connectedCallbackMap.clear();

        clearClientOnly();

        Log.d(
                "CHAT_TEST",
                "SOCKET_DISCONNECT_ALL"
        );
    }

    private synchronized void clearClientOnly() {

        if (lifecycleDisposable != null
                && !lifecycleDisposable.isDisposed()) {

            lifecycleDisposable.dispose();
        }

        lifecycleDisposable =
                null;

        if (stompClient != null) {

            try {

                if (stompClient.isConnected()) {

                    stompClient.disconnect();
                }

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "Socket disconnect 실패",
                        e
                );
            }
        }

        stompClient =
                null;

        isConnecting =
                false;
    }

    private synchronized void scheduleReconnect() {

        if (isDestroyed) {
            return;
        }

        if (isReconnecting) {
            return;
        }

        if (subscriptionMap.isEmpty()
                && connectedCallbackMap.isEmpty()) {

            return;
        }

        isReconnecting =
                true;

        reconnectHandler.postDelayed(
                reconnectRunnable,
                3000
        );
    }

    private synchronized void resubscribeAll() {

        for (SubscriptionInfo info : subscriptionMap.values()) {

            if (info != null) {

                subscribeInternal(
                        info
                );
            }
        }
    }

    @SuppressLint("CheckResult")
    private synchronized void subscribeInternal(
            SubscriptionInfo info
    ) {

        if (info == null
                || stompClient == null
                || !stompClient.isConnected()) {

            return;
        }

        info.dispose();

        info.disposable =
                stompClient.topic(
                        info.topic
                ).subscribe(topicMessage -> {

                    if (info.callback != null) {

                        info.callback.onMessage(
                                topicMessage.getPayload()
                        );
                    }
                }, throwable -> {

                    Log.e(
                            TAG,
                            "topic subscribe error key="
                                    + info.key
                                    + ", topic="
                                    + info.topic,
                            throwable
                    );

                    Log.e(
                            "CHAT_TEST",
                            "SOCKET_TOPIC_ERROR key="
                                    + info.key
                                    + ", topic="
                                    + info.topic,
                            throwable
                    );

                    info.dispose();
                });

        Log.d(
                "CHAT_TEST",
                "SOCKET_SUBSCRIBED key="
                        + info.key
                        + ", topic="
                        + info.topic
        );
    }

    private synchronized void markAllDisconnected() {

        for (SubscriptionInfo info : subscriptionMap.values()) {

            if (info != null) {

                info.dispose();
            }
        }
    }

    private synchronized void notifyConnectedCallbacks() {

        for (ConnectedCallback callback : connectedCallbackMap.values()) {

            if (callback != null) {

                callback.onConnected();
            }
        }
    }

    private static class SubscriptionInfo {

        private final String key;
        private final String topic;
        private final TopicCallback callback;

        private Disposable disposable;

        private SubscriptionInfo(
                String key,
                String topic,
                TopicCallback callback
        ) {

            this.key =
                    key;

            this.topic =
                    topic;

            this.callback =
                    callback;
        }

        private void dispose() {

            if (disposable != null
                    && !disposable.isDisposed()) {

                disposable.dispose();
            }

            disposable =
                    null;
        }
    }
}