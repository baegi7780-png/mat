package com.tech.motjip;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Adapter.FriendAdapter;
import com.tech.motjip.Dto.RequestDto.CreateRoomRequestDto;
import com.tech.motjip.Dto.ResponseDto.CommunityPostPageResponse;
import com.tech.motjip.Dto.ResponseDto.FriendRecommendationResponseDto;
import com.tech.motjip.Dto.ResponseDto.FriendResponseDto;
import com.tech.motjip.Dto.ResponseDto.FriendStatusResponseDto;
import com.tech.motjip.Dto.ResponseDto.LoginResponseDto;
import com.tech.motjip.Model.ChatRoom;
import com.tech.motjip.Model.CommunityMember;
import com.tech.motjip.Model.CommunityPost;
import com.tech.motjip.Utils.DialogUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendActivity
        extends AppCompatActivity {

    private static final String TAG =
            "FriendActivity";

    private TextView tvFriendList;
    private TextView tvFriendRecommend;
    private TextView tvFriendEmpty;

    private RecyclerView recyclerViewFriend;

    private FriendAdapter friendAdapter;

    private boolean showingRecommend = false;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_friend);

        tvFriendList = findViewById(R.id.tvFriendList);
        tvFriendRecommend = findViewById(R.id.tvFriendRecommend);
        tvFriendEmpty = findViewById(R.id.tvFriendEmpty);
        recyclerViewFriend = findViewById(R.id.recyclerViewFriend);

        friendAdapter =
                new FriendAdapter(
                        this,
                        new FriendAdapter.OnFriendClickListener() {

                            @Override
                            public void onChatClick(
                                    FriendResponseDto friend
                            ) {

                                createDirectRoom(
                                        friend
                                );
                            }

                            @Override
                            public void onInviteClick(
                                    FriendResponseDto friend
                            ) {

                                RetrofitClient.getApiService(
                                        FriendActivity.this
                                ).getMyCommunityPosts(
                                        "new",
                                        0,
                                        50
                                ).enqueue(new Callback<CommunityPostPageResponse>() {

                                    @Override
                                    public void onResponse(
                                            Call<CommunityPostPageResponse> call,
                                            Response<CommunityPostPageResponse> response
                                    ) {

                                        if (response.isSuccessful()
                                                && response.body() != null
                                                && response.body().getContent() != null
                                                && !response.body().getContent().isEmpty()) {

                                            filterInvitableCommunities(
                                                    friend,
                                                    response.body().getContent()
                                            );

                                        } else {

                                            Toast.makeText(
                                                    FriendActivity.this,
                                                    "초대 가능한 모임이 없습니다.",
                                                    Toast.LENGTH_SHORT
                                            ).show();
                                        }
                                    }

                                    @Override
                                    public void onFailure(
                                            Call<CommunityPostPageResponse> call,
                                            Throwable t
                                    ) {

                                        Toast.makeText(
                                                FriendActivity.this,
                                                "모임 목록 조회 실패",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                    }
                                });
                            }

                            @Override
                            public void onRecommendAddClick(
                                    FriendResponseDto friend
                            ) {

                                sendFriendRequest(
                                        friend.getMemberId()
                                );
                            }

                            @Override
                            public void onDeleteFriendClick(
                                    FriendResponseDto friend
                            ) {

                                DialogUtil.showConfirmDialog(
                                        FriendActivity.this,
                                        R.drawable.ic_warning,
                                        "친구 삭제",
                                        friend.getNickname()
                                                + "님을 친구 목록에서 삭제하시겠습니까?\n\n"
                                                + "삭제 후 5분 동안 다시 친구 요청을 보낼 수 없습니다.",
                                        () -> deleteFriend(
                                                friend.getMemberId()
                                        )
                                );
                            }
                        }
                );

        recyclerViewFriend.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerViewFriend.setAdapter(friendAdapter);

        tvFriendList.setOnClickListener(v -> {
            showingRecommend = false;
            updateTabUi();
            loadFriendList();
        });

        tvFriendRecommend.setOnClickListener(v -> {
            showingRecommend = true;
            updateTabUi();
            loadRecommendFriends();
        });

        updateTabUi();
        loadFriendList();
    }

    private void updateTabUi() {

        if (showingRecommend) {

            tvFriendRecommend.setBackgroundResource(
                    R.drawable.bg_orange_fill_round
            );

            tvFriendRecommend.setTextColor(
                    getColor(android.R.color.white)
            );

            tvFriendList.setBackgroundResource(
                    R.drawable.bg_gray_round
            );

            tvFriendList.setTextColor(
                    0xFF444444
            );

        } else {

            tvFriendList.setBackgroundResource(
                    R.drawable.bg_orange_fill_round
            );

            tvFriendList.setTextColor(
                    getColor(android.R.color.white)
            );

            tvFriendRecommend.setBackgroundResource(
                    R.drawable.bg_gray_round
            );

            tvFriendRecommend.setTextColor(
                    0xFF444444
            );
        }
    }

    private void loadFriendList() {

        recyclerViewFriend.setVisibility(View.VISIBLE);
        tvFriendEmpty.setVisibility(View.GONE);

        RetrofitClient.getApiService(this)
                .getMyFriends()
                .enqueue(new Callback<List<FriendResponseDto>>() {

                    @Override
                    public void onResponse(
                            Call<List<FriendResponseDto>> call,
                            Response<List<FriendResponseDto>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {

                            friendAdapter.setFriends(
                                    response.body()
                            );

                            recyclerViewFriend.setVisibility(
                                    View.VISIBLE
                            );

                            tvFriendEmpty.setVisibility(
                                    View.GONE
                            );

                        } else {

                            friendAdapter.setFriends(null);

                            recyclerViewFriend.setVisibility(
                                    View.GONE
                            );

                            tvFriendEmpty.setText(
                                    "친구 목록이 없습니다."
                            );

                            tvFriendEmpty.setVisibility(
                                    View.VISIBLE
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<FriendResponseDto>> call,
                            Throwable t
                    ) {

                        friendAdapter.setFriends(null);

                        recyclerViewFriend.setVisibility(
                                View.GONE
                        );

                        tvFriendEmpty.setText(
                                "친구 목록을 불러오지 못했습니다."
                        );

                        tvFriendEmpty.setVisibility(
                                View.VISIBLE
                        );
                    }
                });
    }

    private void loadRecommendFriends() {

        recyclerViewFriend.setVisibility(
                View.GONE
        );

        tvFriendEmpty.setText(
                "친구 추천 목록을 불러오는 중입니다."
        );

        tvFriendEmpty.setVisibility(
                View.VISIBLE
        );

        RetrofitClient.getApiService(this)
                .getFriendRecommendations()
                .enqueue(new Callback<List<FriendRecommendationResponseDto>>() {

                    @Override
                    public void onResponse(
                            Call<List<FriendRecommendationResponseDto>> call,
                            Response<List<FriendRecommendationResponseDto>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null
                                && !response.body().isEmpty()) {

                            friendAdapter.setRecommendedFriends(
                                    response.body()
                            );

                            recyclerViewFriend.setVisibility(
                                    View.VISIBLE
                            );

                            tvFriendEmpty.setVisibility(
                                    View.GONE
                            );

                        } else {

                            friendAdapter.setFriends(null);

                            recyclerViewFriend.setVisibility(
                                    View.GONE
                            );

                            tvFriendEmpty.setText(
                                    "추천 가능한 친구가 없습니다."
                            );

                            tvFriendEmpty.setVisibility(
                                    View.VISIBLE
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<FriendRecommendationResponseDto>> call,
                            Throwable t
                    ) {

                        friendAdapter.setFriends(null);

                        recyclerViewFriend.setVisibility(
                                View.GONE
                        );

                        tvFriendEmpty.setText(
                                "친구 추천 목록을 불러오지 못했습니다."
                        );

                        tvFriendEmpty.setVisibility(
                                View.VISIBLE
                        );
                    }
                });
    }

    private void createDirectRoom(
            FriendResponseDto friend
    ) {

        ApiService apiService =
                RetrofitClient.getApiService(this);

        apiService.getCurrentUser()
                .enqueue(new Callback<LoginResponseDto>() {

                    @Override
                    public void onResponse(
                            Call<LoginResponseDto> call,
                            Response<LoginResponseDto> response
                    ) {

                        if (!response.isSuccessful()
                                || response.body() == null) {

                            Toast.makeText(
                                    FriendActivity.this,
                                    "내 정보를 불러오지 못했습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        Long myId =
                                response.body().getMemberId();

                        Long targetMemberId =
                                friend.getMemberId();

                        if (myId == null
                                || targetMemberId == null) {

                            Toast.makeText(
                                    FriendActivity.this,
                                    "채팅방 생성에 필요한 회원 정보가 없습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        apiService.findDirectRoom(
                                myId,
                                targetMemberId
                        ).enqueue(new Callback<ChatRoom>() {

                            @Override
                            public void onResponse(
                                    Call<ChatRoom> call,
                                    Response<ChatRoom> roomResponse
                            ) {

                                if (roomResponse.isSuccessful()
                                        && roomResponse.body() != null) {

                                    moveToChatRoom(
                                            roomResponse.body()
                                    );

                                    return;
                                }

                                createNewDirectRoom(
                                        apiService,
                                        myId,
                                        friend
                                );
                            }

                            @Override
                            public void onFailure(
                                    Call<ChatRoom> call,
                                    Throwable t
                            ) {

                                Log.e(
                                        TAG,
                                        "DIRECT 방 조회 실패",
                                        t
                                );

                                createNewDirectRoom(
                                        apiService,
                                        myId,
                                        friend
                                );
                            }
                        });
                    }

                    @Override
                    public void onFailure(
                            Call<LoginResponseDto> call,
                            Throwable t
                    ) {

                        Log.e(
                                TAG,
                                "내 정보 조회 실패",
                                t
                        );

                        Toast.makeText(
                                FriendActivity.this,
                                "내 정보를 불러오지 못했습니다.",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void createNewDirectRoom(
            ApiService apiService,
            Long myId,
            FriendResponseDto friend
    ) {

        CreateRoomRequestDto request =
                new CreateRoomRequestDto();

        request.setRoomName(
                friend.getNickname()
        );

        request.setRoomType(
                "DIRECT"
        );

        List<Long> memberIds =
                new ArrayList<>();

        memberIds.add(
                myId
        );

        memberIds.add(
                friend.getMemberId()
        );

        request.setMemberIds(
                memberIds
        );

        apiService.createRoom(
                request
        ).enqueue(new Callback<ChatRoom>() {

            @Override
            public void onResponse(
                    Call<ChatRoom> call,
                    Response<ChatRoom> response
            ) {

                if (response.isSuccessful()
                        && response.body() != null) {

                    moveToChatRoom(
                            response.body()
                    );

                    return;
                }

                Toast.makeText(
                        FriendActivity.this,
                        "채팅방 생성에 실패했습니다.",
                        Toast.LENGTH_SHORT
                ).show();
            }

            @Override
            public void onFailure(
                    Call<ChatRoom> call,
                    Throwable t
            ) {

                Log.e(
                        TAG,
                        "채팅방 생성 실패",
                        t
                );

                Toast.makeText(
                        FriendActivity.this,
                        "채팅방 생성 중 서버 오류가 발생했습니다.",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });
    }

    private void moveToChatRoom(
            ChatRoom room
    ) {

        Intent intent =
                new Intent(
                        FriendActivity.this,
                        MessageActivity.class
                );

        intent.putExtra(
                "roomId",
                room.getRoomId()
        );

        intent.putExtra(
                "roomName",
                room.getRoomName()
        );

        startActivity(
                intent
        );
    }

    private void sendFriendRequest(
            Long receiverId
    ) {

        RetrofitClient.getApiService(this)
                .sendFriendRequest(
                        receiverId
                )
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    FriendActivity.this,
                                    "친구 요청을 보냈습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        Toast.makeText(
                                FriendActivity.this,
                                getErrorMessage(response),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                FriendActivity.this,
                                "서버 오류",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void deleteFriend(
            Long friendMemberId
    ) {

        RetrofitClient.getApiService(this)
                .deleteFriend(
                        friendMemberId
                )
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    FriendActivity.this,
                                    "친구가 삭제되었습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            loadFriendList();

                            return;
                        }

                        Toast.makeText(
                                FriendActivity.this,
                                getErrorMessage(response),
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                FriendActivity.this,
                                "서버 오류",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void filterInvitableCommunities(
            FriendResponseDto friend,
            List<CommunityPost> communities
    ) {

        List<CommunityPost> availableCommunities =
                new ArrayList<>();

        if (communities.isEmpty()) {

            Toast.makeText(
                    this,
                    "초대 가능한 모임이 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        final int[] checkedCount = {0};

        for (CommunityPost community : communities) {

            RetrofitClient.getApiService(this)
                    .getCommunityMembers(
                            community.getComId()
                    )
                    .enqueue(new Callback<List<CommunityMember>>() {

                        @Override
                        public void onResponse(
                                Call<List<CommunityMember>> call,
                                Response<List<CommunityMember>> response
                        ) {

                            checkedCount[0]++;

                            boolean alreadyJoined = false;

                            if (response.isSuccessful()
                                    && response.body() != null) {

                                for (CommunityMember member : response.body()) {

                                    if (member.getMemberId()
                                            .equals(friend.getMemberId())) {

                                        alreadyJoined = true;
                                        break;
                                    }
                                }
                            }

                            if (!alreadyJoined) {

                                availableCommunities.add(
                                        community
                                );
                            }

                            if (checkedCount[0]
                                    == communities.size()) {

                                if (availableCommunities.isEmpty()) {

                                    Toast.makeText(
                                            FriendActivity.this,
                                            "초대 가능한 모임이 없습니다.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                } else {

                                    showCommunitySelectDialog(
                                            friend,
                                            availableCommunities
                                    );
                                }
                            }
                        }

                        @Override
                        public void onFailure(
                                Call<List<CommunityMember>> call,
                                Throwable t
                        ) {

                            checkedCount[0]++;

                            if (checkedCount[0]
                                    == communities.size()) {

                                if (availableCommunities.isEmpty()) {

                                    Toast.makeText(
                                            FriendActivity.this,
                                            "초대 가능한 모임이 없습니다.",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                } else {

                                    showCommunitySelectDialog(
                                            friend,
                                            availableCommunities
                                    );
                                }
                            }
                        }
                    });
        }
    }

    private void showCommunitySelectDialog(
            FriendResponseDto friend,
            List<CommunityPost> communities
    ) {

        List<String> titles =
                new ArrayList<>();

        for (CommunityPost post : communities) {

            titles.add(
                    post.getTitle()
            );
        }

        String[] items =
                titles.toArray(
                        new String[0]
                );

        new AlertDialog.Builder(this)
                .setTitle("초대할 모임 선택")
                .setItems(items, (dialog, which) -> {

                    CommunityPost selectedCommunity =
                            communities.get(which);

                    sendCommunityInvite(
                            selectedCommunity.getComId(),
                            friend.getMemberId()
                    );
                })
                .show();
    }

    private void sendCommunityInvite(
            Long communityId,
            Long receiverId
    ) {

        RetrofitClient.getApiService(this)
                .sendCommunityInvite(
                        communityId,
                        receiverId
                )
                .enqueue(new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    FriendActivity.this,
                                    "모임 초대를 보냈습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        String errorMessage =
                                getErrorMessage(response);

                        Toast.makeText(
                                FriendActivity.this,
                                errorMessage,
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                FriendActivity.this,
                                "서버 오류",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private String getErrorMessage(
            Response<?> response
    ) {

        try {

            if (response.errorBody() != null) {

                String message =
                        response.errorBody().string();

                if (message != null
                        && !message.trim().isEmpty()) {

                    return message;
                }
            }

        } catch (IOException e) {

            return "요청 처리 중 오류가 발생했습니다.";
        }

        if (response.code() == 400) {

            return "잘못된 요청입니다.";
        }

        if (response.code() == 401) {

            return "로그인이 필요합니다.";
        }

        if (response.code() == 403) {

            return "권한이 없습니다.";
        }

        if (response.code() == 404) {

            return "대상을 찾을 수 없습니다.";
        }

        if (response.code() == 409) {

            return "이미 처리된 요청입니다.";
        }

        return "요청 처리에 실패했습니다.";
    }
}