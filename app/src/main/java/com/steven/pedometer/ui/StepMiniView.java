package com.steven.pedometer.ui;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.steven.pedometer.R;
import com.steven.pedometer.viewmodel.StepMiniViewVM;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Steven on 2017-10-02.
 */

public class StepMiniView extends Service {

    public static final String ACTION_FOREGROUND = "com.steven.pedometer.action.foreground";

    public static final String ACTION_BACKGROUND = "com.steven.pedometer.action.background";

    private StepMiniViewVM mStepMiniViewVM;

    public static boolean sIsAlive;

    private WindowManager mWindowManager;

    private View mMiniView;

    private TextView mStepTextView;

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    private final WindowManager.LayoutParams mLayoutParams = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT);

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mStepMiniViewVM = StepMiniViewVM.getInstance();
        mStepMiniViewVM.onCreate(this);
        sIsAlive = true;

        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        initMiniView();

        registerReceiver();
    }

    private void initMiniView() {
        //getting the widget layout from xml using layout inflater
        mMiniView = LayoutInflater.from(this).inflate(R.layout.step_mini_view, null);
        mMiniView.setVisibility(View.GONE);
        mStepTextView = mMiniView.findViewById(R.id.step_text);
        setDigitalFont();
        mStepTextView.setText(Long.toString(mStepMiniViewVM.getCurrentStep()));

        mCompositeDisposable
                .add(mStepMiniViewVM.getStepCount()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(step -> mStepTextView.setText(Long.toString(step))));

        //setting the layout parameters
        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        //getting windows services and adding the floating view to it
        mWindowManager = (WindowManager)getSystemService(WINDOW_SERVICE);
        mWindowManager.addView(mMiniView, params);
        mMiniView.setOnTouchListener(mOnTouchListener);
    }

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        private int initialX;
        private int initialY;
        private float initialTouchX;
        private float initialTouchY;

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = mLayoutParams.x;
                    initialY = mLayoutParams.y;
                    initialTouchX = event.getRawX();
                    initialTouchY = event.getRawY();
                    return true;

                case MotionEvent.ACTION_UP:
                    return true;

                case MotionEvent.ACTION_MOVE:
                    //this code is helping the widget to move around the screen with fingers
                    mLayoutParams.x = initialX + (int) (event.getRawX() - initialTouchX);
                    mLayoutParams.y = initialY + (int) (event.getRawY() - initialTouchY);
                    mWindowManager.updateViewLayout(mMiniView, mLayoutParams);
                    return true;
            }
            return false;
        }
    };

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        filter.addAction(ACTION_FOREGROUND);
        filter.addAction(ACTION_BACKGROUND);
        registerReceiver(mReceiver, filter);
    }

    private void setDigitalFont() {
        Typeface digitalFont = Typeface.createFromAsset(getAssets(),
                "fonts/digital-7.regular.ttf");
        mStepTextView.setTypeface(digitalFont);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mStepMiniViewVM.onDestroy();
        mStepMiniViewVM = null;
        sIsAlive = false;

        if (mWindowManager != null) {
            mWindowManager.removeView(mMiniView);
        }

        unregisterReceiver(mReceiver);

        if (mCompositeDisposable != null) {
            mCompositeDisposable.clear();
        }
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (ACTION_BACKGROUND.equals(intent.getAction())) {
                if (mMiniView != null) {
                    mMiniView.setVisibility(View.VISIBLE);
                }
            } else if (ACTION_FOREGROUND.equals(intent.getAction())) {
                if (mMiniView != null) {
                    mMiniView.setVisibility(View.GONE);
                }
            }
        }
    };
}
