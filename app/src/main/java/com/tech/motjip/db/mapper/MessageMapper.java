package com.tech.motjip.db.mapper;

import com.tech.motjip.Model.Message;
import com.tech.motjip.db.entity.MessageEntity;

import java.util.ArrayList;
import java.util.List;

public class MessageMapper {

    public static MessageEntity toEntity(
            Message message
    ) {

        if (message == null) {
            return null;
        }

        MessageEntity entity =
                new MessageEntity();

        if (message.getId() != null) {
            entity.setId(message.getId());
        }

        if (message.getRoomId() != null) {
            entity.setRoomId(message.getRoomId());
        }

        entity.setSenderId(message.getSenderId());
        entity.setSenderNickname(message.getSenderNickname());
        entity.setMessageContent(message.getMessageContent());
        entity.setSentAt(message.getSentAt());
        entity.setMessageType(message.getMessageType());
        entity.setFileUrl(message.getFileUrl());
        entity.setUnreadCount(message.getUnreadCount());

        return entity;
    }

    public static Message toModel(
            MessageEntity entity,
            Long currentUserId
    ) {

        if (entity == null) {
            return null;
        }

        int viewType;

        if ("SYSTEM".equalsIgnoreCase(entity.getMessageType())) {
            viewType = 2;
        } else if (entity.getSenderId() != null
                && entity.getSenderId().equals(currentUserId)) {
            viewType = 0;
        } else {
            viewType = 1;
        }

        Message message =
                new Message(
                        entity.getMessageContent(),
                        entity.getSenderId(),
                        entity.getSenderNickname(),
                        viewType
                );

        message.setId(entity.getId());
        message.setRoomId(entity.getRoomId());
        message.setSentAt(entity.getSentAt());
        message.setMessageType(entity.getMessageType());
        message.setFileUrl(entity.getFileUrl());
        message.setUnreadCount(entity.getUnreadCount());

        return message;
    }

    public static List<MessageEntity> toEntityList(
            List<Message> messages
    ) {

        List<MessageEntity> entities =
                new ArrayList<>();

        if (messages == null) {
            return entities;
        }

        for (Message message : messages) {

            MessageEntity entity =
                    toEntity(message);

            if (entity != null && entity.getId() > 0) {
                entities.add(entity);
            }
        }

        return entities;
    }

    public static List<Message> toModelList(
            List<MessageEntity> entities,
            Long currentUserId
    ) {

        List<Message> messages =
                new ArrayList<>();

        if (entities == null) {
            return messages;
        }

        for (MessageEntity entity : entities) {

            Message message =
                    toModel(
                            entity,
                            currentUserId
                    );

            if (message != null) {
                messages.add(message);
            }
        }

        return messages;
    }
}