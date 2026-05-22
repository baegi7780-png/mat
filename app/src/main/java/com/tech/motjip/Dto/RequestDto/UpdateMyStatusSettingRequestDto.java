package com.tech.motjip.Dto.RequestDto;

public class UpdateMyStatusSettingRequestDto {

    private Boolean rejectFriendRequest;
    private Boolean rejectChat;
    private Boolean rejectFriendRecommend;
    private Boolean rejectCommunityInvite;

    public UpdateMyStatusSettingRequestDto(
            Boolean rejectFriendRequest,
            Boolean rejectChat,
            Boolean rejectFriendRecommend,
            Boolean rejectCommunityInvite
    ) {
        this.rejectFriendRequest = rejectFriendRequest;
        this.rejectChat = rejectChat;
        this.rejectFriendRecommend = rejectFriendRecommend;
        this.rejectCommunityInvite = rejectCommunityInvite;
    }
}