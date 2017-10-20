package com.steven.pedometer.data.disk;

import android.content.SharedPreferences;

import com.steven.pedometer.data.disk.dao.PedometerDao;
import com.steven.pedometer.data.disk.entity.Pedometer;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.schedulers.Schedulers;

import static com.steven.pedometer.viewmodel.StepFragmentVM.PedometerState.INACTIVE;

/**
 * Created by Steven on 2017-09-30.
 */

public class DiskModel implements IDiskModel {

    private final PedometerDao mPedometerDao;

    private final SharedPreferences mPreferences;

    public DiskModel(PedometerDao pedometerDao, SharedPreferences preferences) {
        mPedometerDao = pedometerDao;
        mPreferences = preferences;
    }

    @Override
    public Single<Pedometer> getPedometerByCurrentDate() {
        Calendar today = Calendar.getInstance();
        return mPedometerDao.getPedometerByDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH));
    }

    @Override
    public void insertPedometer(Pedometer pedometer) {
        mPedometerDao.insertPedometer(pedometer);
    }

    @Override
    public void insertOrUpdatePedometer(Pedometer pedometer) {
        new Thread(() -> mPedometerDao.updatePedometer(pedometer)).start();
    }

    @Override
    public Flowable<Pedometer> getPedometerRecord() {
        final int RECORD_SIZE = 5;
        Calendar today = Calendar.getInstance();
        List<Single<Pedometer>> pedometerList = new ArrayList<>();

        for (int i = RECORD_SIZE; i > 0; i--) {
            today.add(Calendar.DAY_OF_MONTH, -i);

            Single<Pedometer> pedometer = mPedometerDao.getPedometerByDate(today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
                    .onErrorResumeNext(Single.just(new Pedometer(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                            today.get(Calendar.DAY_OF_MONTH), 0l, 0l)));
            pedometerList.add(pedometer);

            today.add(Calendar.DAY_OF_MONTH, i);
        }

        return Single.concat(pedometerList).subscribeOn(Schedulers.io());
    }

    @Override
    public void deletePedometer(Pedometer pedometer) {
        mPedometerDao.deletePedometer(pedometer);
    }

    @Override
    public void updatePedometer(Pedometer pedometer) {
        mPedometerDao.updatePedometer(pedometer);
    }

    @Override
    public void saveState(String state) {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString("state", state);
        editor.commit();
    }

    @Override
    public String getState() {
        String state = mPreferences.getString("state", INACTIVE.state);
        return state;
    }
}
