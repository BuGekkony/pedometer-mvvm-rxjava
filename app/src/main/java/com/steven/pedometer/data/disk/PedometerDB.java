package com.steven.pedometer.data.disk;

import android.arch.persistence.db.SupportSQLiteDatabase;
import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.migration.Migration;
import android.content.Context;

import com.steven.pedometer.data.disk.dao.PedometerDao;
import com.steven.pedometer.data.disk.entity.Pedometer;

/**
 * Created by Steven on 2017-10-06.
 */

@Database(entities = {Pedometer.class}, version = 2)
public abstract class PedometerDB extends RoomDatabase {

    private static PedometerDB sInstance;

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {

        }
    };

    public static synchronized PedometerDB getInstance(Context context) {
        if (sInstance == null) {
            sInstance = Room.databaseBuilder(context.getApplicationContext(),
                    PedometerDB.class, "pedometer")
                    .addMigrations(MIGRATION_1_2)
                    .build();
        }

        return sInstance;
    }

    public abstract PedometerDao pedometerDao();
}
