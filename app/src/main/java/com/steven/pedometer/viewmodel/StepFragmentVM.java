package com.steven.pedometer.viewmodel;

import android.Manifest;
import android.arch.lifecycle.ViewModel;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.steven.pedometer.PedometerApp;
import com.steven.pedometer.data.disk.IDiskModel;
import com.steven.pedometer.data.disk.entity.Pedometer;
import com.steven.pedometer.ui.StepMiniView;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.BehaviorSubject;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Steven on 2017-09-30.
 *
 * When active state, fetch data from StepMiniViewVM
 * When inactive state, fetch data from database
 *
 */
public class StepFragmentVM extends ViewModel {

    private static final String TAG = StepFragmentVM.class.getSimpleName();

    private static final Double AVERAGE_CALORIE_PER_STEP = 0.05;

    @Inject
    Context mContext;

    @Inject
    IDiskModel mDiskModel;

    public enum PedometerState {
        INACTIVE("inactive"), ACTIVE("active");

        public String state;

        PedometerState(String state) {
            this.state = state;
        }
    }

    private PedometerState mPedometerState = PedometerState.INACTIVE;

    private BehaviorSubject<PedometerState> mStateSubject = BehaviorSubject.create();

    // Emit step count to fragment.
    private PublishSubject<Long> mStepSubject = PublishSubject.create();

    // Observe step count from StepMiniViewVM
    private Observable<Long> mStepObservable;

    // To resource clear from StepMiniViewVM
    private Disposable mStepDisposable;

    // Emit distance to fragment.
    private PublishSubject<Long> mDistanceSubject = PublishSubject.create();

    // Observe distance from from StepMiniViewVM
    private Observable<Long> mDistanceObservable;

    // To resource clear from StepMiniViewVM
    private Disposable mDistanceDisposable;

    // Emit calorie to fragment
    private PublishSubject<String> mCalorieSubject = PublishSubject.create();

    public StepFragmentVM() {
        super();
        PedometerApp.getAppComponent().inject(this);
    }

    public void onActivationButtonClick() {
        mPedometerState = getOppositeState(mPedometerState);
        mDiskModel.saveState(mPedometerState.state);
        mStateSubject.onNext(mPedometerState);

        if (mPedometerState == PedometerState.ACTIVE) {
            mContext.startService(new Intent(mContext, StepMiniView.class));
            mStepObservable = observeStep();
            mStepDisposable = mStepObservable.subscribe(stepCount -> {
                mStepSubject.onNext(stepCount);
                mCalorieSubject.onNext(calculateCalorie(stepCount));
            });

            mDistanceObservable = observeDistance();
            mDistanceDisposable = mDistanceObservable.subscribe(distance -> mDistanceSubject.onNext(distance));
        } else {
            saveCurrentPedometer();
            mContext.stopService(new Intent(mContext, StepMiniView.class));

            if (mStepDisposable != null) {
                mStepDisposable.dispose();
            }

            if (mDistanceDisposable != null) {
                mDistanceDisposable.dispose();
            }
        }
    }

    private String calculateCalorie(Long stepCount) {
        double calorie = stepCount * AVERAGE_CALORIE_PER_STEP;
        calorie = Math.round(calorie);
        Log.d(TAG, "calculateCalorie = " + calorie + "kcal");
        return Long.toString((long)calorie);
    }

    private PedometerState getOppositeState(PedometerState currentState) {
        return (currentState == PedometerState.ACTIVE) ?PedometerState.INACTIVE : PedometerState.ACTIVE;
    }

    public BehaviorSubject<PedometerState> getPedometerState() {
        return mStateSubject;
    }

    public void onResume() {
        Log.d(TAG, "omResume");

        mPedometerState = mDiskModel.getState().equals(PedometerState.ACTIVE.state) ? PedometerState.ACTIVE : PedometerState.INACTIVE;
        mStateSubject.onNext(mPedometerState);

        if (mPedometerState == PedometerState.ACTIVE) {
            if (!StepMiniView.sIsAlive) {
                mContext.startService(new Intent(mContext, StepMiniView.class));
            } else {
                long currentStep = StepMiniViewVM.getInstance().getCurrentStep();
                mStepSubject.onNext(currentStep);
                mCalorieSubject.onNext(calculateCalorie(currentStep));
                long currentDistance = StepMiniViewVM.getInstance().getCurrentDistance();
                mDistanceSubject.onNext(currentDistance);
            }

            if (mStepObservable == null) {
                mStepObservable = observeStep();
            }

            mStepDisposable = mStepObservable.subscribe(stepCount -> {
                mStepSubject.onNext(stepCount);
                mCalorieSubject.onNext(calculateCalorie(stepCount));
            });

            if (mDistanceObservable == null) {
                mDistanceObservable = observeDistance();
            }

            mDistanceDisposable = mDistanceObservable.subscribe(distance -> mDistanceSubject.onNext(distance));
        } else {
            mDiskModel.getPedometerByCurrentDate()
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(
                            pedometer -> {
                                mStepSubject.onNext(pedometer.getStep());
                                mCalorieSubject.onNext(calculateCalorie(pedometer.getStep()));
                                mDistanceSubject.onNext(pedometer.getDistance());},
                            error -> {
                                Log.d(TAG, "empty result. save default pedometer.");
                                mDiskModel.insertPedometer(new Pedometer(0, 0));
                                mStepSubject.onNext(0l);
                                mCalorieSubject.onNext(calculateCalorie(0l));
                                mDistanceSubject.onNext(0l);
                            }
                    );
        }
    }

    public void onPause() {
        Log.d(TAG, "onPause");

        mDiskModel.saveState(mPedometerState.state);

        if (mPedometerState == PedometerState.ACTIVE) {
            saveCurrentPedometer();
        }

        if (mStepDisposable != null) {
            mStepDisposable.dispose();
        }

        if (mDistanceDisposable != null) {
            mDistanceDisposable.dispose();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public Observable<Long> observeStep() {
        return StepMiniViewVM.getInstance().getStepCount();
    }

    public Observable<Long> observeDistance() {
        return StepMiniViewVM.getInstance().getDistance();
    }

    public PublishSubject<Long> getCurrentStep() {
        return mStepSubject;
    }

    public PublishSubject<String> getCurrentCalorie() {
        return mCalorieSubject;
    }

    public PublishSubject<Long> getCurrentDistance() {
        return mDistanceSubject;
    }

    private void saveCurrentPedometer() {
        long currentStep = StepMiniViewVM.getInstance().getCurrentStep();
        long currentDistance = StepMiniViewVM.getInstance().getCurrentDistance();
        mDiskModel.insertOrUpdatePedometer(new Pedometer(currentStep, currentDistance));
    }

    public void onDateChanged() {
        if (mPedometerState == StepFragmentVM.PedometerState.INACTIVE) {
            mStepSubject.onNext(0l);
            mCalorieSubject.onNext("0");
            mDistanceSubject.onNext(0l);
            mDiskModel.insertOrUpdatePedometer(new Pedometer(0, 0));
        }
    }

    public String getCurrentState() {
        return mDiskModel.getState();
    }

}
