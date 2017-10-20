package com.steven.pedometer.data.disk.dao;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import com.steven.pedometer.data.disk.entity.Pedometer;

import io.reactivex.Single;


/**
 * Created by Steven on 2017-10-06.
 */

@Dao
public interface PedometerDao {

    @Query("SELECT * FROM pedometer WHERE year = :year AND month = :month AND day = :day")
    Single<Pedometer> getPedometerByDate(int year, int month, int day);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertPedometer(Pedometer pedometer);

    @Delete
    void deletePedometer(Pedometer pedometer);

    @Update
    void updatePedometer(Pedometer pedometer);

}
