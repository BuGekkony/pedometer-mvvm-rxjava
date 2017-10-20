package com.steven.pedometer.di.module;

import android.app.Application;
import android.content.Context;

import com.steven.pedometer.data.disk.IDiskModel;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Steven on 2017-10-03.
 */

@Module
public class AppModule {

    private Application mApplication;

    private IDiskModel mDiskModel;

    public AppModule(Application application, IDiskModel diskModel) {
        mApplication = application;
        mDiskModel = diskModel;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return mApplication;
    }

    @Provides
    public IDiskModel provideDiskModel() {
        return mDiskModel;
    }
}
