package com.steven.pedometer.data.disk;

import com.steven.pedometer.data.disk.entity.Pedometer;

import io.reactivex.Flowable;
import io.reactivex.Single;

/**
 * Created by Steven on 2017-10-06.
 */

/**
 * Access point for local persistence data.
 */
public interface IDiskModel {

    Single<Pedometer> getPedometerByCurrentDate();

    Flowable<Pedometer> getPedometerRecord();

    void insertPedometer(Pedometer pedometer);

    void insertOrUpdatePedometer(Pedometer pedometer);

    void deletePedometer(Pedometer pedometer);

    void updatePedometer(Pedometer pedometer);

    void saveState(String state);

    String getState();
}
