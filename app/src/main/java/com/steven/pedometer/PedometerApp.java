package com.steven.pedometer;

import android.app.Application;
import android.content.SharedPreferences;

import com.steven.pedometer.data.disk.DiskModel;
import com.steven.pedometer.data.disk.IDiskModel;
import com.steven.pedometer.data.disk.PedometerDB;
import com.steven.pedometer.di.component.AppComponent;
import com.steven.pedometer.di.component.DaggerAppComponent;
import com.steven.pedometer.di.module.AppModule;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Created by Steven on 2017-09-30.
 */

public class PedometerApp extends Application {

    private static final String PREF_NAME = "Pedometer_preference";

    private static AppComponent sAppComponent;

    @Override
    public void onCreate() {
        super.onCreate();
        sAppComponent = initDagger(this);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                        .setDefaultFontPath("fonts/digital-7.regular.ttf")
                        .setFontAttrId(R.attr.fontPath)
                        .build());
    }

    public static AppComponent getAppComponent() {
        return sAppComponent;
    }

    protected AppComponent initDagger(PedometerApp application) {

        SharedPreferences preferences = application.getSharedPreferences(PREF_NAME, MODE_PRIVATE);

        PedometerDB database = PedometerDB.getInstance(application);
        IDiskModel diskModel = new DiskModel(database.pedometerDao(), preferences);

        return DaggerAppComponent.builder()
                .appModule(new AppModule(application, diskModel))
                .build();
    }
}
