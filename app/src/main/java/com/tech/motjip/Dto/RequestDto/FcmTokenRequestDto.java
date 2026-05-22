package com.tech.motjip.Dto.RequestDto;

public class FcmTokenRequestDto {

    private String fcmToken;

    public FcmTokenRequestDto(
            String fcmToken
    ) {

        this.fcmToken = fcmToken;
    }

    public String getFcmToken() {

        return fcmToken;
    }

    public void setFcmToken(
            String fcmToken
    ) {

        this.fcmToken = fcmToken;
    }
}