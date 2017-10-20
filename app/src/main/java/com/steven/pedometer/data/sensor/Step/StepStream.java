package com.steven.pedometer.data.sensor.Step;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

import static android.content.Context.SENSOR_SERVICE;

public class StepStream implements SensorEventListener {
    private Context mContext;

    private static StepStream sInstance;

    private SensorManager mSensorManager;

    private Sensor mSensor;

    private boolean mIsListening;

    private static final PublishSubject<SensorEvent> mStepPublishSubject = PublishSubject.create();

    private StepStream(Context context) {
        mContext = context;
        mSensorManager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public synchronized static StepStream getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new StepStream(context);
        }

        return sInstance;
    }

    public void startListening() {
        if (!mIsListening) {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
            mIsListening = true;
        }
    }

    public void stopListening() {
        if (mIsListening) {
            mSensorManager.unregisterListener(this);
            mIsListening = false;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        mStepPublishSubject.onNext(event);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public Observable<SensorEvent> getSensorEvent() {
        return mStepPublishSubject;
    }
}
