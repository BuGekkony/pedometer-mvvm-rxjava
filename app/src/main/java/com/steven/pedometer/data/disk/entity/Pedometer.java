package com.steven.pedometer.data.disk.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;

import java.util.Calendar;

/**
 * Created by Steven on 2017-10-06.
 */

@Entity(tableName = "pedometer", primaryKeys = {"year", "month", "day"})
public final class Pedometer {

    @ColumnInfo(name = "year")
    private final int year;

    @ColumnInfo(name = "month")
    private final int month;

    @ColumnInfo(name = "day")
    private final int day;

    @ColumnInfo(name = "step")
    private final long step;

    @ColumnInfo(name = "distance")
    private final long distance;

    public Pedometer(int year, int month, int day, long step, long distance) {
        this.year = year;
        this.month = month;
        this.day = day;
        this.step = step;
        this.distance = distance;
    }

    @Ignore
    public Pedometer(long step, long distance) {
        Calendar today = Calendar.getInstance();

        year = today.get(Calendar.YEAR);
        month = today.get(Calendar.MONTH);
        day = today.get(Calendar.DAY_OF_MONTH);
        this.step = step;
        this.distance = distance;
    }

    public int getYear() {
        return year;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public long getStep() {
        return step;
    }

    public long getDistance() {
        return distance;
    }

    @Override
    public String toString() {
        return new String("year = " + year + "\nmonth = " + month + "\nday = " + day +
                "\nstep = " + step + "\ndistance = " + distance);
    }

    @Override
    public boolean equals(Object obj) {
        Pedometer p = (Pedometer)obj;
        return this.year == p.getYear() && this.month == p.getMonth() && this.day == p.getDay()
                && this.step == p.getStep() && this.distance == p.getDistance();
    }
}
