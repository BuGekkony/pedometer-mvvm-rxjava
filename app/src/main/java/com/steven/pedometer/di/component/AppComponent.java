package com.steven.pedometer.di.component;

import com.steven.pedometer.di.module.AppModule;
import com.steven.pedometer.viewmodel.RecordFragmentVM;
import com.steven.pedometer.viewmodel.StepFragmentVM;
import com.steven.pedometer.viewmodel.StepMiniViewVM;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Steven on 2017-10-03.
 */

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {

    void inject(StepFragmentVM target);

    void inject(StepMiniViewVM target);

    void inject(RecordFragmentVM target);

}
