package com.tech.motjip.Dto.ResponseDto;

public class MyStatusSettingResponseDto {

    private Boolean rejectFriendRequest;
    private Boolean rejectChat;
    private Boolean rejectFriendRecommend;
    private Boolean rejectCommunityInvite;

    public Boolean getRejectFriendRequest() {
        return rejectFriendRequest;
    }

    public Boolean getRejectChat() {
        return rejectChat;
    }

    public Boolean getRejectFriendRecommend() {
        return rejectFriendRecommend;
    }

    public Boolean getRejectCommunityInvite() {
        return rejectCommunityInvite;
    }
}