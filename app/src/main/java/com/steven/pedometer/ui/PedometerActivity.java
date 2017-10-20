package com.steven.pedometer.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.steven.pedometer.R;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Steven on 2017-09-30.
 */

public class PedometerActivity extends AppCompatActivity {

    private static final int SYSTEM_ALERT_WINDOW_PERMISSION = 2000;

    private StepFragment mStepFragment;

    private RecordFragment mRecordFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pedometer);
        addStepFragment();
        addRecordFragment();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, SYSTEM_ALERT_WINDOW_PERMISSION);
        }

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(mDateChangeReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case SYSTEM_ALERT_WINDOW_PERMISSION :

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(this)) {
                        finish();
                    }
                }

                break;
        }
    }

    private void addStepFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment stepFragment = fragmentManager.findFragmentById(R.id.step_container);

        if (stepFragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            stepFragment = new StepFragment();
            fragmentTransaction.add(R.id.step_container, stepFragment);
            fragmentTransaction.commit();
        }

        mStepFragment = (StepFragment)stepFragment;
    }

    private void addRecordFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment recordFragment = fragmentManager.findFragmentById(R.id.record_container);

        if (recordFragment == null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            recordFragment = new RecordFragment();
            fragmentTransaction.add(R.id.record_container, recordFragment);
            fragmentTransaction.commit();
        }

        mRecordFragment = (RecordFragment)recordFragment;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    private BroadcastReceiver mDateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
                mStepFragment.onDateChanged();
                mRecordFragment.onDateChanged();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mDateChangeReceiver);
    }
}
