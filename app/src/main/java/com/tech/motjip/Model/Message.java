package com.tech.motjip.Model;

import com.google.gson.annotations.SerializedName;

public class Message {

    public static final String TYPE_TEXT =
            "TEXT";

    public static final String TYPE_IMAGE =
            "IMAGE";
    public static final String TYPE_VIDEO =
            "VIDEO";
    public static final String TYPE_SYSTEM =
            "SYSTEM";



    public static final String TYPE_DATE_HEADER =
            "DATE_HEADER";

    @SerializedName("id")
    private Long id;

    @SerializedName("messageContent")
    private String messageContent;

    @SerializedName("senderId")
    private Long senderId;

    @SerializedName("senderNickname")
    private String senderNickname;

    @SerializedName("senderProfileImage")
    private String senderProfileImage;

    @SerializedName("roomId")
    private Long roomId;

    @SerializedName("messageType")
    private String messageType;

    @SerializedName("fileUrl")
    private String fileUrl;

    @SerializedName("sentAt")
    private String sentAt;

    @SerializedName("unreadCount")
    private int unreadCount;

    private int viewType;

    private String dateHeaderText;

    private String dateHeaderKey;

    public Message() {
    }

    public Message(
            String content,
            Long senderId,
            String senderNickname,
            int viewType
    ) {

        this.messageContent = content;
        this.senderId = senderId;
        this.senderNickname = senderNickname;
        this.viewType = viewType;
    }

    public static Message createDateHeader(
            String dateHeaderKey,
            String dateHeaderText
    ) {

        Message message =
                new Message();

        message.setMessageType(
                TYPE_DATE_HEADER
        );

        message.setDateHeaderKey(
                dateHeaderKey
        );

        message.setDateHeaderText(
                dateHeaderText
        );

        message.setMessageContent(
                dateHeaderText
        );

        return message;
    }

    public boolean isDateHeader() {

        return TYPE_DATE_HEADER.equalsIgnoreCase(
                messageType
        );
    }

    public boolean isSystemMessage() {

        return TYPE_SYSTEM.equalsIgnoreCase(
                messageType
        );
    }

    public Long getId() {
        return id;
    }

    public String getContent() {
        return messageContent;
    }

    public String getMessageContent() {
        return messageContent;
    }

    public Long getSenderId() {
        return senderId;
    }

    public String getSenderNickname() {
        return senderNickname;
    }

    public String getSenderProfileImage() {
        return senderProfileImage;
    }

    public Long getRoomId() {
        return roomId;
    }

    public String getMessageType() {
        return messageType;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getSentAt() {
        return sentAt;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public int getViewType() {
        return viewType;
    }

    public String getDateHeaderText() {
        return dateHeaderText;
    }

    public String getDateHeaderKey() {
        return dateHeaderKey;
    }

    public void setId(
            Long id
    ) {

        this.id = id;
    }

    public void setContent(
            String content
    ) {

        this.messageContent = content;
    }

    public void setMessageContent(
            String messageContent
    ) {

        this.messageContent = messageContent;
    }

    public void setSenderId(
            Long senderId
    ) {

        this.senderId = senderId;
    }

    public void setSenderNickname(
            String senderNickname
    ) {

        this.senderNickname = senderNickname;
    }

    public void setSenderProfileImage(
            String senderProfileImage
    ) {

        this.senderProfileImage = senderProfileImage;
    }

    public void setRoomId(
            Long roomId
    ) {

        this.roomId = roomId;
    }

    public void setMessageType(
            String messageType
    ) {

        this.messageType = messageType;
    }

    public void setFileUrl(
            String fileUrl
    ) {

        this.fileUrl = fileUrl;
    }

    public void setSentAt(
            String sentAt
    ) {

        this.sentAt = sentAt;
    }

    public void setUnreadCount(
            int unreadCount
    ) {

        this.unreadCount = unreadCount;
    }

    public void setViewType(
            int viewType
    ) {

        this.viewType = viewType;
    }

    public void setDateHeaderText(
            String dateHeaderText
    ) {

        this.dateHeaderText = dateHeaderText;
    }

    public void setDateHeaderKey(
            String dateHeaderKey
    ) {

        this.dateHeaderKey = dateHeaderKey;
    }





}