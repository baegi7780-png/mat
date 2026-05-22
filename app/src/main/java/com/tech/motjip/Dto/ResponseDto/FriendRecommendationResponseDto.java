package com.tech.motjip.Dto.ResponseDto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FriendRecommendationResponseDto {

    private Long memberId;

    private String nickname;

    private String profileImgUrl;

    private double distanceKm;

    private String status;
}