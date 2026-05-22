package com.tech.motjip;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.tech.motjip.Adapter.CommunityMemberAdapter;
import com.tech.motjip.Controller.CommunityController;
import com.tech.motjip.Controller.FriendController;
import com.tech.motjip.Dto.ResponseDto.FriendStatusResponseDto;
import com.tech.motjip.Model.CommunityMember;
import com.tech.motjip.Utils.DialogUtil;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CommunityMemberListActivity
        extends AppCompatActivity {

    private RecyclerView recyclerViewMembers;

    private TextView tvEmpty;

    private CommunityMemberAdapter adapter;

    private CommunityController communityController;

    private FriendController friendController;

    private Long comId;

    @Override
    protected void onCreate(
            @Nullable Bundle savedInstanceState
    ) {
        super.onCreate(savedInstanceState);

        setContentView(
                R.layout.activity_community_member_list
        );

        recyclerViewMembers =
                findViewById(R.id.recyclerViewMembers);

        tvEmpty =
                findViewById(R.id.tvEmpty);

        communityController =
                new CommunityController(this);

        friendController =
                new FriendController(this);

        adapter =
                new CommunityMemberAdapter(
                        this,
                        member -> showKickConfirmDialog(member),
                        member -> handleFriendClick(member)
                );

        recyclerViewMembers.setLayoutManager(
                new LinearLayoutManager(this)
        );

        recyclerViewMembers.setAdapter(adapter);

        comId =
                getIntent().getLongExtra(
                        "comId",
                        -1
                );

        if (comId == -1) {

            Toast.makeText(
                    this,
                    "게시글 정보가 올바르지 않습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            finish();

            return;
        }

        loadCommunityMembers();
    }

    private void loadCommunityMembers() {

        communityController.getCommunityMembers(
                comId,
                new CommunityController.CommunityMembersCallback() {

                    @Override
                    public void onSuccess(
                            List<CommunityMember> members
                    ) {

                        if (members == null || members.isEmpty()) {

                            tvEmpty.setVisibility(View.VISIBLE);

                            recyclerViewMembers.setVisibility(View.GONE);

                            return;
                        }

                        tvEmpty.setVisibility(View.GONE);

                        recyclerViewMembers.setVisibility(View.VISIBLE);

                        adapter.setMembers(
                                members
                        );
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                CommunityMemberListActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void handleFriendClick(
            CommunityMember member
    ) {

        if (member == null
                || member.getMemberId() == null) {

            Toast.makeText(
                    this,
                    "사용자 정보가 올바르지 않습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        friendController.getFriendStatus(
                member.getMemberId(),
                new Callback<FriendStatusResponseDto>() {

                    @Override
                    public void onResponse(
                            Call<FriendStatusResponseDto> call,
                            Response<FriendStatusResponseDto> response
                    ) {

                        if (!response.isSuccessful()
                                || response.body() == null
                                || response.body().getStatus() == null) {

                            Toast.makeText(
                                    CommunityMemberListActivity.this,
                                    "친구 상태를 확인할 수 없습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            return;
                        }

                        String status =
                                response.body().getStatus();

                        if ("FRIEND".equals(status)) {

                            showDeleteFriendConfirmDialog(
                                    member
                            );

                        } else if ("PENDING".equals(status)) {

                            showMessageDialog(
                                    "이미 친구 요청을 보낸 상태입니다."
                            );

                        } else if ("RECEIVED".equals(status)) {

                            showMessageDialog(
                                    "상대방이 이미 친구 요청을 보냈습니다. 알림 페이지에서 수락 또는 거절하세요."
                            );

                        } else {

                            sendFriendRequest(member);
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<FriendStatusResponseDto> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                CommunityMemberListActivity.this,
                                "서버 연결 실패",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void showDeleteFriendConfirmDialog(
            CommunityMember member
    ) {

        DialogUtil.showConfirmDialog(
                this,
                R.drawable.ic_warning,
                "친구 삭제",
                member.getNickname()
                        + "님을 친구 목록에서 삭제하시겠습니까?\n\n"
                        + "삭제 후 5분 동안 다시 친구 요청을 보낼 수 없습니다.",
                () -> deleteFriend(member)
        );
    }

    private void sendFriendRequest(
            CommunityMember member
    ) {

        if (member == null
                || member.getMemberId() == null) {

            Toast.makeText(
                    this,
                    "사용자 정보가 올바르지 않습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        friendController.sendFriendRequest(
                member.getMemberId(),
                new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    CommunityMemberListActivity.this,
                                    member.getNickname()
                                            + "님에게 친구 요청을 보냈습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            member.setFriend(false);

                            member.setFriendStatus(
                                    "PENDING"
                            );

                            adapter.notifyDataSetChanged();

                            return;
                        }

                        String errorMessage =
                                getErrorMessage(response);

                        showMessageDialog(
                                errorMessage
                        );
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        showMessageDialog(
                                "서버 연결 실패"
                        );
                    }
                }
        );
    }

    private void deleteFriend(
            CommunityMember member
    ) {

        if (member == null
                || member.getMemberId() == null) {

            Toast.makeText(
                    this,
                    "사용자 정보가 올바르지 않습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        friendController.deleteFriend(
                member.getMemberId(),
                new Callback<Void>() {

                    @Override
                    public void onResponse(
                            Call<Void> call,
                            Response<Void> response
                    ) {

                        if (response.isSuccessful()) {

                            Toast.makeText(
                                    CommunityMemberListActivity.this,
                                    "친구를 삭제했습니다.",
                                    Toast.LENGTH_SHORT
                            ).show();

                            member.setFriend(false);

                            member.setFriendStatus(
                                    "NONE"
                            );

                            adapter.notifyDataSetChanged();

                        } else {

                            String errorMessage =
                                    getErrorMessage(response);

                            Toast.makeText(
                                    CommunityMemberListActivity.this,
                                    errorMessage,
                                    Toast.LENGTH_SHORT
                            ).show();
                        }
                    }

                    @Override
                    public void onFailure(
                            Call<Void> call,
                            Throwable t
                    ) {

                        Toast.makeText(
                                CommunityMemberListActivity.this,
                                "서버 연결 실패",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void showKickConfirmDialog(
            CommunityMember member
    ) {

        if (member == null
                || member.getMemberId() == null) {

            Toast.makeText(
                    this,
                    "참여자 정보가 올바르지 않습니다.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        DialogUtil.showConfirmDialog(
                this,
                R.drawable.ic_warning,
                "참여자 추방",
                member.getNickname()
                        + "님을 모임에서 추방하시겠습니까?",
                () -> kickMember(member)
        );
    }

    private void kickMember(
            CommunityMember member
    ) {

        communityController.kickCommunityMember(
                comId,
                member.getMemberId(),
                new CommunityController.KickMemberCallback() {

                    @Override
                    public void onSuccess() {

                        Toast.makeText(
                                CommunityMemberListActivity.this,
                                "참여자를 추방했습니다.",
                                Toast.LENGTH_SHORT
                        ).show();

                        loadCommunityMembers();
                    }

                    @Override
                    public void onError(
                            String message
                    ) {

                        Toast.makeText(
                                CommunityMemberListActivity.this,
                                message,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                }
        );
    }

    private void showMessageDialog(
            String message
    ) {

        new AlertDialog.Builder(
                CommunityMemberListActivity.this
        )
                .setTitle("알림")
                .setMessage(message)
                .setPositiveButton(
                        "확인",
                        null
                )
                .show();
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