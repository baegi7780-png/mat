package com.tech.motjip.Fragment;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Adapter.ChatRoomAdapter;
import com.tech.motjip.GroupCreateActivity;
import com.tech.motjip.MessageActivity;
import com.tech.motjip.Model.ChatRoom;
import com.tech.motjip.R;
import com.tech.motjip.manager.socket.SocketManager;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerView;

    private EditText etChatSearch;

    private ChatRoomAdapter adapter;

    // =========================
    // RecyclerView에 실제로 표시되는 목록
    // =========================
    private final List<ChatRoom> roomList =
            new ArrayList<>();

    // =========================
    // 서버에서 받아온 원본 전체 목록
    // 검색 필터를 해제하면 이 목록을 다시 표시
    // =========================
    private final List<ChatRoom> allRoomList =
            new ArrayList<>();

    private String currentSearchKeyword =
            "";

    private final SocketManager socketManager =
            SocketManager.getInstance();

    private static final String ROOM_LIST_SUBSCRIBE_KEY =
            "room_list_update";

    private static final String ROOM_LIST_CONNECTED_KEY =
            "room_list_connected";

    private boolean isLoadingRooms =
            false;

    private boolean roomUpdateSubscribed =
            false;

    private boolean skipNextResumeRoomReload =
            false;

    private boolean pendingRoomReload =
            false;

    private boolean isOpeningChatRoom =
            false;

    private final String TAG =
            "ChatFragment";

    public ChatFragment() {

    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState
    ) {

        Log.d(
                TAG,
                "onCreateView 시작"
        );

        View view =
                inflater.inflate(
                        R.layout.fragment_chat,
                        container,
                        false
                );

        recyclerView =
                view.findViewById(
                        R.id.rv_chat_list
                );

        etChatSearch =
                view.findViewById(
                        R.id.et_chat_search
                );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        requireContext()
                )
        );

        adapter =
                new ChatRoomAdapter(
                        roomList,
                        this::openChatRoom
                );

        recyclerView.setAdapter(
                adapter
        );

        disableRecyclerViewChangeAnimation();

        setupChatRoomSearch();

        ImageView btnCreateRoom =
                view.findViewById(
                        R.id.btn_create_room
                );

        if (btnCreateRoom != null) {

            btnCreateRoom.setOnClickListener(v -> {

                if (!isAdded()
                        || getContext() == null) {

                    return;
                }

                Log.d(
                        TAG,
                        "그룹 채팅 생성 버튼 클릭"
                );

                Intent intent =
                        new Intent(
                                requireContext(),
                                GroupCreateActivity.class
                        );

                startActivity(
                        intent
                );
            });

        } else {

            Log.e(
                    TAG,
                    "btn_create_room 을 찾을 수 없습니다."
            );
        }

        loadChatRooms();

        connectRoomListSocket();

        return view;
    }

    private void setupChatRoomSearch() {

        if (etChatSearch == null) {

            Log.e(
                    TAG,
                    "et_chat_search 를 찾을 수 없습니다."
            );

            return;
        }

        etChatSearch.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {

            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {

                currentSearchKeyword =
                        s == null
                                ? ""
                                : s.toString().trim();

                Log.d(
                        TAG,
                        "채팅방 검색어 변경 keyword="
                                + currentSearchKeyword
                );

                applyRoomSearchFilter();
            }

            @Override
            public void afterTextChanged(
                    Editable s
            ) {

            }
        });
    }

    private void applyRoomSearchFilter() {

        roomList.clear();

        if (currentSearchKeyword == null
                || currentSearchKeyword.trim().isEmpty()) {

            roomList.addAll(
                    allRoomList
            );

        } else {

            String keyword =
                    currentSearchKeyword
                            .trim()
                            .toLowerCase();

            for (ChatRoom room : allRoomList) {

                if (room == null) {

                    continue;
                }

                String roomName =
                        ChatRoomAdapter.getDisplayRoomName(
                                room
                        );

                if (roomName != null
                        && roomName
                        .toLowerCase()
                        .contains(
                                keyword
                        )) {

                    roomList.add(
                            room
                    );
                }
            }
        }

        Log.d(
                TAG,
                "검색 필터 적용 전체="
                        + allRoomList.size()
                        + ", 표시="
                        + roomList.size()
                        + ", keyword="
                        + currentSearchKeyword
        );

        if (adapter != null) {

            adapter.notifyDataSetChanged();
        }
    }

    private void disableRecyclerViewChangeAnimation() {

        if (recyclerView == null) {

            return;
        }

        RecyclerView.ItemAnimator animator =
                recyclerView.getItemAnimator();

        if (animator instanceof SimpleItemAnimator) {

            ((SimpleItemAnimator) animator)
                    .setSupportsChangeAnimations(
                            false
                    );
        }
    }

    private Long getLoginMemberId() {

        if (!isAdded()
                || getContext() == null) {

            return 0L;
        }

        SharedPreferences authPrefs =
                requireContext().getSharedPreferences(
                        "auth",
                        Context.MODE_PRIVATE
                );

        return authPrefs.getLong(
                "memberId",
                0L
        );
    }

    private void loadChatRooms() {

        Log.d(
                TAG,
                "loadChatRooms 시작"
        );

        if (!isAdded()
                || getContext() == null) {

            Log.e(
                    TAG,
                    "loadChatRooms 중단: Fragment attach 안됨"
            );

            return;
        }

        if (isLoadingRooms) {

            Log.d(
                    TAG,
                    "loadChatRooms 중복 호출 감지 → pendingRoomReload 예약"
            );

            pendingRoomReload =
                    true;

            return;
        }

        Long memberId =
                getLoginMemberId();

        if (memberId == null
                || memberId <= 0) {

            Log.e(
                    TAG,
                    "memberId 없음"
            );

            return;
        }

        isLoadingRooms =
                true;

        ApiService apiService =
                RetrofitClient.getApiService(
                        requireContext()
                );

        apiService.getMyRooms(
                memberId
        ).enqueue(new Callback<List<ChatRoom>>() {

            @Override
            public void onResponse(
                    @NonNull Call<List<ChatRoom>> call,
                    @NonNull Response<List<ChatRoom>> response
            ) {

                isLoadingRooms =
                        false;

                if (!isAdded()
                        || getView() == null) {

                    Log.e(
                            TAG,
                            "응답 수신 후 Fragment detach 상태"
                    );

                    return;
                }

                Log.d(
                        TAG,
                        "getMyRooms 응답 코드: "
                                + response.code()
                );

                if (response.isSuccessful()
                        && response.body() != null) {

                    allRoomList.clear();

                    allRoomList.addAll(
                            response.body()
                    );

                    applyRoomSearchFilter();

                    Log.d(
                            TAG,
                            "채팅방 전체 개수: "
                                    + allRoomList.size()
                                    + ", 검색 후 표시 개수: "
                                    + roomList.size()
                    );

                } else {

                    Log.e(
                            TAG,
                            "채팅방 응답 실패: "
                                    + response.code()
                    );
                }

                if (pendingRoomReload) {

                    pendingRoomReload =
                            false;

                    if (recyclerView != null) {

                        recyclerView.postDelayed(
                                ChatFragment.this::loadChatRooms,
                                150
                        );
                    }
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<List<ChatRoom>> call,
                    @NonNull Throwable t
            ) {

                isLoadingRooms =
                        false;

                if (!isAdded()) {

                    return;
                }

                Log.e(
                        TAG,
                        "채팅방 로드 실패",
                        t
                );

                if (pendingRoomReload) {

                    pendingRoomReload =
                            false;

                    if (recyclerView != null) {

                        recyclerView.postDelayed(
                                ChatFragment.this::loadChatRooms,
                                300
                        );
                    }
                }
            }
        });
    }

    private void connectRoomListSocket() {

        Log.d(
                TAG,
                "connectRoomListSocket 시작"
        );

        if (!isAdded()
                || getContext() == null) {

            Log.e(
                    TAG,
                    "connectRoomListSocket 중단: Fragment attach 안됨"
            );

            return;
        }

        socketManager.addConnectedCallback(
                ROOM_LIST_CONNECTED_KEY,
                () -> {

                    if (!roomUpdateSubscribed) {

                        subscribeRoomUpdate();

                        roomUpdateSubscribed =
                                true;

                        Log.d(
                                "CHAT_TEST",
                                "ROOM_UPDATE_SUBSCRIBED"
                        );

                    } else {

                        Log.d(
                                "CHAT_TEST",
                                "ROOM_UPDATE_SUBSCRIBE_SKIP"
                        );
                    }
                }
        );

        socketManager.connect(
                requireContext()
        );
    }

    private void subscribeRoomUpdate() {

        Log.d(
                TAG,
                "subscribeRoomUpdate 시작"
        );

        socketManager.subscribe(
                ROOM_LIST_SUBSCRIBE_KEY,
                "/sub/chat/rooms/update",
                payload -> {

                    Log.d(
                            TAG,
                            "채팅방 목록 업데이트 수신 body="
                                    + payload
                    );

                    Log.d(
                            "CHAT_TEST",
                            "ROOM_UPDATE_RECEIVED payload="
                                    + payload
                    );

                    if (!isAdded()
                            || getActivity() == null) {

                        Log.e(
                                TAG,
                                "업데이트 수신했지만 Fragment attach 안됨"
                        );

                        return;
                    }

                    requireActivity().runOnUiThread(() -> {

                        if (!isAdded()
                                || getView() == null) {

                            return;
                        }

                        handleRoomUpdatePayload(
                                payload
                        );
                    });
                }
        );
    }

    private void handleRoomUpdatePayload(
            String payload
    ) {

        try {

            if (payload == null
                    || payload.trim().isEmpty()) {

                scheduleRoomReload(
                        150
                );

                return;
            }

            JSONObject jsonObject =
                    new JSONObject(
                            payload
                    );

            if (!jsonObject.has(
                    "roomId"
            )
                    || jsonObject.isNull(
                    "roomId"
            )) {

                scheduleRoomReload(
                        150
                );

                return;
            }

            long updatedRoomId =
                    jsonObject.optLong(
                            "roomId",
                            -1L
                    );

            if (updatedRoomId <= 0) {

                scheduleRoomReload(
                        150
                );

                return;
            }

            int position =
                    findRoomPositionById(
                            updatedRoomId
                    );

            if (position < 0) {

                Log.d(
                        TAG,
                        "새 채팅방 감지 → 지연 전체 리스트 새로고침 roomId="
                                + updatedRoomId
                );

                scheduleRoomReload(
                        200
                );

                return;
            }

            ChatRoom room =
                    allRoomList.get(
                            position
                    );

            if (room == null) {

                scheduleRoomReload(
                        150
                );

                return;
            }

            boolean changed =
                    false;

            List<String> payloads =
                    new ArrayList<>();

            if (jsonObject.has(
                    "lastMessage"
            )
                    && !jsonObject.isNull(
                    "lastMessage"
            )) {

                String newLastMessage =
                        jsonObject.optString(
                                "lastMessage",
                                room.getLastMessage()
                        );

                if (!safeEquals(
                        room.getLastMessage(),
                        newLastMessage
                )) {

                    room.setLastMessage(
                            newLastMessage
                    );

                    payloads.add(
                            ChatRoomAdapter.PAYLOAD_LAST_MESSAGE
                    );

                    changed =
                            true;
                }
            }

            if (jsonObject.has(
                    "lastMessageType"
            )
                    && !jsonObject.isNull(
                    "lastMessageType"
            )) {

                String newLastMessageType =
                        jsonObject.optString(
                                "lastMessageType",
                                room.getLastMessageType()
                        );

                if (!safeEquals(
                        room.getLastMessageType(),
                        newLastMessageType
                )) {

                    room.setLastMessageType(
                            newLastMessageType
                    );

                    if (!payloads.contains(
                            ChatRoomAdapter.PAYLOAD_LAST_MESSAGE
                    )) {

                        payloads.add(
                                ChatRoomAdapter.PAYLOAD_LAST_MESSAGE
                        );
                    }

                    changed =
                            true;
                }
            }

            if (jsonObject.has(
                    "time"
            )
                    && !jsonObject.isNull(
                    "time"
            )) {

                String newTime =
                        jsonObject.optString(
                                "time",
                                room.getTime()
                        );

                if (!safeEquals(
                        room.getTime(),
                        newTime
                )) {

                    room.setTime(
                            newTime
                    );

                    payloads.add(
                            ChatRoomAdapter.PAYLOAD_TIME
                    );

                    changed =
                            true;
                }
            }

            if (jsonObject.has(
                    "unreadCount"
            )
                    && !jsonObject.isNull(
                    "unreadCount"
            )) {

                long myMemberId =
                        getLoginMemberId();

                long targetMemberId =
                        jsonObject.optLong(
                                "targetMemberId",
                                -1L
                        );

                int unreadCount =
                        jsonObject.optInt(
                                "unreadCount",
                                (int) room.getUnreadCount()
                        );

                if (targetMemberId > 0) {

                    if (targetMemberId == myMemberId) {

                        if (room.getUnreadCount()
                                != unreadCount) {

                            room.setUnreadCount(
                                    unreadCount
                            );

                            payloads.add(
                                    ChatRoomAdapter.PAYLOAD_UNREAD_COUNT
                            );

                            changed =
                                    true;
                        }

                        Log.d(
                                "CHAT_TEST",
                                "ROOM_UNREAD_UPDATE roomId="
                                        + room.getRoomId()
                                        + ", targetMemberId="
                                        + targetMemberId
                                        + ", myMemberId="
                                        + myMemberId
                                        + ", unreadCount="
                                        + room.getUnreadCount()
                        );

                    } else {

                        Log.d(
                                "CHAT_TEST",
                                "ROOM_UNREAD_SKIP_NOT_TARGET roomId="
                                        + room.getRoomId()
                                        + ", targetMemberId="
                                        + targetMemberId
                                        + ", myMemberId="
                                        + myMemberId
                        );
                    }

                } else {

                    if (room.getUnreadCount()
                            != unreadCount) {

                        room.setUnreadCount(
                                unreadCount
                        );

                        payloads.add(
                                ChatRoomAdapter.PAYLOAD_UNREAD_COUNT
                        );

                        changed =
                                true;
                    }

                    Log.d(
                            "CHAT_TEST",
                            "ROOM_UNREAD_UPDATE_NO_TARGET roomId="
                                    + room.getRoomId()
                                    + ", unreadCount="
                                    + room.getUnreadCount()
                    );
                }
            }

            if (changed
                    && adapter != null) {

                ChatRoom updatedRoom =
                        allRoomList.remove(
                                position
                        );

                allRoomList.add(
                        0,
                        updatedRoom
                );

                applyRoomSearchFilter();

                if (recyclerView != null
                        && (currentSearchKeyword == null
                        || currentSearchKeyword.trim().isEmpty())) {

                    recyclerView.post(() -> {

                        if (recyclerView != null) {

                            recyclerView.scrollToPosition(
                                    0
                            );
                        }
                    });
                }
            }

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "채팅방 업데이트 payload 처리 실패",
                    e
            );

            Log.e(
                    "CHAT_TEST",
                    "ROOM_UPDATE_PARSE_ERROR",
                    e
            );

            scheduleRoomReload(
                    300
            );
        }
    }

    private void scheduleRoomReload(
            long delayMillis
    ) {

        if (!isAdded()
                || recyclerView == null) {

            return;
        }

        if (isLoadingRooms) {

            pendingRoomReload =
                    true;

            return;
        }

        recyclerView.postDelayed(
                this::loadChatRooms,
                delayMillis
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

    private int findRoomPositionById(
            long roomId
    ) {

        for (int i = 0;
             i < allRoomList.size();
             i++) {

            ChatRoom room =
                    allRoomList.get(
                            i
                    );

            if (room == null
                    || room.getRoomId() == null) {

                continue;
            }

            if (room.getRoomId() == roomId) {

                return i;
            }
        }

        return -1;
    }

    private void openChatRoom(
            ChatRoom room
    ) {

        Log.d(
                "CHAT_CRASH_TRACE",
                "OPEN_CHAT_ROOM_START isAdded="
                        + isAdded()
                        + ", contextNull="
                        + (getContext() == null)
                        + ", activityNull="
                        + (getActivity() == null)
                        + ", recyclerViewNull="
                        + (recyclerView == null)
                        + ", adapterNull="
                        + (adapter == null)
                        + ", isLoadingRooms="
                        + isLoadingRooms
                        + ", isOpeningChatRoom="
                        + isOpeningChatRoom
        );

        if (isLoadingRooms) {

            Log.d(
                    TAG,
                    "채팅방 목록 로딩 중 클릭 무시"
            );

            return;
        }

        if (isOpeningChatRoom) {

            Log.d(
                    TAG,
                    "채팅방 중복 열기 차단"
            );

            return;
        }

        if (!isAdded()
                || getContext() == null
                || getActivity() == null
                || recyclerView == null
                || adapter == null) {

            Log.e(
                    TAG,
                    "Fragment 상태 불안정 - 채팅방 이동 차단"
            );

            return;
        }

        if (room == null
                || room.getRoomId() == null
                || room.getRoomId() <= 0) {

            Log.e(
                    TAG,
                    "잘못된 roomId"
            );

            Toast.makeText(
                    requireContext(),
                    "채팅방 정보를 불러오는 중입니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String displayName =
                ChatRoomAdapter.getDisplayRoomName(
                        room
                );

        Log.d(
                TAG,
                "채팅방 클릭 roomId="
                        + room.getRoomId()
                        + ", roomName="
                        + displayName
        );

        Intent intent =
                new Intent(
                        requireContext(),
                        MessageActivity.class
                );

        intent.putExtra(
                "roomId",
                room.getRoomId()
        );

        intent.putExtra(
                "roomName",
                displayName
        );

        intent.putExtra(
                "roomType",
                room.getRoomType()
        );

        isOpeningChatRoom =
                true;

        Log.d(
                "CHAT_CRASH_TRACE",
                "OPEN_CHAT_ROOM_LAUNCH_START_ACTIVITY roomId="
                        + room.getRoomId()
                        + ", roomName="
                        + displayName
                        + ", roomType="
                        + room.getRoomType()
        );

        startActivity(
                intent
        );
    }

    @Override
    public void onResume() {

        super.onResume();

        isOpeningChatRoom =
                false;

        Log.d(
                TAG,
                "onResume"
        );

        if (skipNextResumeRoomReload) {

            skipNextResumeRoomReload =
                    false;

        } else {

            loadChatRooms();
        }

        if (!socketManager.isConnected()) {

            connectRoomListSocket();

        } else if (!roomUpdateSubscribed) {

            subscribeRoomUpdate();

            roomUpdateSubscribed =
                    true;
        }
    }

    @Override
    public void onPause() {

        super.onPause();

        Log.d(
                TAG,
                "onPause"
        );
    }

    @Override
    public void onDestroyView() {

        super.onDestroyView();

        Log.d(
                TAG,
                "onDestroyView"
        );

        socketManager.unsubscribe(
                ROOM_LIST_SUBSCRIBE_KEY
        );

        socketManager.removeConnectedCallback(
                ROOM_LIST_CONNECTED_KEY
        );

        roomUpdateSubscribed =
                false;

        pendingRoomReload =
                false;

        isOpeningChatRoom =
                false;

        recyclerView =
                null;

        etChatSearch =
                null;

        adapter =
                null;
    }
}