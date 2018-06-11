package com.zoma.map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.google.android.gms.location.LocationResult;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by noura_000 on 2/12/2018.
 */

public class GetLastLocation extends TimerTask {

    boolean GPSEnabled = false;
    boolean NetworkEnabled = false;
    private LocationManager locationManager;
    private LocationResult locationResult;
    private Timer timer;

    @Override
    public void run() {
        locationManager.removeUpdates(locationListenerGps);
        locationManager.removeUpdates(locationListenerNetwork);

        Location net_loc=null, gps_loc=null;
        if(GPSEnabled)
            gps_loc=locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(NetworkEnabled)
            net_loc=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        //if there are both values use the latest one
        if(gps_loc!=null && net_loc!=null){
            if(gps_loc.getTime()>net_loc.getTime())
                locationResult.gotLocation(gps_loc);
            else
                locationResult.gotLocation(net_loc);
            return;
        }

        if(gps_loc!=null){
            locationResult.gotLocation(gps_loc);
            return;
        }
        if(net_loc!=null){
            locationResult.gotLocation(net_loc);
            return;
        }
        locationResult.gotLocation(null);
    }

    LocationListener locationListenerGps = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel();
            locationResult.gotLocation(location);
            // gps will keep going
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerNetwork);
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

    };

    LocationListener locationListenerNetwork = new LocationListener() {
        public void onLocationChanged(Location location) {
            timer.cancel();
            locationResult.gotLocation(location);
            locationManager.removeUpdates(this);
            locationManager.removeUpdates(locationListenerGps);
        }

        public void onProviderDisabled(String provider) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    };



    public static abstract class LocationResult{
        public abstract void gotLocation(Location location);
    }
}
