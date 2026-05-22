package com.tech.motjip.Dto.ResponseDto;

public class FriendResponseDto {

    private Long memberId;

    private String nickname;

    private String email;

    private String profileImgUrl;

    // 추천 거리
    private Double distanceKm;

    // 친구 상태
    private String status;

    public FriendResponseDto() {
    }

    // 친구 목록용 생성자
    public FriendResponseDto(
            Long memberId,
            String nickname,
            String email,
            String profileImgUrl
    ) {

        this.memberId = memberId;
        this.nickname = nickname;
        this.email = email;
        this.profileImgUrl = profileImgUrl;
    }

    // 추천 목록용 생성자
    public FriendResponseDto(
            Long memberId,
            String nickname,
            String email,
            String profileImgUrl,
            Double distanceKm,
            String status
    ) {

        this.memberId = memberId;
        this.nickname = nickname;
        this.email = email;
        this.profileImgUrl = profileImgUrl;
        this.distanceKm = distanceKm;
        this.status = status;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(
            Long memberId
    ) {
        this.memberId = memberId;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(
            String nickname
    ) {
        this.nickname = nickname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(
            String email
    ) {
        this.email = email;
    }

    public String getProfileImgUrl() {
        return profileImgUrl;
    }

    public void setProfileImgUrl(
            String profileImgUrl
    ) {
        this.profileImgUrl = profileImgUrl;
    }

    public Double getDistanceKm() {
        return distanceKm;
    }

    public void setDistanceKm(
            Double distanceKm
    ) {
        this.distanceKm = distanceKm;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(
            String status
    ) {
        this.status = status;
    }
}