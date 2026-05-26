package com.tech.motjip.Model;

import com.google.gson.annotations.SerializedName;

public class RecommendedPlace {

    @SerializedName("placeId")
    private Long placeId;

    @SerializedName("placeName")
    private String placeName;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("averageRating")
    private Double averageRating;

    @SerializedName("profileImageUrl")
    private String profileImageUrl;

    public Long getPlaceId() {
        return placeId;
    }

    public String getPlaceName() {
        return placeName;
    }

    public Double getLatitude() {
        return latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public Double getAverageRating() {
        return averageRating;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public String getFormattedAverageRating() {
        if (averageRating == null) {
            return "";
        }

        if (averageRating % 1 == 0) {
            return String.valueOf(
                    averageRating.intValue()
            );
        }

        return String.format(
                "%.1f",
                averageRating
        );
    }
}