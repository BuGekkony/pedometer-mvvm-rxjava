package com.steven.pedometer.data.sensor.Location;

/**
 * Created by Steven on 2017-10-07.
 */

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.steven.pedometer.R;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

/**
 * emmit location every 3sec
 */
public class LocationStream {

    private static final String TAG = "LocationStream";

    private LocationManager mLocationManager;

    private Context mContext;

    private static LocationStream sInstance;

    private PublishSubject<Location> mLocationSubject = PublishSubject.create();

    private boolean mIsActivated;

    private LocationStream(Context context) {
        mContext = context;
        mLocationManager = (LocationManager)mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    public synchronized static LocationStream getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new LocationStream(context);
        }

        return sInstance;
    }

    public void startLocationStream() {
        if (!mIsActivated) {
            try {
                //use gps provider for brevity.
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        1000,
                        1,
                        mLocationListener);

                mIsActivated = true;
                Log.d(TAG, "startLocationStream. request location updates");
            } catch (SecurityException e) {
                Log.e(TAG, "no location permission." + e.toString());
            }
        }
    }

    private LocationListener mLocationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            mLocationSubject.onNext(location);

            Log.d(TAG, "onLocationChanged. ");
            Log.d(TAG, "provider = " + location.getProvider() + "\nlong = " + location.getLongitude() +
                "\nlat = " + location.getLatitude());
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {
            Log.d(TAG, "onStatusChanged. ");
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.d(TAG, "onProviderEnabled. provider = " + provider);
            Toast.makeText(mContext, mContext.getString(R.string.gps_enabled), Toast.LENGTH_LONG).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.d(TAG, "onProviderDisabled. provider = " + provider);
            Toast.makeText(mContext, mContext.getString(R.string.gps_disabled), Toast.LENGTH_LONG).show();
        }
    };

    public void stopLocationStream() {
        if (mIsActivated) {
            mLocationManager.removeUpdates(mLocationListener);
            Log.d(TAG, "stopLocationStream. removeUpdates");
            mIsActivated = false;
        }
    }

    public Observable<Location> observeLocation() {
        return mLocationSubject;
    }
}
