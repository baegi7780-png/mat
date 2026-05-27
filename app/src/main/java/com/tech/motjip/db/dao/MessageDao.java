package com.tech.motjip.db.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.tech.motjip.db.entity.MessageEntity;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessages(
            List<MessageEntity> messages
    );

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(
            MessageEntity message
    );

    @Query("SELECT * FROM messages WHERE roomId = :roomId ORDER BY sentAt ASC")
    List<MessageEntity> getMessagesByRoomId(
            long roomId
    );

    @Query("DELETE FROM messages WHERE roomId = :roomId")
    void deleteMessagesByRoomId(
            long roomId
    );

    @Query("DELETE FROM messages")
    void deleteAllMessages();
}