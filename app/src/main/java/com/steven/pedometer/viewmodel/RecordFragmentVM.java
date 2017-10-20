package com.steven.pedometer.viewmodel;

import android.arch.lifecycle.ViewModel;
import android.util.Log;

import com.steven.pedometer.PedometerApp;
import com.steven.pedometer.data.disk.IDiskModel;
import com.steven.pedometer.data.disk.entity.Pedometer;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.subjects.PublishSubject;

/**
 * Created by Steven on 2017-10-09.
 */

public class RecordFragmentVM extends ViewModel {

    private static final String TAG = RecordFragmentVM.class.getSimpleName();

    private PublishSubject<List<Pedometer>> mStepRecordSubject = PublishSubject.create();

    @Inject
    IDiskModel mDiskModel;

    public RecordFragmentVM() {
        PedometerApp.getAppComponent().inject(this);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public PublishSubject<List<Pedometer>> getStepRecord() {
        return mStepRecordSubject;
    }

    public void onResume() {
        fetchStepRecord();
    }

    private void fetchStepRecord() {
        List<Pedometer> record = new ArrayList<>();

        mDiskModel.getPedometerRecord().subscribe(
                pedometer -> {
                    record.add(pedometer);
                    mStepRecordSubject.onNext(record);
                    Log.d(TAG, "emitted pedometer = " + pedometer.toString());
                });
    }

    public void onDateChanged() {
        fetchStepRecord();
    }
}
