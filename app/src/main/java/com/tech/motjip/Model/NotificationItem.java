package com.tech.motjip.Model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationItem {

    private Long notificationId;

    private String senderNickname;

    private String type;

    private Long targetId;

    private String message;

    private String status;

    private boolean isRead;

    private boolean isSelected;

    private String createdAt;
}