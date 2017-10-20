package com.steven.pedometer.ui;

import android.Manifest;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.steven.pedometer.R;
import com.steven.pedometer.databinding.FragmentStepBinding;
import com.steven.pedometer.viewmodel.StepFragmentVM;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static com.steven.pedometer.viewmodel.StepFragmentVM.PedometerState.ACTIVE;

/**
 * Created by Steven on 2017-09-30.
 */
public class StepFragment extends Fragment {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 2017;

    private static final String TAG = StepFragment.class.getSimpleName();

    private StepFragmentVM mStepFragmentVM;

    private FragmentStepBinding mStepBinding;

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mStepFragmentVM = ViewModelProviders.of(this).get(StepFragmentVM.class);
        mStepBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_step, container, false);
        mStepBinding.setStepFragment(this);
        return mStepBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        mCompositeDisposable
                .add(mStepFragmentVM.getPedometerState()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(state -> updateActivationButtonView(state)));

        mCompositeDisposable
                .add(mStepFragmentVM.getCurrentStep()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(stepCount -> mStepBinding.stepCount.setText(Long.toString(stepCount))));

        mCompositeDisposable
                .add(mStepFragmentVM.getCurrentDistance()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(distance -> mStepBinding.textDistanceValue.setText(Long.toString(distance))));

        mCompositeDisposable
                .add(mStepFragmentVM.getCurrentCalorie()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(calorie -> mStepBinding.textCalorieValue.setText(calorie)));

        mStepFragmentVM.onResume();
    }

    private void updateActivationButtonView(StepFragmentVM.PedometerState state) {
        if (state == ACTIVE) {
            mStepBinding.activationButton.setImageResource(R.drawable.ic_pause_black);
        } else {
            mStepBinding.activationButton.setImageResource(R.drawable.ic_play_arrow_black);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(StepMiniView.ACTION_FOREGROUND);
        getActivity().sendBroadcast(intent);
    }

    @Override
    public void onPause() {
        super.onPause();
        mCompositeDisposable.clear();
        mStepFragmentVM.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Intent intent = new Intent(StepMiniView.ACTION_BACKGROUND);
        getActivity().sendBroadcast(intent);
    }

    public void onActivationButtonClick(View v) {
        Log.d(TAG, "onStartButtonClick");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && mStepFragmentVM.getCurrentState().equals(StepFragmentVM.PedometerState.INACTIVE.state)) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            }
        }

        mStepFragmentVM.onActivationButtonClick();
    }

    public void onDateChanged() {
        mStepFragmentVM.onDateChanged();
    }
}
