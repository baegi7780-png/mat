package com.tech.motjip.Model;

import com.google.gson.annotations.SerializedName;

public class Review {

    @SerializedName("reviewId")
    private Long reviewId;

    @SerializedName("placeId")
    private Long placeId;

    @SerializedName("memberId")
    private Long memberId;

    // =========================
    // 작성자 닉네임
    // 서버 ReviewResponseDto의 nickname과 매핑
    // =========================
    @SerializedName("nickname")
    private String nickname;

    // =========================
    // 작성자 프로필 이미지
    // 서버 ReviewResponseDto의 profileImageUrl과 매핑
    // =========================
    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    // =========================
    // 내가 작성한 후기 여부
    // 서버 ReviewResponseDto의 mine과 매핑
    // =========================
    @SerializedName("mine")
    private boolean mine;

    @SerializedName("rating")
    private int rating;

    @SerializedName("revisit")
    private boolean revisit;

    @SerializedName("content")
    private String content;

    @SerializedName("tags")
    private String tags;

    @SerializedName("imageUrl")
    private String imageUrl;

    // =========================
    // 작성일
    // 서버에서 한국식 문자열로 내려줌
    // 예: 2026년 05월 26일 16시 30분
    // =========================
    @SerializedName("createdAt")
    private String createdAt;

    public Long getReviewId() {
        return reviewId;
    }

    public Long getPlaceId() {
        return placeId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getNickname() {
        return nickname;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public boolean isMine() {
        return mine;
    }

    public int getRating() {
        return rating;
    }

    public boolean isRevisit() {
        return revisit;
    }

    public String getContent() {
        return content;
    }

    public String getTags() {
        return tags;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}