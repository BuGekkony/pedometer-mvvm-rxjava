package com.steven.pedometer.data.disk;

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.persistence.room.Room;
import android.content.SharedPreferences;
import android.support.test.InstrumentationRegistry;

import com.steven.pedometer.data.disk.entity.Pedometer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import static org.junit.Assert.*;

/**
 * Created by Steven on 2017-10-14.
 */
public class DiskModelTest {

    @Mock
    SharedPreferences mSharedPreference;

    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private PedometerDB mDatabase;
    private DiskModel mDiskModel;

    @Before
    public void initDb() throws Exception {
        // using an in-memory database because the information stored here disappears when the
        // process is killed
        mDatabase = Room.inMemoryDatabaseBuilder(InstrumentationRegistry.getContext(),
                PedometerDB.class)
                // allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build();

        mDiskModel = new DiskModel(mDatabase.pedometerDao(), mSharedPreference);
    }

    @After
    public void closeDb() throws Exception {
        mDatabase.close();
    }

    @Test
    public void getPedometerRecord() throws Exception {
        Calendar today = Calendar.getInstance();
        final List<Pedometer> expectedResult = new ArrayList<>();

        for (int i = 5; i > 0; i--) {
            today.add(Calendar.DAY_OF_MONTH, -i);

            Pedometer pedometer = new Pedometer(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH), i*100, i*20);
            mDatabase.pedometerDao().insertPedometer(pedometer);
            expectedResult.add(pedometer);

            today.add(Calendar.DAY_OF_MONTH, i);
        }

        List<Pedometer> pedometerList = mDiskModel.getPedometerRecord()
                .toList()
                .blockingGet();

        assertEquals(pedometerList, expectedResult);
    }


    @Test
    public void insertTest() throws Exception {
        Calendar today = Calendar.getInstance();

        today.add(Calendar.DAY_OF_MONTH, -2);
        Pedometer pedometer = new Pedometer(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH),100, 20);

        mDatabase.pedometerDao().insertPedometer(pedometer);
        mDatabase.pedometerDao().getPedometerByDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH),
                today.get(Calendar.DAY_OF_MONTH)).test().assertValue(value -> value.getStep() == 100);
    }
}