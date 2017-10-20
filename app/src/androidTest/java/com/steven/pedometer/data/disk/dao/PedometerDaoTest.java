package com.steven.pedometer.data.disk.dao;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.steven.pedometer.data.disk.PedometerDB;
import com.steven.pedometer.data.disk.entity.Pedometer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by Steven on 2017-10-11.
 */
@RunWith(AndroidJUnit4.class)
public class PedometerDaoTest {

    private PedometerDB mDatabase;

    @Before
    public void initDb() throws Exception {
        mDatabase = Room.inMemoryDatabaseBuilder(
                InstrumentationRegistry.getContext(),
                PedometerDB.class)
                // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build();
    }

    @After
    public void closeDb() throws Exception {
        mDatabase.close();
    }

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    @Test
    public void getPedometerByDate() throws Exception {
        mDatabase.pedometerDao().insertPedometer(new Pedometer(2000, 1, 1, 100, 100));
        mDatabase.pedometerDao().getPedometerByDate(2000, 1, 1)
                .test()
                .assertValue(pedometer -> pedometer.getStep() == 100 && pedometer.getDistance() == 100);
    }

    @Test
    public void getPedometerByDateNonRx() throws Exception {
    }

    @Test
    public void insertPedometer() throws Exception {
        mDatabase.pedometerDao().insertPedometer(new Pedometer(2000, 1, 1, 100, 100));
        mDatabase.pedometerDao().insertPedometer(new Pedometer(2000, 1, 1, 300, 300));
        mDatabase.pedometerDao().getPedometerByDate(2000, 1, 1)
                .test()
                .assertValue(pedometer -> pedometer.getStep() == 300);
    }

    @Test
    public void deletePedometer() throws Exception {
        final Pedometer pedometer = new Pedometer(2000, 1, 1, 100, 100);
        mDatabase.pedometerDao().insertPedometer(pedometer);
        mDatabase.pedometerDao().deletePedometer(pedometer);
        mDatabase.pedometerDao().getPedometerByDate(pedometer.getYear(), pedometer.getMonth(), pedometer.getDay())
                .test()
                .assertNoValues();
    }

    @Test
    public void updatePedometer() throws Exception {
        mDatabase.pedometerDao().insertPedometer(new Pedometer(2000, 1, 1, 100, 100));
        mDatabase.pedometerDao().updatePedometer(new Pedometer(2000, 1, 1, 300, 300));
        mDatabase.pedometerDao().getPedometerByDate(2000, 1, 1)
                .test()
                .assertValue(pedometer -> pedometer.getStep() == 300 && pedometer.getDistance() == 300);
    }

}