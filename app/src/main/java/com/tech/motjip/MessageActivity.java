package com.tech.motjip;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Controller.MessageAdapter;
import com.tech.motjip.Dto.ResponseDto.LoginResponseDto;
import com.tech.motjip.Handler.PreferenceManager;
import com.tech.motjip.Model.Message;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;

public class MessageActivity
        extends AppCompatActivity {

    private RecyclerView recyclerView;

    private MessageAdapter adapter;

    private List<Message> messageList;

    private EditText etMessage;

    private Button btnSend;

    private String currentUserId;

    private String currentNickname;

    private long roomId = -1;

    private StompClient stompClient;

    private final String TAG = "ChatTest";

    @Override
    protected void onCreate(
            Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_message
        );

        ImageView btnBack =
                findViewById(R.id.btn_custom_back);

        TextView tvTitle =
                findViewById(R.id.tv_room_title);

        recyclerView =
                findViewById(R.id.rv_message_list);

        etMessage =
                findViewById(R.id.et_message);

        btnSend =
                findViewById(R.id.btn_send);

        messageList =
                new ArrayList<>();

        adapter =
                new MessageAdapter(
                        messageList,
                        ""
                );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerView.setAdapter(adapter);

        String roomName =
                getIntent().getStringExtra(
                        "roomName"
                );

        if (roomName != null) {

            tvTitle.setText(roomName);
        }

        roomId =
                getIntent().getLongExtra(
                        "roomId",
                        -1
                );

        btnBack.setOnClickListener(v -> finish());

        recyclerView.addOnLayoutChangeListener(
                (v,
                 left,
                 top,
                 right,
                 bottom,
                 oldLeft,
                 oldTop,
                 oldRight,
                 oldBottom) -> {

                    if (bottom < oldBottom) {

                        recyclerView.postDelayed(() -> {

                            if (messageList.size() > 0) {

                                recyclerView.scrollToPosition(
                                        messageList.size() - 1
                                );
                            }

                        }, 100);
                    }
                }
        );

        etMessage.setOnKeyListener(
                (v, keyCode, event) -> {

                    if (event.getAction()
                            == KeyEvent.ACTION_DOWN
                            && keyCode
                            == KeyEvent.KEYCODE_ENTER) {

                        btnSend.performClick();

                        return true;
                    }

                    return false;
                }
        );

        btnSend.setOnClickListener(v -> {

            String content =
                    etMessage.getText()
                            .toString()
                            .trim();

            if (!content.isEmpty()
                    && roomId != -1) {

                sendMessage(content);
            }
        });

        if (roomId != -1) {

            fetchUserInfoAndStartChat();

        } else {

            Toast.makeText(
                    this,
                    "채팅방 정보를 찾을 수 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();
        }
    }

    private void fetchUserInfoAndStartChat() {

        RetrofitClient.getApiService(this)
                .getCurrentUser()
                .enqueue(
                        new Callback<LoginResponseDto>() {

                            @Override
                            public void onResponse(
                                    @NonNull Call<LoginResponseDto> call,
                                    @NonNull Response<LoginResponseDto> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    LoginResponseDto user =
                                            response.body();

                                    if (user.getMemberId() != null) {

                                        currentUserId =
                                                String.valueOf(
                                                        user.getMemberId()
                                                );
                                    }

                                    currentNickname =
                                            user.getNickname();

                                    PreferenceManager.saveNickname(
                                            MessageActivity.this,
                                            currentNickname
                                    );
                                }

                                if (currentUserId == null
                                        || currentUserId.trim().isEmpty()
                                        || currentUserId.contains("@")) {

                                    currentUserId = "0";
                                }

                                if (currentNickname == null
                                        || currentNickname.trim().isEmpty()) {

                                    currentNickname =
                                            PreferenceManager.getNickname(
                                                    MessageActivity.this
                                            );

                                    if (currentNickname == null) {

                                        currentNickname = "익명";
                                    }
                                }

                                adapter =
                                        new MessageAdapter(
                                                messageList,
                                                currentUserId
                                        );

                                recyclerView.setAdapter(adapter);

                                loadChatHistory();

                                connectWebSocket();
                            }

                            @Override
                            public void onFailure(
                                    @NonNull Call<LoginResponseDto> call,
                                    @NonNull Throwable t
                            ) {

                                Log.e(
                                        TAG,
                                        "유저 정보 로드 실패",
                                        t
                                );

                                currentUserId = "0";

                                currentNickname =
                                        PreferenceManager.getNickname(
                                                MessageActivity.this
                                        );

                                if (currentNickname == null) {

                                    currentNickname = "익명";
                                }

                                loadChatHistory();

                                connectWebSocket();
                            }
                        }
                );
    }

    private void loadChatHistory() {

        ApiService apiService =
                RetrofitClient.getApiService(this);

        apiService.getChatMessages(roomId)
                .enqueue(
                        new Callback<List<Message>>() {

                            @Override
                            public void onResponse(
                                    @NonNull Call<List<Message>> call,
                                    @NonNull Response<List<Message>> response
                            ) {

                                if (response.isSuccessful()
                                        && response.body() != null) {

                                    List<Message> historyList =
                                            response.body();

                                    for (Message msg : historyList) {

                                        if (msg.getSenderId() != null
                                                && msg.getSenderId()
                                                .equals(currentUserId)) {

                                            msg.setViewType(0);

                                        } else {

                                            msg.setViewType(1);
                                        }
                                    }

                                    messageList.clear();

                                    messageList.addAll(historyList);

                                    adapter.notifyDataSetChanged();

                                    if (messageList.size() > 0) {

                                        recyclerView.scrollToPosition(
                                                messageList.size() - 1
                                        );
                                    }
                                }
                            }

                            @Override
                            public void onFailure(
                                    @NonNull Call<List<Message>> call,
                                    @NonNull Throwable t
                            ) {

                                Log.e(
                                        TAG,
                                        "채팅 내역 로드 실패",
                                        t
                                );
                            }
                        }
                );
    }

    @SuppressLint("CheckResult")
    private void connectWebSocket() {

        String url =
                "wss://spout-distant-cost.ngrok-free.dev/ws/chat/websocket";

        Map<String, String> connectHeaders =
                new HashMap<>();

        SharedPreferences prefs =
                getSharedPreferences(
                        "AppPrefs",
                        Context.MODE_PRIVATE
                );

        String accessToken =
                prefs.getString(
                        "ACCESS_TOKEN",
                        null
                );

        if (accessToken != null
                && !accessToken.trim().isEmpty()) {

            connectHeaders.put(
                    "Authorization",
                    "Bearer " + accessToken
            );
        }

        stompClient =
                Stomp.over(
                        Stomp.ConnectionProvider.OKHTTP,
                        url,
                        connectHeaders
                );

        stompClient.lifecycle()
                .subscribe(lifecycleEvent -> {

                    switch (lifecycleEvent.getType()) {

                        case OPENED:

                            Log.d(
                                    TAG,
                                    "🎉 서버 연결 성공!"
                            );

                            break;

                        case ERROR:

                            Log.e(
                                    TAG,
                                    "🚨 에러 발생",
                                    lifecycleEvent.getException()
                            );

                            break;

                        case CLOSED:

                            Log.d(
                                    TAG,
                                    "🔌 연결 끊어짐."
                            );

                            break;
                    }
                });

        stompClient.topic(
                "/sub/chat/room/" + roomId
        ).subscribe(topicMessage -> {

            String receivedData =
                    topicMessage.getPayload();

            try {

                JSONObject jsonObject =
                        new JSONObject(receivedData);

                String sender =
                        jsonObject.getString(
                                "senderId"
                        );

                String text =
                        jsonObject.getString(
                                "messageContent"
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

                int viewType =
                        sender.equals(currentUserId)
                                ? 0
                                : 1;

                Message receivedMsg =
                        new Message(
                                text,
                                sender,
                                nickname,
                                viewType
                        );

                receivedMsg.setSentAt(sentAt);

                runOnUiThread(() -> {

                    messageList.add(receivedMsg);

                    adapter.notifyItemInserted(
                            messageList.size() - 1
                    );

                    recyclerView.scrollToPosition(
                            messageList.size() - 1
                    );
                });

            } catch (Exception e) {

                Log.e(
                        TAG,
                        "WebSocket 메시지 처리 실패",
                        e
                );
            }
        });

        stompClient.connect();
    }

    private void sendMessage(
            String content
    ) {

        try {

            if (stompClient != null
                    && stompClient.isConnected()) {

                JSONObject payload =
                        new JSONObject();

                payload.put(
                        "roomId",
                        roomId
                );

                try {

                    payload.put(
                            "senderId",
                            Long.parseLong(currentUserId)
                    );

                } catch (NumberFormatException e) {

                    payload.put(
                            "senderId",
                            0L
                    );
                }

                payload.put(
                        "senderNickname",
                        currentNickname
                );

                payload.put(
                        "messageContent",
                        content
                );

                stompClient.send(
                        "/pub/chat/message",
                        payload.toString()
                ).subscribe();

                etMessage.setText("");

            } else {

                Toast.makeText(
                        this,
                        "서버와 연결이 끊겨 메시지를 보낼 수 없습니다.",
                        Toast.LENGTH_SHORT
                ).show();
            }

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "메시지 전송 실패",
                    e
            );
        }
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();

        if (stompClient != null
                && stompClient.isConnected()) {

            stompClient.disconnect();
        }
    }
}