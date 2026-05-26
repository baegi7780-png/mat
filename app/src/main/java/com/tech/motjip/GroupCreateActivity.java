package com.tech.motjip;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Adapter.GroupFriendAdapter;
import com.tech.motjip.Dto.RequestDto.CreateRoomRequestDto;
import com.tech.motjip.Dto.ResponseDto.FriendResponseDto;
import com.tech.motjip.Dto.ResponseDto.LoginResponseDto;
import com.tech.motjip.Model.ChatRoom;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupCreateActivity extends AppCompatActivity {

    private static final String TAG =
            "GROUP_CREATE";

    private RecyclerView recyclerView;

    private GroupFriendAdapter adapter;

    private final List<FriendResponseDto> friendList =
            new ArrayList<>();

    private EditText etRoomName;

    private Button btnCreate;

    private Long myId;

    private Long fromRoomId = -1L;

    private boolean isCreatingRoom = false;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        Log.d(
                TAG,
                "onCreate 시작"
        );

        setContentView(
                R.layout.activity_group_create
        );

        fromRoomId =
                getIntent().getLongExtra(
                        "fromRoomId",
                        -1
                );

        Log.d(
                TAG,
                "fromRoomId = " + fromRoomId
        );

        recyclerView =
                findViewById(
                        R.id.rv_friend_list
                );

        etRoomName =
                findViewById(
                        R.id.et_room_name
                );

        setupRoomNameInput();

        btnCreate =
                findViewById(
                        R.id.btn_create_group
                );

        btnCreate.setEnabled(
                false
        );

        recyclerView.setLayoutManager(
                new LinearLayoutManager(
                        this
                )
        );

        adapter =
                new GroupFriendAdapter(
                        friendList
                );

        recyclerView.setAdapter(
                adapter
        );

        Log.d(
                TAG,
                "RecyclerView / Adapter 초기화 완료"
        );

        loadFriends();

        btnCreate.setOnClickListener(v -> {

            Log.d(
                    TAG,
                    "그룹 생성 버튼 클릭"
            );

            createGroupRoom();
        });
    }

    private void setupRoomNameInput() {

        if (etRoomName == null) {

            return;
        }

        etRoomName.setSingleLine(
                true
        );

        etRoomName.setMaxLines(
                1
        );

        etRoomName.setFilters(
                new InputFilter[]{
                        new InputFilter.LengthFilter(
                                25
                        ),
                        (source, start, end, dest, dstart, dend) -> {

                            String input =
                                    source.toString();

                            if (input.contains(
                                    "\n"
                            )
                                    || input.contains(
                                    "\r"
                            )) {

                                return input
                                        .replace(
                                                "\n",
                                                ""
                                        )
                                        .replace(
                                                "\r",
                                                ""
                                        );
                            }

                            return null;
                        }
                }
        );
    }

    private void loadFriends() {

        Log.d(
                TAG,
                "loadFriends 시작"
        );

        ApiService apiService =
                RetrofitClient.getApiService(
                        this
                );

        apiService.getCurrentUser()
                .enqueue(new Callback<LoginResponseDto>() {

                    @Override
                    public void onResponse(
                            Call<LoginResponseDto> call,
                            Response<LoginResponseDto> response
                    ) {

                        Log.d(
                                TAG,
                                "getCurrentUser 응답 코드 = "
                                        + response.code()
                        );

                        if (response.isSuccessful()
                                && response.body() != null) {

                            myId =
                                    response.body()
                                            .getMemberId();

                            Log.d(
                                    TAG,
                                    "현재 사용자 myId = "
                                            + myId
                            );

                            if (!isCreatingRoom) {

                                btnCreate.setEnabled(
                                        true
                                );
                            }

                            apiService.getMyFriends()
                                    .enqueue(new Callback<List<FriendResponseDto>>() {

                                        @Override
                                        public void onResponse(
                                                Call<List<FriendResponseDto>> call,
                                                Response<List<FriendResponseDto>> response
                                        ) {

                                            Log.d(
                                                    TAG,
                                                    "getMyFriends 응답 코드 = "
                                                            + response.code()
                                            );

                                            if (response.isSuccessful()
                                                    && response.body() != null) {

                                                friendList.clear();

                                                friendList.addAll(
                                                        response.body()
                                                );

                                                Log.d(
                                                        TAG,
                                                        "친구 목록 개수 = "
                                                                + friendList.size()
                                                );

                                                for (FriendResponseDto friend : friendList) {

                                                    Log.d(
                                                            TAG,
                                                            "친구 로드됨 memberId="
                                                                    + friend.getMemberId()
                                                                    + ", nickname="
                                                                    + friend.getNickname()
                                                                    + ", selected="
                                                                    + friend.isSelected()
                                                    );
                                                }

                                                adapter.notifyDataSetChanged();

                                            } else {

                                                String error =
                                                        getErrorBody(
                                                                response
                                                        );

                                                Log.e(
                                                        TAG,
                                                        "친구 목록 불러오기 실패 code="
                                                                + response.code()
                                                                + ", error="
                                                                + error
                                                );

                                                Toast.makeText(
                                                        GroupCreateActivity.this,
                                                        "친구 목록을 불러오지 못했습니다.",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            }
                                        }

                                        @Override
                                        public void onFailure(
                                                Call<List<FriendResponseDto>> call,
                                                Throwable t
                                        ) {

                                            Log.e(
                                                    TAG,
                                                    "getMyFriends 통신 실패",
                                                    t
                                            );

                                            Toast.makeText(
                                                    GroupCreateActivity.this,
                                                    "친구 목록 조회 실패",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    });

                        } else {

                            String error =
                                    getErrorBody(
                                            response
                                    );

                            Log.e(
                                    TAG,
                                    "현재 사용자 조회 실패 code="
                                            + response.code()
                                            + ", error="
                                            + error
                            );

                            btnCreate.setEnabled(
                                    false
                            );

                            Toast.makeText(
                                    GroupCreateActivity.this,
                                    "사용자 정보를 불러오지 못했습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<LoginResponseDto> call,
                            Throwable t
                    ) {

                        Log.e(
                                TAG,
                                "getCurrentUser 통신 실패",
                                t
                        );

                        btnCreate.setEnabled(
                                false
                        );

                        Toast.makeText(
                                GroupCreateActivity.this,
                                "사용자 정보 조회 실패",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void createGroupRoom() {

        Log.d(
                TAG,
                "createGroupRoom 시작"
        );

        if (isCreatingRoom) {

            Log.d(
                    TAG,
                    "createGroupRoom 중복 클릭 방지"
            );

            return;
        }

        if (myId == null) {

            Log.e(
                    TAG,
                    "createGroupRoom 중단: myId == null"
            );

            Toast.makeText(
                    this,
                    "사용자 정보를 불러오는 중입니다. 잠시 후 다시 시도해주세요.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        String roomName =
                etRoomName.getText()
                        .toString()
                        .trim();

        Log.d(
                TAG,
                "입력된 roomName = " + roomName
        );

        if (roomName.isEmpty()) {

            Log.e(
                    TAG,
                    "createGroupRoom 중단: roomName 비어있음"
            );

            Toast.makeText(
                    this,
                    "방 제목을 입력해주세요.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (roomName.length() > 25) {

            Toast.makeText(
                    this,
                    "방 제목은 최대 25자까지 입력 가능합니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        isCreatingRoom =
                true;

        btnCreate.setEnabled(
                false
        );

        List<Long> memberIds =
                new ArrayList<>();

        memberIds.add(
                myId
        );

        Log.d(
                TAG,
                "초기 memberIds = " + memberIds
        );

        if (fromRoomId != -1) {

            Log.d(
                    TAG,
                    "기존 방 참여자 포함 모드 fromRoomId = "
                            + fromRoomId
            );

            ApiService apiService =
                    RetrofitClient.getApiService(
                            this
                    );

            apiService.getParticipants(
                    fromRoomId
            ).enqueue(new Callback<List<FriendResponseDto>>() {

                @Override
                public void onResponse(
                        Call<List<FriendResponseDto>> call,
                        Response<List<FriendResponseDto>> response
                ) {

                    Log.d(
                            TAG,
                            "getParticipants 응답 코드 = "
                                    + response.code()
                    );

                    if (response.isSuccessful()
                            && response.body() != null) {

                        Log.d(
                                TAG,
                                "기존 방 참여자 수 = "
                                        + response.body().size()
                        );

                        for (FriendResponseDto friend : response.body()) {

                            Log.d(
                                    TAG,
                                    "기존 참여자 확인 memberId="
                                            + friend.getMemberId()
                                            + ", nickname="
                                            + friend.getNickname()
                            );

                            if (friend.getMemberId() != null
                                    && !memberIds.contains(
                                    friend.getMemberId()
                            )) {

                                memberIds.add(
                                        friend.getMemberId()
                                );

                                Log.d(
                                        TAG,
                                        "기존 참여자 추가됨 memberId="
                                                + friend.getMemberId()
                                );
                            }
                        }

                        Log.d(
                                TAG,
                                "기존 참여자 추가 후 memberIds = "
                                        + memberIds
                        );

                        addSelectedFriends(
                                memberIds
                        );

                    } else {

                        isCreatingRoom =
                                false;

                        btnCreate.setEnabled(
                                true
                        );

                        String error =
                                getErrorBody(
                                        response
                                );

                        Log.e(
                                TAG,
                                "참여자 조회 실패 code="
                                        + response.code()
                                        + ", error="
                                        + error
                        );

                        Toast.makeText(
                                GroupCreateActivity.this,
                                "참여자 조회 실패",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }

                @Override
                public void onFailure(
                        Call<List<FriendResponseDto>> call,
                        Throwable t
                ) {

                    isCreatingRoom =
                            false;

                    btnCreate.setEnabled(
                            true
                    );

                    Log.e(
                            TAG,
                            "getParticipants 통신 실패",
                            t
                    );

                    Toast.makeText(
                            GroupCreateActivity.this,
                            "참여자 조회 실패",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            });

            return;
        }

        addSelectedFriends(
                memberIds
        );
    }

    private void addSelectedFriends(
            List<Long> memberIds
    ) {

        Log.d(
                TAG,
                "addSelectedFriends 시작"
        );

        Log.d(
                TAG,
                "선택 친구 추가 전 memberIds = "
                        + memberIds
        );

        Log.d(
                TAG,
                "friendList size = "
                        + friendList.size()
        );

        for (FriendResponseDto friend : friendList) {

            Log.d(
                    TAG,
                    "친구 선택 상태 확인 memberId="
                            + friend.getMemberId()
                            + ", nickname="
                            + friend.getNickname()
                            + ", selected="
                            + friend.isSelected()
            );

            if (friend.isSelected()) {

                if (friend.getMemberId() != null
                        && !memberIds.contains(
                        friend.getMemberId()
                )) {

                    memberIds.add(
                            friend.getMemberId()
                    );

                    Log.d(
                            TAG,
                            "선택 친구 추가됨 memberId="
                                    + friend.getMemberId()
                    );

                } else {

                    Log.d(
                            TAG,
                            "선택 친구 추가 스킵 memberId="
                                    + friend.getMemberId()
                                    + ", 이미 포함 또는 null"
                    );
                }
            }
        }

        Log.d(
                TAG,
                "선택 친구 추가 후 최종 memberIds = "
                        + memberIds
        );

        createRoom(
                memberIds
        );
    }

    private void createRoom(
            List<Long> memberIds
    ) {

        Log.d(
                TAG,
                "createRoom 시작"
        );

        Log.d(
                TAG,
                "최종 memberIds = "
                        + memberIds
        );

        String roomName =
                etRoomName.getText()
                        .toString()
                        .trim();

        Log.d(
                TAG,
                "생성 요청 roomName = "
                        + roomName
        );

        Log.d(
                TAG,
                "생성 요청 roomType = GROUP"
        );

        CreateRoomRequestDto request =
                new CreateRoomRequestDto();

        request.setRoomName(
                roomName
        );

        request.setRoomType(
                "GROUP"
        );

        request.setMemberIds(
                memberIds
        );

        ApiService apiService =
                RetrofitClient.getApiService(
                        this
                );

        apiService.createRoom(
                request
        ).enqueue(new Callback<ChatRoom>() {

            @Override
            public void onResponse(
                    Call<ChatRoom> call,
                    Response<ChatRoom> response
            ) {

                Log.d(
                        TAG,
                        "createRoom 응답 코드 = "
                                + response.code()
                );

                if (response.isSuccessful()
                        && response.body() != null) {

                    ChatRoom room =
                            response.body();

                    Log.d(
                            TAG,
                            "그룹 생성 성공 roomId = "
                                    + room.getRoomId()
                    );

                    Log.d(
                            TAG,
                            "그룹 생성 성공 roomName = "
                                    + room.getRoomName()
                    );

                    Log.d(
                            TAG,
                            "그룹 생성 성공 roomType = "
                                    + room.getRoomType()
                    );

                    Toast.makeText(
                            GroupCreateActivity.this,
                            "그룹 채팅 생성 완료",
                            Toast.LENGTH_SHORT
                    ).show();

                    Intent resultIntent =
                            new Intent();

                    resultIntent.putExtra(
                            "roomCreated",
                            true
                    );

                    resultIntent.putExtra(
                            "roomId",
                            room.getRoomId()
                    );

                    resultIntent.putExtra(
                            "roomName",
                            room.getRoomName()
                    );

                    resultIntent.putExtra(
                            "roomType",
                            "GROUP"
                    );

                    setResult(
                            RESULT_OK,
                            resultIntent
                    );

                    finish();

                } else {

                    isCreatingRoom =
                            false;

                    btnCreate.setEnabled(
                            true
                    );

                    String error =
                            getErrorBody(
                                    response
                            );

                    Log.e(
                            TAG,
                            "그룹 생성 실패 code="
                                    + response.code()
                                    + ", error="
                                    + error
                    );

                    Toast.makeText(
                            GroupCreateActivity.this,
                            "그룹 채팅 생성 실패",
                            Toast.LENGTH_SHORT
                    ).show();
                }
            }

            @Override
            public void onFailure(
                    Call<ChatRoom> call,
                    Throwable t
            ) {

                isCreatingRoom =
                        false;

                btnCreate.setEnabled(
                        true
                );

                Log.e(
                        TAG,
                        "createRoom 통신 실패",
                        t
                );

                Toast.makeText(
                        GroupCreateActivity.this,
                        "서버 연결 실패",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private String getErrorBody(
            Response<?> response
    ) {

        Log.d(
                TAG,
                "getErrorBody 호출"
        );

        try {

            if (response.errorBody() != null) {

                String error =
                        response.errorBody()
                                .string();

                Log.e(
                        TAG,
                        "errorBody = "
                                + error
                );

                return error;
            }

        } catch (Exception e) {

            Log.e(
                    TAG,
                    "에러 바디 읽기 실패",
                    e
            );
        }

        return "";
    }
}