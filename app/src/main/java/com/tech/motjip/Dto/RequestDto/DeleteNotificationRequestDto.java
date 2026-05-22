package com.tech.motjip.Dto.RequestDto;

import java.util.List;

public class DeleteNotificationRequestDto {

    private List<Long> notificationIds;

    public DeleteNotificationRequestDto(
            List<Long> notificationIds
    ) {
        this.notificationIds = notificationIds;
    }

    public List<Long> getNotificationIds() {
        return notificationIds;
    }

    public void setNotificationIds(
            List<Long> notificationIds
    ) {
        this.notificationIds = notificationIds;
    }
}