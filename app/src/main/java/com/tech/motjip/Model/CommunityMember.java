package com.tech.motjip.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityMember {

    private Long memberId;

    private String nickname;

    private String profileImageUrl;

    private String role;

    private String status;

    // 🔥 기존 친구 여부
    private boolean friend;

    // 🔥 실서비스 친구 상태
    // NONE / PENDING / RECEIVED / FRIEND
    private String friendStatus;
}