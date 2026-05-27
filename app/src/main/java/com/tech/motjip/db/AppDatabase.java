package com.tech.motjip.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.tech.motjip.db.dao.MessageDao;
import com.tech.motjip.db.entity.MessageEntity;

@Database(
        entities = {
                MessageEntity.class
        },
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase
        extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract MessageDao messageDao();

    public static AppDatabase getInstance(
            Context context
    ) {

        if (instance == null) {

            synchronized (AppDatabase.class) {

                if (instance == null) {

                    instance =
                            Room.databaseBuilder(
                                            context.getApplicationContext(),
                                            AppDatabase.class,
                                            "motjip_database"
                                    )
                                    .fallbackToDestructiveMigration()
                                    .build();
                }
            }
        }

        return instance;
    }
}