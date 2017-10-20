package com.steven.pedometer.ui;

import android.support.v4.app.Fragment;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.steven.pedometer.R;
import com.steven.pedometer.databinding.FragmentRecordBinding;
import com.steven.pedometer.viewmodel.RecordFragmentVM;

import org.eazegraph.lib.models.BarModel;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;

import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by Steven on 2017-09-30.
 */

public class RecordFragment extends Fragment {

    private static final String TAG = "RecordFragment";

    private FragmentRecordBinding mRecordBinding;

    private RecordFragmentVM mRecordFragmentVM;

    private final CompositeDisposable mCompositeDisposable = new CompositeDisposable();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mRecordFragmentVM = ViewModelProviders.of(this).get(RecordFragmentVM.class);
        mRecordBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_record, container, false);
        mRecordBinding.setRecordFragment(this);

        return mRecordBinding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();

        mCompositeDisposable
                .add(mRecordFragmentVM.getStepRecord()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(record -> {
                            mRecordBinding.barchart.clearChart();

                            final int barColor = getBarColor();
                            for (int i = 0; i < record.size(); i++) {
                                BarModel barModel = new BarModel(record.get(i).getStep(), barColor);
                                barModel.setLegendLabel((record.get(i).getMonth() + 1) + "/" + record.get(i).getDay());
                                mRecordBinding.barchart.addBar(barModel);
                            }

                            mRecordBinding.barchart.startAnimation();
                        }));

        mRecordFragmentVM.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCompositeDisposable.clear();
    }

    private int getBarColor() {
        if (SDK_INT >= 23) {
            return getActivity().getColor(R.color.colorAccent);
        } else {
            return getActivity().getResources().getColor(R.color.colorAccent);
        }
    }

    public void onDateChanged() {
        mRecordFragmentVM.onDateChanged();
    }
}
