package com.tech.motjip;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.API.ApiService;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Adapter.GroupFriendAdapter;
import com.tech.motjip.Dto.ResponseDto.FriendResponseDto;
import com.tech.motjip.Model.Participant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InviteFriendActivity
        extends AppCompatActivity {

    private RecyclerView recyclerView;

    private Button btnInvite;

    private GroupFriendAdapter adapter;

    private final List<FriendResponseDto> friendList =
            new ArrayList<>();

    private final Set<Long> currentParticipantIds =
            new HashSet<>();

    private Long roomId = -1L;

    private Long myMemberId = -1L;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {

        super.onCreate(
                savedInstanceState
        );

        setContentView(
                R.layout.activity_invite_friend
        );

        roomId =
                getIntent().getLongExtra(
                        "roomId",
                        -1L
                );

        SharedPreferences prefs =
                getSharedPreferences(
                        "auth",
                        MODE_PRIVATE
                );

        myMemberId =
                prefs.getLong(
                        "memberId",
                        -1L
                );

        recyclerView =
                findViewById(
                        R.id.rv_invite_friend_list
                );

        btnInvite =
                findViewById(
                        R.id.btn_invite_selected
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

        btnInvite.setEnabled(
                false
        );

        loadCurrentParticipants();

        btnInvite.setOnClickListener(v ->
                inviteSelectedFriends()
        );
    }

    private void loadCurrentParticipants() {

        if (roomId == null
                || roomId <= 0) {

            Toast.makeText(
                    this,
                    "채팅방 정보를 찾을 수 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        ApiService apiService =
                RetrofitClient.getApiService(
                        this
                );

        apiService.getRoomMembers(
                        roomId
                )
                .enqueue(new Callback<List<Participant>>() {

                    @Override
                    public void onResponse(
                            Call<List<Participant>> call,
                            Response<List<Participant>> response
                    ) {

                        currentParticipantIds.clear();

                        if (response.isSuccessful()
                                && response.body() != null) {

                            for (Participant participant : response.body()) {

                                if (participant != null
                                        && participant.getMemberId() != null) {

                                    currentParticipantIds.add(
                                            participant.getMemberId()
                                    );
                                }
                            }

                        } else {

                            Log.e(
                                    "INVITE",
                                    "참여자 목록 조회 실패 code="
                                            + response.code()
                            );
                        }

                        loadFriends();
                    }

                    @Override
                    public void onFailure(
                            Call<List<Participant>> call,
                            Throwable t
                    ) {

                        Log.e(
                                "INVITE",
                                "참여자 조회 실패",
                                t
                        );

                        loadFriends();
                    }
                });
    }

    private void loadFriends() {

        ApiService apiService =
                RetrofitClient.getApiService(
                        this
                );

        apiService.getMyFriends()
                .enqueue(new Callback<List<FriendResponseDto>>() {

                    @Override
                    public void onResponse(
                            Call<List<FriendResponseDto>> call,
                            Response<List<FriendResponseDto>> response
                    ) {

                        if (response.isSuccessful()
                                && response.body() != null) {

                            friendList.clear();

                            for (FriendResponseDto friend : response.body()) {

                                if (friend == null
                                        || friend.getMemberId() == null) {

                                    continue;
                                }

                                if (currentParticipantIds.contains(
                                        friend.getMemberId()
                                )) {

                                    continue;
                                }

                                friend.setSelected(
                                        false
                                );

                                friendList.add(
                                        friend
                                );
                            }

                            adapter.notifyDataSetChanged();

                            btnInvite.setEnabled(
                                    !friendList.isEmpty()
                            );

                            if (friendList.isEmpty()) {

                                Toast.makeText(
                                        InviteFriendActivity.this,
                                        "초대 가능한 친구가 없습니다.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }

                        } else {

                            Toast.makeText(
                                    InviteFriendActivity.this,
                                    "친구 목록을 불러오지 못했습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            btnInvite.setEnabled(
                                    false
                            );
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<List<FriendResponseDto>> call,
                            Throwable t
                    ) {

                        Log.e(
                                "INVITE",
                                "친구 목록 조회 실패",
                                t
                        );

                        Toast.makeText(
                                InviteFriendActivity.this,
                                "친구 목록 조회 실패",
                                Toast.LENGTH_SHORT
                        ).show();

                        btnInvite.setEnabled(
                                false
                        );
                    }
                });
    }

    private void inviteSelectedFriends() {

        if (roomId == null
                || roomId <= 0) {

            Toast.makeText(
                    this,
                    "채팅방 정보를 찾을 수 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        if (myMemberId == null
                || myMemberId <= 0) {

            Toast.makeText(
                    this,
                    "로그인 정보를 찾을 수 없습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        List<Long> selectedFriendIds =
                new ArrayList<>();

        for (FriendResponseDto friend : friendList) {

            if (friend.isSelected()
                    && friend.getMemberId() != null
                    && !currentParticipantIds.contains(
                    friend.getMemberId()
            )) {

                selectedFriendIds.add(
                        friend.getMemberId()
                );
            }
        }

        if (selectedFriendIds.isEmpty()) {

            Toast.makeText(
                    this,
                    "초대할 친구를 선택해주세요.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        btnInvite.setEnabled(
                false
        );

        ApiService apiService =
                RetrofitClient.getApiService(
                        this
                );

        final int[] successCount = {0};
        final int[] failCount = {0};

        final int totalCount =
                selectedFriendIds.size();

        for (Long friendId : selectedFriendIds) {

            apiService.inviteFriendToRoom(
                    roomId,
                    friendId,
                    myMemberId
            ).enqueue(new Callback<String>() {

                @Override
                public void onResponse(
                        Call<String> call,
                        Response<String> response
                ) {

                    if (response.isSuccessful()) {

                        successCount[0]++;

                    } else {

                        failCount[0]++;

                        String errorMessage =
                                "친구 초대 실패";

                        try {

                            if (response.errorBody() != null) {

                                errorMessage =
                                        response.errorBody().string();
                            }

                        } catch (Exception e) {

                            Log.e(
                                    "INVITE",
                                    "초대 실패 응답 파싱 실패",
                                    e
                            );
                        }

                        Log.e(
                                "INVITE",
                                errorMessage
                        );
                    }

                    checkInviteFinished(
                            successCount[0],
                            failCount[0],
                            totalCount
                    );
                }

                @Override
                public void onFailure(
                        Call<String> call,
                        Throwable t
                ) {

                    failCount[0]++;

                    Log.e(
                            "INVITE",
                            "친구 초대 실패",
                            t
                    );

                    checkInviteFinished(
                            successCount[0],
                            failCount[0],
                            totalCount
                    );
                }
            });
        }
    }

    private void checkInviteFinished(
            int successCount,
            int failCount,
            int totalCount
    ) {

        if (successCount + failCount < totalCount) {

            return;
        }

        if (successCount > 0) {

            Toast.makeText(
                    this,
                    "친구 초대 완료",
                    Toast.LENGTH_SHORT
            ).show();

            Intent resultIntent =
                    new Intent();

            resultIntent.putExtra(
                    "reloadMembers",
                    true
            );

            setResult(
                    RESULT_OK,
                    resultIntent
            );

            finish();

            return;
        }

        btnInvite.setEnabled(
                true
        );

        Toast.makeText(
                this,
                "친구 초대 실패",
                Toast.LENGTH_SHORT
        ).show();
    }
}