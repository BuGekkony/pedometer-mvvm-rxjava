package com.steven.pedometer.viewmodel;

import android.app.Notification;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.location.Location;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.steven.pedometer.PedometerApp;
import com.steven.pedometer.data.disk.IDiskModel;
import com.steven.pedometer.data.disk.entity.Pedometer;
import com.steven.pedometer.data.sensor.Location.LocationStream;
import com.steven.pedometer.data.sensor.Step.StepDetector;
import com.steven.pedometer.data.sensor.Step.StepStream;
import com.steven.pedometer.NotificationChannel;

import java.util.Calendar;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

/**
 * Created by Steven on 2017-10-02.
 */

public class StepMiniViewVM {

    private static final StepDetector mStepDetector = new StepDetector();

    private static final int NOTI_ID = 20171002;
    private static final String TAG = StepMiniViewVM.class.getSimpleName();

    @Inject
    Context mContext;

    @Inject
    IDiskModel mDiskModel;

    private long mCurrentStep;

    private long mLastStepSavedInDB;

    private long mCurrentDistance;

    private long mLastDistSavedInDB;

    private Location mCurrentLocation;

    private PublishSubject<Long> mStepSubject = PublishSubject.create();

    private PublishSubject<Long> mDistanceSubject = PublishSubject.create();

    private StepMiniViewVM() {
        PedometerApp.getAppComponent().inject(this);
    }

    private static StepMiniViewVM sInstance;

    public synchronized static StepMiniViewVM getInstance() {
        if (sInstance == null) {
            sInstance = new StepMiniViewVM();
        }

        return sInstance;
    }

    private void observeStep() {
         StepStream.getInstance(mContext).getSensorEvent()
                .filter(sensorEvent -> sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                .filter(sensorEvent -> mStepDetector.updateAccel(sensorEvent.timestamp, sensorEvent.values[0], sensorEvent.values[1], sensorEvent.values[2]))
                .flatMap(sensorEvent -> Observable.just(new Long(++mCurrentStep)))
                .subscribeOn(Schedulers.computation())
                .subscribe(step -> {
                    Log.d(TAG, "current step = " + mCurrentStep);
                    mStepSubject.onNext(mCurrentStep);

                    if (mCurrentStep >= mLastStepSavedInDB + 100) {
                        mLastStepSavedInDB = mCurrentStep;
                        mLastDistSavedInDB = mCurrentDistance;

                        mDiskModel.insertOrUpdatePedometer(new Pedometer(mLastStepSavedInDB, mLastDistSavedInDB));
                    }
                }, error -> Log.d(TAG, error.toString()));
    }

    private void observeLocation() {
        LocationStream.getInstance(mContext).observeLocation()
                .subscribeOn(Schedulers.computation())
                .subscribe(location -> {
                    Log.d(TAG, "Emitted location : " + location.toString());

                    if(mCurrentLocation == null) {
                        mCurrentLocation = location;
                    } else {
                        float movedDistance = mCurrentLocation.distanceTo(location);

                        Log.d(TAG, "moved distance = " + movedDistance);

                        mCurrentDistance += Math.round(movedDistance);
                        mDistanceSubject.onNext(mCurrentDistance);
                        mCurrentLocation = location;
                    }

                    //In case process killed, save step count to db.
                    if (mCurrentDistance >= mLastDistSavedInDB + 30) {
                        mLastDistSavedInDB = mCurrentDistance;
                        mLastStepSavedInDB = mCurrentStep;
                        mDiskModel.insertOrUpdatePedometer(new Pedometer(mLastStepSavedInDB, mLastDistSavedInDB));
                    }
                }, error -> Log.d(TAG, error.toString()));
    }

    public Observable<Long> getStepCount() {
        return mStepSubject;
    }

    public Observable<Long> getDistance() {
        return mDistanceSubject;
    }

    public Notification getForegroundNoti() {
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            NotificationChannel.createNotificationChannelIfNeeded(mContext);
            return new NotificationCompat.Builder(mContext, NotificationChannel.GROUP_NAME)
                    .build();
        } else if (android.os.Build.VERSION.SDK_INT >= 16) {
            return new Notification.Builder(mContext)
                    .build();
        } else {
            return new Notification();
        }
    }

    public void onCreate(Service service) {
        StepStream.getInstance(mContext).startListening();
        LocationStream.getInstance(mContext).startLocationStream();

        service.startForeground(NOTI_ID, getForegroundNoti());

        mDiskModel.getPedometerByCurrentDate()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                        pedometer -> {
                                mCurrentStep = pedometer.getStep();
                                mLastStepSavedInDB = mCurrentStep;
                                mCurrentDistance = pedometer.getDistance();
                                mLastDistSavedInDB = mCurrentDistance;

                                mStepSubject.onNext(mCurrentStep);
                                mDistanceSubject.onNext(mCurrentDistance);

                                Log.d(TAG, "onCreate. fetched from DB.\nCurrent step = " + mCurrentStep +
                                    "\nCurrent distance = " + mCurrentDistance);
                            },
                        error -> {
                                Log.d(TAG, "Empty result. save new pedometer.");
                                mDiskModel.insertPedometer(new Pedometer(0, 0));
                                mCurrentStep = 0;
                                mLastStepSavedInDB = mCurrentStep;
                                mCurrentDistance = 0;
                                mLastDistSavedInDB = mCurrentDistance;
                        });

        observeStep();
        observeLocation();

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        mContext.registerReceiver(mDateChangeReceiver, filter);

    }

    public void onDestroy() {
        StepStream.getInstance(mContext).stopListening();
        LocationStream.getInstance(mContext).stopLocationStream();
        mContext.unregisterReceiver(mDateChangeReceiver);
    }

    public long getCurrentStep() {
        Log.d(TAG, "getCurrentStep = " + mCurrentStep);
        return mCurrentStep;
    }

    public long getCurrentDistance() {
        return mCurrentDistance;
    }

    private BroadcastReceiver mDateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive. action = " + intent.getAction());
            if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
                Calendar yesterday = Calendar.getInstance();
                yesterday.add(Calendar.DAY_OF_MONTH, -1);

                mDiskModel.insertOrUpdatePedometer(new Pedometer(yesterday.get(Calendar.YEAR), yesterday.get(Calendar.MONTH)
                    , yesterday.get(Calendar.DAY_OF_MONTH), mCurrentStep, mCurrentDistance));

                mCurrentDistance = 0;
                mLastDistSavedInDB = 0;
                mCurrentStep = 0;
                mLastStepSavedInDB = 0;

                Calendar today = Calendar.getInstance();
                mDiskModel.insertOrUpdatePedometer(new Pedometer(today.get(Calendar.YEAR),
                        today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH), 0, 0));

                mStepSubject.onNext(0l);
                mDistanceSubject.onNext(0l);
            }
        }
    };
}
