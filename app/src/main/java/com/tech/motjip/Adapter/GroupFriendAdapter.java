package com.tech.motjip.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tech.motjip.API.RetrofitClient;
import com.tech.motjip.Dto.ResponseDto.FriendResponseDto;
import com.tech.motjip.R;

import java.util.List;

public class GroupFriendAdapter
        extends RecyclerView.Adapter<GroupFriendAdapter.ViewHolder> {

    private List<FriendResponseDto> friendList;

    public GroupFriendAdapter(
            List<FriendResponseDto> friendList
    ) {

        this.friendList = friendList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent,
            int viewType
    ) {

        View view =
                LayoutInflater.from(parent.getContext())
                        .inflate(
                                R.layout.item_group_friend,
                                parent,
                                false
                        );

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder,
            int position
    ) {

        FriendResponseDto friend =
                friendList.get(position);

        holder.tvName.setText(
                friend.getNickname()
        );

        holder.checkBox.setOnCheckedChangeListener(null);

        holder.checkBox.setChecked(
                friend.isSelected()
        );

        bindProfileImage(
                holder,
                friend
        );

        holder.itemView.setOnClickListener(v -> {

            boolean newSelected =
                    !friend.isSelected();

            friend.setSelected(
                    newSelected
            );

            holder.checkBox.setChecked(
                    newSelected
            );
        });

        holder.checkBox.setOnClickListener(v -> {

            boolean newSelected =
                    holder.checkBox.isChecked();

            friend.setSelected(
                    newSelected
            );
        });
    }

    private void bindProfileImage(
            @NonNull ViewHolder holder,
            @NonNull FriendResponseDto friend
    ) {

        String profileImgUrl =
                friend.getProfileImgUrl();

        if (profileImgUrl != null
                && !profileImgUrl.trim().isEmpty()) {

            String imageUrl =
                    buildImageUrl(
                            profileImgUrl
                    );

            Glide.with(holder.itemView.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .fallback(R.drawable.default_profile)
                    .circleCrop()
                    .into(holder.ivProfile);

        } else {

            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.default_profile)
                    .placeholder(R.drawable.default_profile)
                    .error(R.drawable.default_profile)
                    .circleCrop()
                    .into(holder.ivProfile);
        }
    }

    private String buildImageUrl(
            String imageUrl
    ) {

        if (imageUrl == null
                || imageUrl.trim().isEmpty()) {

            return null;
        }

        String trimmedUrl =
                imageUrl.trim();

        if (trimmedUrl.startsWith("http://")
                || trimmedUrl.startsWith("https://")) {

            return trimmedUrl;
        }

        return RetrofitClient.BASE_URL.replaceAll(
                "/$",
                ""
        )
                + "/"
                + trimmedUrl.replaceAll(
                "^/",
                ""
        );
    }

    @Override
    public int getItemCount() {

        if (friendList == null) {

            return 0;
        }

        return friendList.size();
    }

    static class ViewHolder
            extends RecyclerView.ViewHolder {

        ImageView ivProfile;

        TextView tvName;

        CheckBox checkBox;

        public ViewHolder(
                @NonNull View itemView
        ) {

            super(itemView);

            ivProfile =
                    itemView.findViewById(
                            R.id.iv_friend_profile
                    );

            tvName =
                    itemView.findViewById(
                            R.id.tv_friend_name
                    );

            checkBox =
                    itemView.findViewById(
                            R.id.cb_select
                    );
        }
    }
}