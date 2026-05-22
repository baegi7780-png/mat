package com.tech.motjip.Model;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CommunityPost implements Serializable {

    private Long comId;

    private String tag;

    private String title;

    private String content;

    private String region;

    private String placeName;

    private String meetingAt;

    private String imageUrl;

    private String writerNickname;

    private String createdAt;

    private boolean favorite;

    private boolean mine;

    private boolean joined;

    private boolean closed;

    private int memberCount;
}