package com.tech.motjip.Dto.ResponseDto;

import com.google.gson.annotations.SerializedName;

public class ChatRoomMemberResponseDto {

    @SerializedName("userId")
    private Long userId;

    @SerializedName("nickname")
    private String nickname;

    @SerializedName("email")
    private String email;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    public Long getUserId() {
        return userId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getEmail() {
        return email;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }
}