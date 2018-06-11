package com.zoma.map;

import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by noura_000 on 5/10/2018.
 */

public class GetDirectionsData extends AsyncTask<Object,String,String> {
    String googleDirectionsData;
    GoogleMap mMap;
    String url;
    String duration,distance;
    LatLng latlng;

    @Override
    protected String doInBackground(Object... objects) {
        Log.d("GetNearbyPlacesData", "doInBackground entered");//
        mMap = (GoogleMap) objects[0];
        url = (String) objects[1];
        DownloadURL durl = new DownloadURL();
        try {
            googleDirectionsData = durl.readURL(url);
            Log.d("GooglePlacesReadTask", "doInBackground Exit");//
        } catch (MalformedURLException e) {
            Log.d("GooglePlacesReadTask", e.toString());//
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return googleDirectionsData;
    }

    @Override
    protected void onPostExecute(String s) {
        Log.d("GoogleDirectionReadTask", "onPostExecute Entered");//
        //HashMap<String,String> directionsList = null;
        String[] directionsList;
        DataParser  parser = new DataParser();
        directionsList = parser.parseDirections(s);
        displayDirection(directionsList);


        /*distance = directionsList.get("distance");
        duration = directionsList.get("duration");

        Log.d("GoogleDirectionReadTask", "onPostExecute Exit");//

        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latlng);
        markerOptions.draggable(true);
        markerOptions.title("Duration="+duration);
        markerOptions.snippet(distance);

        mMap.addMarker(markerOptions);*/
    }

    public void displayDirection(String[] directionsList) {
        int count = directionsList.length;
        for(int i = 0 ; i < count ; i++)
        {
            PolylineOptions options = new PolylineOptions();
            options.color(Color.CYAN);
            options.width(10);
            options.addAll(PolyUtil.decode(directionsList[i]));

            mMap.addPolyline(options);
        }
    }
}
