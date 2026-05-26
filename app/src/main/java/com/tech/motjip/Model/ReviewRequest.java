package com.tech.motjip.Model;

import java.util.List;

public class ReviewRequest {

    /*
     * 장소 ID
     */
    private Long placeId;

    /*
     * 별점
     */
    private int rating;

    /*
     * 재방문 여부
     */
    private boolean revisit;

    /*
     * 선택 태그
     */
    private List<String> tags;

    /*
     * 후기 내용
     */
    private String content;

    public ReviewRequest() {}

    public Long getPlaceId() {
        return placeId;
    }

    public void setPlaceId(Long placeId) {
        this.placeId = placeId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isRevisit() {
        return revisit;
    }

    public void setRevisit(boolean revisit) {
        this.revisit = revisit;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}