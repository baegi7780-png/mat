package com.tech.motjip.Adapter;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Model.CommunityMember;
import com.tech.motjip.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommunityMemberAdapter
        extends RecyclerView.Adapter<CommunityMemberAdapter.ViewHolder> {

    public interface OnKickClickListener {

        void onKickClick(
                CommunityMember member
        );
    }

    public interface OnFriendClickListener {

        void onFriendClick(
                CommunityMember member
        );
    }

    private final Context context;

    private final List<CommunityMember> memberList =
            new ArrayList<>();

    private final Long myMemberId;

    private final OnKickClickListener kickClickListener;

    private final OnFriendClickListener friendClickListener;

    private boolean isHost = false;

    public CommunityMemberAdapter(
            Context context,
            OnKickClickListener kickClickListener,
            OnFriendClickListener friendClickListener
    ) {

        this.context = context;

        this.kickClickListener =
                kickClickListener;

        this.friendClickListener =
                friendClickListener;

        SharedPreferences prefs =
                context.getSharedPreferences(
                        "auth",
                        Context.MODE_PRIVATE
                );

        long savedMemberId =
                prefs.getLong(
                        "memberId",
                        -1L
                );

        if (savedMemberId == -1L) {

            myMemberId = null;

        } else {

            myMemberId = savedMemberId;
        }
    }

    public void setMembers(
            List<CommunityMember> members
    ) {

        memberList.clear();

        isHost = false;

        if (members != null) {

            for (CommunityMember member : members) {

                boolean isMe =
                        myMemberId != null
                                && member.getMemberId() != null
                                && myMemberId.equals(
                                member.getMemberId()
                        );

                if (isMe
                        && "HOST".equals(
                        member.getRole()
                )) {

                    isHost = true;

                    break;
                }
            }

            Collections.sort(
                    members,
                    (a, b) -> {

                        if ("HOST".equals(a.getRole())
                                && !"HOST".equals(b.getRole())) {

                            return -1;
                        }

                        if (!"HOST".equals(a.getRole())
                                && "HOST".equals(b.getRole())) {

                            return 1;
                        }

                        boolean aIsMe =
                                myMemberId != null
                                        && a.getMemberId() != null
                                        && myMemberId.equals(
                                        a.getMemberId()
                                );

                        boolean bIsMe =
                                myMemberId != null
                                        && b.getMemberId() != null
                                        && myMemberId.equals(
                                        b.getMemberId()
                                );

                        if (aIsMe && !bIsMe) {

                            return -1;
                        }

                        if (!aIsMe && bIsMe) {

                            return 1;
                        }

                        return 0;
                    }
            );

            memberList.addAll(
                    members
            );
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(context)
                        .inflate(
                                R.layout.item_community_member,
                                parent,
                                false
                        );

        return new ViewHolder(
                view
        );
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        CommunityMember member =
                memberList.get(position);

        boolean isMe =
                myMemberId != null
                        && member.getMemberId() != null
                        && myMemberId.equals(
                        member.getMemberId()
                );

        holder.tvNickname.setText(
                member.getNickname()
        );

        if ("HOST".equals(member.getRole())) {

            if (isMe) {

                holder.tvRole.setText(
                        "모임장 · 나"
                );

            } else {

                holder.tvRole.setText(
                        "모임장"
                );
            }

        } else if (isMe) {

            holder.tvRole.setText(
                    "나"
            );

        } else {

            holder.tvRole.setText(
                    "참여자"
            );
        }

        if (isMe) {

            holder.btnChat.setVisibility(
                    View.INVISIBLE
            );

            holder.btnFriend.setVisibility(
                    View.INVISIBLE
            );

        } else {

            holder.btnChat.setVisibility(
                    View.VISIBLE
            );

            holder.btnFriend.setVisibility(
                    View.VISIBLE
            );
        }

        String friendStatus =
                member.getFriendStatus();

        if ("FRIEND".equals(friendStatus)
                || member.isFriend()) {

            holder.btnFriend.setText(
                    "친구 삭제"
            );

            holder.btnFriend.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.parseColor("#E53935")
                    )
            );

            holder.btnFriend.setTextColor(
                    Color.WHITE
            );

        } else if ("PENDING".equals(friendStatus)) {

            holder.btnFriend.setText(
                    "요청됨"
            );

            holder.btnFriend.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.parseColor("#FFF3E0")
                    )
            );

            holder.btnFriend.setTextColor(
                    Color.parseColor("#F59D31")
            );

        } else if ("RECEIVED".equals(friendStatus)) {

            holder.btnFriend.setText(
                    "요청 확인"
            );

            holder.btnFriend.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.parseColor("#FFF3E0")
                    )
            );

            holder.btnFriend.setTextColor(
                    Color.parseColor("#F59D31")
            );

        } else {

            holder.btnFriend.setText(
                    "친구 요청"
            );

            holder.btnFriend.setBackgroundTintList(
                    ColorStateList.valueOf(
                            Color.parseColor("#FFF3E0")
                    )
            );

            holder.btnFriend.setTextColor(
                    Color.parseColor("#F59D31")
            );
        }

        if (isHost
                && !"HOST".equals(member.getRole())
                && !isMe) {

            holder.btnKick.setVisibility(
                    View.VISIBLE
            );

        } else {

            holder.btnKick.setVisibility(
                    View.GONE
            );
        }

        String profileImageUrl =
                member.getProfileImageUrl();

        if (profileImageUrl != null
                && !profileImageUrl.isEmpty()) {

            String baseUrl =
                    RetrofitClient.BASE_URL
                            .replace("/api/", "/")
                            .replace("/api", "");

            if (baseUrl.endsWith("/")) {

                baseUrl =
                        baseUrl.substring(
                                0,
                                baseUrl.length() - 1
                        );
            }

            if (!profileImageUrl.startsWith("/")) {

                profileImageUrl =
                        "/" + profileImageUrl;
            }

            String fullImageUrl =
                    baseUrl + profileImageUrl;

            Glide.with(context)
                    .load(fullImageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .into(holder.ivProfile);

        } else {

            Glide.with(context)
                    .load(R.drawable.default_profile)
                    .into(holder.ivProfile);
        }

        holder.btnChat.setOnClickListener(v -> {

            /*
             * 현재 1:1 채팅은 아직 연결 전.
             * 이후 ChatController 연결 시 이 부분을 수정하면 됨.
             */
        });

        holder.btnFriend.setOnClickListener(v -> {

            if (friendClickListener != null) {

                friendClickListener.onFriendClick(
                        member
                );
            }
        });

        holder.btnKick.setOnClickListener(v -> {

            if (kickClickListener != null) {

                kickClickListener.onKickClick(
                        member
                );
            }
        });
    }

    @Override
    public int getItemCount() {

        return memberList.size();
    }

    public static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView ivProfile;

        TextView tvNickname;

        TextView tvRole;

        Button btnChat;

        Button btnFriend;

        Button btnKick;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            ivProfile =
                    itemView.findViewById(
                            R.id.ivProfile
                    );

            tvNickname =
                    itemView.findViewById(
                            R.id.tvNickname
                    );

            tvRole =
                    itemView.findViewById(
                            R.id.tvRole
                    );

            btnChat =
                    itemView.findViewById(
                            R.id.btnChat
                    );

            btnFriend =
                    itemView.findViewById(
                            R.id.btnFriend
                    );

            btnKick =
                    itemView.findViewById(
                            R.id.btnKick
                    );
        }
    }
}